package org.agentcloud.modules.nacos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/nacos/hello")
    public String hello() {
        return "hello agent-cloud-nacos";
    }
}
