package com.bluestaq.notesvault.service;

import com.bluestaq.notesvault.model.Note;
import com.bluestaq.notesvault.repository.NoteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note createNote(Note note) {
        return noteRepository.save(note);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note getNoteById(UUID id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));
    }

    public void deleteNote(UUID id) {
    if (!noteRepository.existsById(id)) {
        throw new RuntimeException("Note not found with id: " + id);
    }
    noteRepository.deleteById(id);
    }
}