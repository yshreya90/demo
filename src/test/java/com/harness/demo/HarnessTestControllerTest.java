package com.harness.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HarnessTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHeartbeatEndpoint() throws Exception {
        mockMvc.perform(get("/heartbeat"))
                .andExpect(status().isOk())
                .andExpect(content().string("Application is up and running!"));
    }
}