package org.agentcloud.modules.baseagent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.agentcloud.modules.baseagent.a2a.A2aDelegationTool;
import org.agentcloud.modules.baseagent.prompt.NacosPromptClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    @Bean(name = "baseAgent")
    public ReactAgent baseAgent(ChatModel chatModel, NacosPromptClient promptClient, A2aDelegationTool a2aDelegationTool) {
        return ReactAgent.builder()
                .name("BaseAgent")
                .description("Agent Cloud 基础问答助手")
                .model(chatModel)
                .instruction(promptClient.loadRequiredPrompt().template())
                .methodTools(a2aDelegationTool)
                .outputKey("messages")
                .build();
    }
}
