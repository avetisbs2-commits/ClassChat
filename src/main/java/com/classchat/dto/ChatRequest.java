package com.classchat.dto;

/**
 * What the browser sends to the server (as JSON) when a user presses "Send".
 * Example: {"sender":"Ana","content":"Hello!"}
 *
 * Note: there is NO timestamp here. The time is decided by the server, never by the browser,
 * because a browser clock can be wrong (or changed on purpose).
 */
public record ChatRequest(String sender, String content) {
}
