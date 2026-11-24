package com.classchat.service;

/**
 * Thrown when a chat message breaks a rule (for example: blank text, or a nickname that is too long).
 * The controller catches it and tells the sender what was wrong.
 */
public class InvalidMessageException extends RuntimeException {

    public InvalidMessageException(String message) {
        super(message);
    }
}
