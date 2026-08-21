package org.agentcloud.modules.baseagent.a2a;

import com.alibaba.cloud.ai.a2a.registry.nacos.discovery.NacosAgentCardProvider;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardWrapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * BaseAgent 通过 Nacos 发现远程 Agent 后，以 A2A JSON-RPC 协议委派任务。
 */
@Component
public class A2aDelegationTool {

    private static final String ANALYSIS_AGENT_NAME = "AnalysisAgent";

    private final NacosAgentCardProvider agentCardProvider;
    private final RestClient restClient;

    public A2aDelegationTool(NacosAgentCardProvider agentCardProvider, RestClient.Builder restClientBuilder) {
        this.agentCardProvider = agentCardProvider;
        this.restClient = restClientBuilder.build();
    }

    @Tool(name = "send_message", description = "将数据分析任务委派给已注册的 AnalysisAgent，并返回其 A2A 任务结果")
    public String sendMessage(
            @ToolParam(description = "目标 Agent 名称，当前仅允许 AnalysisAgent") String agentName,
            @ToolParam(description = "需要委派的完整数据分析任务") String task) {
        if (!ANALYSIS_AGENT_NAME.equals(agentName)) {
            throw new IllegalArgumentException("当前仅支持委派给 AnalysisAgent");
        }
        if (!StringUtils.hasText(task)) {
            throw new IllegalArgumentException("委派任务不能为空");
        }

        AgentCardWrapper agentCard = agentCardProvider.getAgentCard(agentName);
        if (agentCard == null || !StringUtils.hasText(agentCard.url())) {
            throw new IllegalStateException("Nacos 中未发现可调用的 Agent: " + agentName);
        }

        Map<String, Object> payload = Map.of(
                "jsonrpc", "2.0",
                "id", UUID.randomUUID().toString(),
                "method", "message/send",
                "params", Map.of(
                        "message", Map.of(
                                "kind", "message",
                                "messageId", UUID.randomUUID().toString(),
                                "role", "user",
                                "parts", List.of(Map.of("kind", "text", "text", task)))));

        return restClient.post()
                .uri(agentCard.url())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
    }
}
