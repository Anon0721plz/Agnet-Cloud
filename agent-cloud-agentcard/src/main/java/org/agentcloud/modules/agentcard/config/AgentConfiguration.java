package org.agentcloud.modules.agentcard.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    @Bean(name = "baseAgent")
    public ReactAgent baseAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("BaseAgent")
                .description("Agent Cloud 基础问答助手")
                .model(chatModel)
                .instruction("你是 Agent Cloud 的基础问答助手。请使用中文回答，并保持回答准确、简洁。")
                .outputKey("messages")
                .build();
    }
}
