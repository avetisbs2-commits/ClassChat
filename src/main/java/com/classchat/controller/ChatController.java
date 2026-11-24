package com.classchat.controller;

import com.classchat.dto.ChatMessage;
import com.classchat.dto.ChatRequest;
import com.classchat.dto.ErrorMessage;
import com.classchat.service.ChatService;
import com.classchat.service.InvalidMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * Receives chat messages from browsers over WebSocket.
 * It looks like a Spring MVC controller, but @MessageMapping handles STOMP messages, not HTTP requests.
 */
@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * A browser sends a STOMP SEND frame to "/app/chat.send".
     * ("/app" is the prefix set in WebSocketConfig; "/chat.send" is the part we map here.)
     *
     * @SendTo takes the returned object, converts it to JSON and publishes it to "/topic/messages".
     * The built-in broker then delivers it to EVERY browser that is subscribed to that topic.
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessage send(ChatRequest request) {
        return chatService.process(request);
    }

    /**
     * If the service throws InvalidMessageException, nothing is broadcast.
     * Instead, only the sender receives the error on their private destination "/user/queue/errors".
     */
    @MessageExceptionHandler(InvalidMessageException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorMessage handleInvalidMessage(InvalidMessageException e) {
        return new ErrorMessage(e.getMessage());
    }

    /** Any other problem (for example broken JSON): tell the sender, and write the details in the log. */
    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public ErrorMessage handleOther(Exception e) {
        log.warn("Could not process a chat message", e);
        return new ErrorMessage("The server could not process your message.");
    }
}
