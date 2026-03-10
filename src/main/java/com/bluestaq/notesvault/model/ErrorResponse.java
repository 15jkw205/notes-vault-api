package com.bluestaq.notesvault.model;

import java.time.LocalDateTime;

/**
 * Standard error response returned by the API when something goes wrong.
 * All error responses include a status code, a message, and a timestamp.
 */
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;

    // Automatically sets the timestamp when the error response is created
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}