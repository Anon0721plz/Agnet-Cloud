package org.agentcloud.modules.baseagent.prompt;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Prompt 变更后更新本地 Agent 的系统指令，查询失败时保留旧版本。
 */
@Component
public class AgentPromptRefresher {

    private static final Logger log = LoggerFactory.getLogger(AgentPromptRefresher.class);

    private final NacosPromptClient promptClient;
    private final ReactAgent baseAgent;

    public AgentPromptRefresher(NacosPromptClient promptClient, @Qualifier("baseAgent") ReactAgent baseAgent) {
        this.promptClient = promptClient;
        this.baseAgent = baseAgent;
    }

    @Scheduled(fixedDelayString = "${agent-cloud.prompt.refresh-interval:30s}")
    public void refresh() {
        try {
            promptClient.refreshPrompt().ifPresent(prompt -> {
                baseAgent.setInstruction(prompt.template());
                log.info("基础 Agent Prompt 已更新，版本: {}", prompt.version());
            });
        } catch (Exception exception) {
            log.error("刷新基础 Agent Prompt 失败，继续使用当前有效 Prompt", exception);
        }
    }
}
