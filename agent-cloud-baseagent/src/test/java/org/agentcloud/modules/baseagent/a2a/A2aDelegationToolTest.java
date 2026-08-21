package org.agentcloud.modules.baseagent.a2a;

import com.alibaba.cloud.ai.a2a.registry.nacos.discovery.NacosAgentCardProvider;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class A2aDelegationToolTest {

    @Test
    void shouldDiscoverAnalysisAgentAndSendA2aMessage() throws Exception {
        NacosAgentCardProvider agentCardProvider = mock(NacosAgentCardProvider.class);
        AgentCardWrapper agentCard = mock(AgentCardWrapper.class);
        when(agentCardProvider.getAgentCard("AnalysisAgent")).thenReturn(agentCard);
        when(agentCard.url()).thenReturn("http://analysis-agent:9004/a2a");

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://analysis-agent:9004/a2a"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.method").value("message/send"))
                .andExpect(jsonPath("$.params.message.role").value("user"))
                .andExpect(jsonPath("$.params.message.parts[0].kind").value("text"))
                .andExpect(jsonPath("$.params.message.parts[0].text").value("分析本月销售趋势"))
                .andRespond(withSuccess("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"result\":{\"kind\":\"task\"}}",
                        MediaType.APPLICATION_JSON));

        Class<?> toolClass = Class.forName("org.agentcloud.modules.baseagent.a2a.A2aDelegationTool");
        Object tool = toolClass.getConstructor(NacosAgentCardProvider.class, RestClient.Builder.class)
                .newInstance(agentCardProvider, builder);
        Method sendMessage = toolClass.getMethod("sendMessage", String.class, String.class);

        String response = (String) sendMessage.invoke(tool, "AnalysisAgent", "分析本月销售趋势");

        assertEquals("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"result\":{\"kind\":\"task\"}}", response);
        server.verify();
    }
}
