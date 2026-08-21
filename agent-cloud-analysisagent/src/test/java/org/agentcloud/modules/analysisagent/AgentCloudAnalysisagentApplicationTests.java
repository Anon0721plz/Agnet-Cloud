package org.agentcloud.modules.analysisagent;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentCloudAnalysisagentApplicationTests {

    @Test
    void shouldDeclareAnalysisAgentBeanForA2aRegistration() throws Exception {
        Class<?> configurationClass = Class.forName(
                "org.agentcloud.modules.analysisagent.config.AnalysisAgentConfiguration");

        assertTrue(configurationClass.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        assertDoesNotThrow(() -> {
            boolean hasAnalysisAgentBean = java.util.Arrays.stream(configurationClass.getDeclaredMethods())
                    .anyMatch(method -> method.isAnnotationPresent(Bean.class)
                            && java.util.Arrays.asList(method.getAnnotation(Bean.class).name()).contains("analysisAgent"));
            if (!hasAnalysisAgentBean) {
                throw new AssertionError("AnalysisAgent 必须声明名为 analysisAgent 的 Spring Bean");
            }
        });
    }

}
