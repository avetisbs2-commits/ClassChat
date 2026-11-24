package com.classchat.dto;

import java.time.Instant;

/**
 * What the server sends to every connected browser (as JSON).
 * Example: {"sender":"Ana","content":"Hello!","timestamp":"2026-11-24T10:15:30.123Z"}
 *
 * @param timestamp the moment when the SERVER processed the message (an Instant is a point on the UTC timeline)
 */
public record ChatMessage(String sender, String content, Instant timestamp) {
}
