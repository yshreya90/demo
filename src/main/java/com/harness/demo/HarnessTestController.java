package com.harness.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HarnessTestController {

    @GetMapping("/heartbeat")
    public String heartbeat() {
        return "Application is up and running!";
    }
}
