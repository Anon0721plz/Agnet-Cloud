package org.agentcloud.modules.analysisagent;

import org.agentcloud.modules.analysisagent.prompt.NacosPromptProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(NacosPromptProperties.class)
public class AgentCloudAnalysisagentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentCloudAnalysisagentApplication.class, args);
    }

}
