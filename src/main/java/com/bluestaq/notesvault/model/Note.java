package com.bluestaq.notesvault.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a note in the Notes Vault system.
 * Maps to the 'notes' table in PostgreSQL.
 */
@Entity
@Table(name = "notes")
public class Note {

    // Auto-generated UUID primary key
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Note content — cannot be blank and must not exceed 500 characters
    @NotBlank(message = "Content cannot be blank")
    @Size(max = 500, message = "Content cannot exceed 500 characters")
    @Column(nullable = false)
    private String content;

    // Timestamp set automatically when the note is first persisted
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatically sets createdAt before the entity is saved
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}