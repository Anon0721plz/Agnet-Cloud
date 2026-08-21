package org.agentcloud.modules.baseagent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.agentcloud.modules.baseagent.prompt.NacosPromptClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    @Bean(name = "baseAgent")
    public ReactAgent baseAgent(ChatModel chatModel, NacosPromptClient promptClient) {
        return ReactAgent.builder()
                .name("BaseAgent")
                .description("Agent Cloud 基础问答助手")
                .model(chatModel)
                .instruction(promptClient.loadRequiredPrompt().template())
                .outputKey("messages")
                .build();
    }
}
