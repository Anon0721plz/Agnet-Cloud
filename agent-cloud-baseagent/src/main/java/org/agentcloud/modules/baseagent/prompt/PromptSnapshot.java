package org.agentcloud.modules.baseagent.prompt;

/**
 * Nacos 返回的已发布 Prompt 快照。
 */
public record PromptSnapshot(String template, String md5, String version) {
}
