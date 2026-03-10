package com.bluestaq.notesvault.exception;

/**
 * Thrown when a note cannot be found by the given ID.
 * Handled by GlobalExceptionHandler and returns a 404 Not Found response.
 */
public class NoteNotFoundException extends RuntimeException {
    public NoteNotFoundException(String message) {
        super(message);
    }
}