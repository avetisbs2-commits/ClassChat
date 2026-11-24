package com.classchat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Smoke test: the whole Spring application (HTTP + WebSocket + STOMP broker) can start. */
@SpringBootTest
class ClassChatApplicationTests {

    @Test
    void contextLoads() {
    }
}
