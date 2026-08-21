package org.agentcloud.modules.baseagent;

import org.agentcloud.modules.baseagent.prompt.NacosPromptProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(NacosPromptProperties.class)
public class AgentCloudBaseAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentCloudBaseAgentApplication.class, args);
    }

}
