package org.agentcloud.modules.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/system/hello")
    public String hello() {
        return "hello agent-cloud-system";
    }
}
