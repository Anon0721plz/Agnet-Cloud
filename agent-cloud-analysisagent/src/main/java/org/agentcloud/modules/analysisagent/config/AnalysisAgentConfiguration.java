package org.agentcloud.modules.analysisagent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.agentcloud.modules.analysisagent.prompt.NacosPromptClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisAgentConfiguration {

    @Bean(name = "analysisAgent")
    public ReactAgent analysisAgent(ChatModel chatModel, NacosPromptClient promptClient) {
        return ReactAgent.builder()
                .name("AnalysisAgent")
                .description("Agent Cloud 数据分析助手")
                .model(chatModel)
                .instruction(promptClient.loadRequiredPrompt().template())
                .outputKey("messages")
                .build();
    }
}
