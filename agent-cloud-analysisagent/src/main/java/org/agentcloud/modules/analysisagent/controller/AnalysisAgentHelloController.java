package org.agentcloud.modules.analysisagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisAgentHelloController {

    @GetMapping("/agent/hello")
    public String hello() {
        return "hello agent-cloud-analysisagent";
    }
}
