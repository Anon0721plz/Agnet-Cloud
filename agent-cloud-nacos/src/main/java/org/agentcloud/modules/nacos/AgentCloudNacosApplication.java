package org.agentcloud.modules.nacos;

import com.alibaba.nacos.NacosServerBasicApplication;
import com.alibaba.nacos.NacosServerWebApplication;
import com.alibaba.nacos.console.NacosConsole;
import com.alibaba.nacos.core.listener.startup.NacosStartUp;
import com.alibaba.nacos.core.listener.startup.NacosStartUpManager;
import com.alibaba.nacos.sys.env.Constants;
import com.alibaba.nacos.sys.env.DeploymentType;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Agent Cloud 一体化 Nacos（server + console）启动类。
 *
 * <p>对齐官方 NacosBootstrap.startWithConsole 的 merged 部署序列：core 上下文（无 Web）→ server Web 上下文（8848）→
 * console 上下文（8080）。本应用即完整 Nacos Server，直连 MySQL（nacos-agent-cloud 库，与 E:\nacos3.2.3 部署目录共用数据），
 * 无需单独启动 Nacos Server。</p>
 *
 * <p>Console 源码整体拷贝自 F:\nacos-3.2.3 源码工程的 nacos-console 模块（包名 com.alibaba.nacos.console 保持不变，
 * 便于后续与上游同步）。</p>
 */
public class AgentCloudNacosApplication {

    public static void main(String[] args) throws Exception {
        // banner 中的 ${pid} 占位符需要（Spring Boot 仅特供 application.version 等少数变量）
        System.setProperty("pid", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        // merged 部署模式：core + server(8848) + console(8080) 一体
        System.setProperty(Constants.NACOS_DEPLOYMENT_TYPE, Constants.NACOS_DEPLOYMENT_TYPE_MERGED);
        EnvUtil.setDeploymentType(DeploymentType.MERGED);
        // 对齐官方 startup.cmd -m standalone：单机模式，否则 member 寻址走 address-server（jmenv.tbsite.net）导致启动失败
        System.setProperty(Constants.STANDALONE_MODE_PROPERTY_NAME, "true");
        // 独立的 nacos.home（数据/日志目录），避免与 C:\Users\&lt;user&gt;\nacos 或部署目录 E:\nacos3.2.3 相互干扰
        if (System.getProperty(EnvUtil.NACOS_HOME_KEY) == null) {
            Path home = Paths.get("nacos-home").toAbsolutePath();
            Files.createDirectories(home.resolve("logs"));
            System.setProperty(EnvUtil.NACOS_HOME_KEY, home.toString());
        }
        // 对齐官方 startup.cmd 的 CUSTOM_SEARCH_LOCATIONS：NacosCoreStartUp 启动时强制读取
        // additional-location 下的 application.properties（官方发行包的 conf/ 目录），实际配置仍由本应用 application.yml 提供
        Path confDir = Paths.get(System.getProperty(EnvUtil.NACOS_HOME_KEY), "conf");
        Files.createDirectories(confDir);
        Path confFile = confDir.resolve("application.properties");
        if (Files.notExists(confFile)) {
            Files.writeString(confFile, "# placeholder for NacosCoreStartUp, real config lives in application.yml\n");
        }
        System.setProperty("spring.config.additional-location",
            "file:" + confDir.toAbsolutePath().toString().replace('\\', '/') + "/");
        // core 上下文：注册中心/配置中心核心服务（无 Web）
        NacosStartUpManager.start(NacosStartUp.CORE_START_UP_PHASE);
        ConfigurableApplicationContext coreContext = new SpringApplicationBuilder(NacosServerBasicApplication.class)
            .web(WebApplicationType.NONE).run(args);
        // server Web 上下文：8848 主端口（OpenAPI/Admin API，客户端注册与配置读写）
        NacosStartUpManager.start(NacosStartUp.WEB_START_UP_PHASE);
        new SpringApplicationBuilder(NacosServerWebApplication.class).parent(coreContext).run(args);
        // console 上下文：8080 控制台（UI + /v3/console/*）。
        // 端口/上下文/编码映射须挂在本上下文上（web 上下文的 server.port 由 nacos-server.jar 内置配置映射 8848，
        // 不能共用 application.yml 的 server.port），等价于官方 nacos-console.properties
        NacosStartUpManager.start(NacosStartUp.CONSOLE_START_UP_PHASE);
        new SpringApplicationBuilder(NacosConsole.class).parent(coreContext)
            .properties("server.port=${nacos.console.port:8080}",
                "server.servlet.context-path=${nacos.console.contextPath:}",
                "server.servlet.encoding.enabled=true", "server.servlet.encoding.force=true",
                "server.servlet.encoding.charset=UTF-8")
            .run(args);
    }
}
