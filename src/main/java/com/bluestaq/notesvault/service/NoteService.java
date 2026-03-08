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
}