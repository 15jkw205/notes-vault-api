package com.bluestaq.notesvault.service;

import com.bluestaq.notesvault.exception.NoteNotFoundException;
import com.bluestaq.notesvault.model.Note;
import com.bluestaq.notesvault.repository.NoteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for Note operations.
 * Contains all business logic and acts as the bridge between
 * the controller and the repository.
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;

    // Constructor injection — preferred over @Autowired for testability
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    // Persists a new note to the database and returns the saved entity
    public Note createNote(Note note) {
        return noteRepository.save(note);
    }

    // Retrieves all notes from the database
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    // Retrieves a single note by ID — throws NoteNotFoundException if not found
    public Note getNoteById(UUID id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(
                        "Note not found with id: " + id));
    }

    // Deletes a note by ID — throws NoteNotFoundException if not found
    public void deleteNote(UUID id) {
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException("Note not found with id: " + id);
        }
        noteRepository.deleteById(id);
    }
}