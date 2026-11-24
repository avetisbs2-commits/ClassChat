package com.classchat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.classchat.dto.ChatMessage;
import com.classchat.dto.ChatRequest;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * An end-to-end test of the message flow, with NO browser. It starts the real application on a random
 * port and connects two Java STOMP clients, "Ana" and "Ben", exactly like two browser tabs would.
 * You do not have to change this file. Read it to see the whole flow in code.
 *
 * It passes with the starter AND after you finish the TODOs (it checks only valid messages).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatBroadcastTest {

    @Value("${local.server.port}")
    private int port;

    // The broker keeps a list of who is connected and what they subscribed to. We use it to wait.
    @Autowired
    private SimpUserRegistry userRegistry;

    @Test
    void messageFromOneUserIsReceivedByEveryUser() throws Exception {
        BlockingQueue<ChatMessage> anaInbox = new LinkedBlockingQueue<>();
        BlockingQueue<ChatMessage> benInbox = new LinkedBlockingQueue<>();
        StompSession ana = connectAndSubscribe(anaInbox);
        StompSession ben = connectAndSubscribe(benInbox);
        waitUntilSubscribed(2);   // do not send before both users are really listening

        // Ana sends a message to "/app/chat.send"  ->  ChatController.send(...)
        ana.send("/app/chat.send", new ChatRequest("Ana", "Hello from Ana"));

        // The broker delivers it to everybody who subscribed to "/topic/messages": Ana AND Ben.
        ChatMessage receivedByBen = benInbox.poll(5, TimeUnit.SECONDS);
        ChatMessage receivedByAna = anaInbox.poll(5, TimeUnit.SECONDS);

        assertNotNull(receivedByBen, "Ben should receive the message");
        assertEquals("Ana", receivedByBen.sender());
        assertEquals("Hello from Ana", receivedByBen.content());
        assertNotNull(receivedByAna, "the sender receives its own message too (that is how the chat shows it)");
        assertEquals("Hello from Ana", receivedByAna.content());

        // And the other way round
        ben.send("/app/chat.send", new ChatRequest("Ben", "Hi Ana!"));
        ChatMessage reply = anaInbox.poll(5, TimeUnit.SECONDS);
        assertNotNull(reply, "Ana should receive Ben's reply");
        assertEquals("Ben", reply.sender());
        assertEquals("Hi Ana!", reply.content());

        ana.disconnect();
        ben.disconnect();
    }

    /** Connects like a browser does, and puts every received chat message into the given queue. */
    private StompSession connectAndSubscribe(BlockingQueue<ChatMessage> inbox) throws Exception {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new JacksonJsonMessageConverter());   // JSON <-> Java objects

        StompSession session = client
                .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                inbox.add((ChatMessage) payload);
            }
        });
        return session;
    }

    /** SUBSCRIBE is asynchronous: wait until the broker has registered the given number of subscribers. */
    private void waitUntilSubscribed(int expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (countSubscribers() < expected) {
            if (System.nanoTime() > deadline) {
                fail("the subscriptions were not registered in time");
            }
            Thread.sleep(20);
        }
    }

    private long countSubscribers() {
        return userRegistry.getUsers().stream()
                .flatMap(user -> user.getSessions().stream())
                .flatMap(session -> session.getSubscriptions().stream())
                .filter(subscription -> subscription.getDestination().equals("/topic/messages"))
                .count();
    }
}
