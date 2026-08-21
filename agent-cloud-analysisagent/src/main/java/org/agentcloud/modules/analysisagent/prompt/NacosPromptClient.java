package org.agentcloud.modules.analysisagent.prompt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通过 Nacos OpenAPI 读取已发布 Prompt。当前 nacos-client 版本没有 Prompt Java SDK，
 * 因此使用官方 HTTP API，并仅在此类中处理鉴权和缓存。
 */
@Component
public class NacosPromptClient {

    private static final Logger log = LoggerFactory.getLogger(NacosPromptClient.class);

    private final NacosPromptProperties properties;
    private final RestClient restClient;
    private final AtomicReference<PromptSnapshot> currentPrompt = new AtomicReference<>();
    private volatile AccessToken accessToken;

    public NacosPromptClient(NacosPromptProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder == null || properties == null ? null : restClientBuilder
                .baseUrl("http://" + properties.getServerAddr() + "/nacos")
                .build();
    }

    public PromptSnapshot loadRequiredPrompt() {
        PromptSnapshot prompt = queryPrompt(null)
                .orElseThrow(() -> new IllegalStateException("Nacos 未返回 Prompt: " + promptKey()));
        validateTemplate(prompt);
        currentPrompt.set(prompt);
        return prompt;
    }

    public Optional<PromptSnapshot> refreshPrompt() {
        PromptSnapshot previous = currentPrompt.get();
        if (previous == null) {
            return Optional.of(loadRequiredPrompt());
        }

        Optional<PromptSnapshot> prompt = queryPrompt(previous.md5());
        prompt.ifPresent(value -> {
            validateTemplate(value);
            currentPrompt.set(value);
        });
        return prompt;
    }

    private Optional<PromptSnapshot> queryPrompt(String md5) {
        try {
            return doQuery(getAccessToken(), md5);
        } catch (HttpClientErrorException.Unauthorized exception) {
            accessToken = null;
            return doQuery(getAccessToken(), md5);
        }
    }

    private Optional<PromptSnapshot> doQuery(String token, String md5) {
        ResponseEntity<PromptResponse> response = restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/v3/client/ai/prompt")
                            .queryParam("promptKey", promptKey())
                            .queryParam("label", properties.getLabel());
                    if (StringUtils.hasText(md5)) {
                        uriBuilder.queryParam("md5", md5);
                    }
                    return uriBuilder.build();
                })
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .toEntity(PromptResponse.class);

        if (response.getStatusCode().value() == 304) {
            return Optional.empty();
        }
        PromptResponse body = response.getBody();
        if (body == null || body.code() != 0 || body.data() == null) {
            throw new IllegalStateException("读取 Nacos Prompt 失败: " + promptKey());
        }
        return Optional.of(new PromptSnapshot(body.data().template(), body.data().md5(), body.data().version()));
    }

    private String getAccessToken() {
        AccessToken cached = accessToken;
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return cached.value();
        }
        synchronized (this) {
            cached = accessToken;
            if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
                return cached.value();
            }
            accessToken = login();
            return accessToken.value();
        }
    }

    private AccessToken login() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", properties.getUsername());
        form.add("password", properties.getPassword());
        LoginResponse response = restClient.post()
                .uri("/v3/auth/user/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(LoginResponse.class);
        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new IllegalStateException("Nacos 登录失败，无法读取 Prompt: " + promptKey());
        }
        long ttl = response.tokenTtl() == null ? 60 : Math.max(60, response.tokenTtl());
        Instant expiresAt = Instant.now().plusSeconds(Math.max(1, ttl - 30));
        log.debug("已获取 Nacos Prompt API 访问令牌，过期时间: {}", expiresAt);
        return new AccessToken(response.accessToken(), expiresAt);
    }

    private String promptKey() {
        return properties == null ? "agent-cloud-analysisagent-system-prompt" : properties.getPromptKey();
    }

    private void validateTemplate(PromptSnapshot prompt) {
        if (prompt == null || !StringUtils.hasText(prompt.template())) {
            throw new IllegalStateException("Nacos Prompt 模板为空: " + promptKey());
        }
    }

    private record AccessToken(String value, Instant expiresAt) {
    }

    private record LoginResponse(String accessToken, Long tokenTtl) {
    }

    private record PromptResponse(int code, PromptData data) {
    }

    private record PromptData(String template, String md5, String version) {
    }
}
