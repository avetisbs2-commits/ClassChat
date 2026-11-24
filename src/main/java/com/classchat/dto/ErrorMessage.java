package com.classchat.dto;

/**
 * Sent only to the ONE user who sent a bad message (not to everybody).
 * Example: {"message":"Message must not be blank."}
 */
public record ErrorMessage(String message) {
}
