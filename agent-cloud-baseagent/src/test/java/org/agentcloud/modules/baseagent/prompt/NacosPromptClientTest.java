package org.agentcloud.modules.baseagent.prompt;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NacosPromptClientTest {

    @Test
    void shouldLoginAndLoadPublishedPrompt() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NacosPromptProperties properties = new NacosPromptProperties();
        NacosPromptClient client = new NacosPromptClient(properties, builder);

        server.expect(requestTo("http://127.0.0.1:8848/nacos/v3/auth/user/login"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("username=nacos&password=nacos"))
                .andRespond(withSuccess("{\"accessToken\":\"test-token\",\"tokenTtl\":18000}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://127.0.0.1:8848/nacos/v3/client/ai/prompt?promptKey=agent-cloud-baseagent-system-prompt&label=latest"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("{\"code\":0,\"data\":{\"template\":\"你是基础问答助手\",\"md5\":\"md5-value\",\"version\":\"0.0.1\"}}", MediaType.APPLICATION_JSON));

        PromptSnapshot prompt = client.loadRequiredPrompt();

        assertEquals("你是基础问答助手", prompt.template());
        assertEquals("md5-value", prompt.md5());
        server.verify();
    }
}
