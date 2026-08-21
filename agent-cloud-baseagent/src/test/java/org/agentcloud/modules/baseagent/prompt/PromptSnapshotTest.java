package org.agentcloud.modules.baseagent.prompt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromptSnapshotTest {

    @Test
    void shouldExposePromptTemplateAndMetadata() {
        PromptSnapshot prompt = new PromptSnapshot("你是基础问答助手", "md5-value", "0.0.1");

        assertEquals("你是基础问答助手", prompt.template());
        assertEquals("md5-value", prompt.md5());
        assertEquals("0.0.1", prompt.version());
    }
}
