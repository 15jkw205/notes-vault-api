package com.bluestaq.notesvault.controller;

import com.bluestaq.notesvault.model.Note;
import com.bluestaq.notesvault.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for the Notes Vault API.
 * Exposes all four endpoints — POST, GET all, GET by ID, and DELETE.
 * Delegates all business logic to NoteService.
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    // Constructor injection — preferred over @Autowired for testability
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // POST /notes — creates a new note, returns 201 Created
    @PostMapping
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note) {
        Note created = noteService.createNote(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /notes — retrieves all notes, returns 200 OK with JSON array
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    // GET /notes/{id} — retrieves a note by ID, returns 200 OK or 404
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable UUID id) {
        Note note = noteService.getNoteById(id);
        return ResponseEntity.ok(note);
    }

    // DELETE /notes/{id} — deletes a note by ID, returns 204 No Content or 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /notes/{id} — updates an existing note, returns 200 OK or 404
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable UUID id,
            @Valid @RequestBody Note note) {
        Note updated = noteService.updateNote(id, note);
        return ResponseEntity.ok(updated);
    }
}