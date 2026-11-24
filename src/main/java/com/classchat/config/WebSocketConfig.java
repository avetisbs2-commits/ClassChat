package com.classchat.config;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * Turns on WebSocket + STOMP and defines the "address book" of the chat.
 *
 * Three words to know:
 *  - WebSocket: one long-lived connection between a browser and the server. Both sides can send at any time.
 *  - STOMP:     a small text protocol on top of WebSocket. It adds "destinations" (like addresses),
 *               and the commands CONNECT, SUBSCRIBE and SEND.
 *  - Broker:    the part that receives a message for a destination and delivers it to all subscribers.
 *               Here it is Spring's simple in-memory broker (no extra server like RabbitMQ is needed).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The browser starts at http(s)://host/ws with a normal HTTP request ("handshake") that asks to
        // upgrade to WebSocket. After that, the same connection carries STOMP frames.
        // We do not call setAllowedOrigins(...): by default only the SAME origin (the page served by this
        // application) may connect. That is what we want, and it is the safe default.
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new UniqueUserHandshakeHandler());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Destinations that START with these prefixes are handled by the broker itself.
        //   /topic/...  one message goes to ALL subscribers (our chat room)
        //   /queue/...  used for private messages to one user (we use it for errors)
        registry.enableSimpleBroker("/topic", "/queue");

        // Messages that a browser sends to "/app/..." are routed to our @MessageMapping methods.
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Gives every WebSocket connection its own random "user name".
     * Why? So the server can answer ONE connection privately (see ChatController, @SendToUser).
     * There is no login in ClassChat, so this name is only an internal id; it is not the nickname.
     */
    private static class UniqueUserHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                          Map<String, Object> attributes) {
            String id = UUID.randomUUID().toString();
            return () -> id;
        }
    }
}
