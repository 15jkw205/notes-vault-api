package com.bluestaq.notesvault;

import com.bluestaq.notesvault.exception.NoteNotFoundException;
import com.bluestaq.notesvault.model.Note;
import com.bluestaq.notesvault.repository.NoteRepository;
import com.bluestaq.notesvault.service.NoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Service layer tests for NoteService.
 * Uses Mockito to mock the NoteRepository so business logic
 * can be tested in complete isolation from the database.
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    // Mocked repository — no real database calls are made
    @Mock
    private NoteRepository noteRepository;

    // NoteService instance with the mocked repository injected
    @InjectMocks
    private NoteService noteService;

    // createNote — should save the note and return it
    @Test
    void createNote_shouldSaveAndReturnNote() {
        Note note = new Note();
        note.setContent("Test note");

        when(noteRepository.save(note)).thenReturn(note);

        Note result = noteService.createNote(note);

        assertNotNull(result);
        assertEquals("Test note", result.getContent());
        verify(noteRepository, times(1)).save(note);
    }

    // getAllNotes — should return a list of all notes
    @Test
    void getAllNotes_shouldReturnAllNotes() {
        Note note1 = new Note();
        note1.setContent("First note");

        Note note2 = new Note();
        note2.setContent("Second note");

        when(noteRepository.findAll()).thenReturn(List.of(note1, note2));

        List<Note> result = noteService.getAllNotes();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(noteRepository, times(1)).findAll();
    }

    // getNoteById — should return the correct note when found
    @Test
    void getNoteById_shouldReturnNoteWhenFound() {
        UUID id = UUID.randomUUID();
        Note note = new Note();
        note.setContent("Test note");

        when(noteRepository.findById(id)).thenReturn(Optional.of(note));

        Note result = noteService.getNoteById(id);

        assertNotNull(result);
        assertEquals("Test note", result.getContent());
        verify(noteRepository, times(1)).findById(id);
    }

    // getNoteById — should throw NoteNotFoundException when note does not exist
    @Test
    void getNoteById_shouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class,
                () -> noteService.getNoteById(id));
        verify(noteRepository, times(1)).findById(id);
    }

    // deleteNote — should delete the note when it exists
    @Test
    void deleteNote_shouldDeleteNoteWhenFound() {
        UUID id = UUID.randomUUID();

        when(noteRepository.existsById(id)).thenReturn(true);
        doNothing().when(noteRepository).deleteById(id);

        noteService.deleteNote(id);

        verify(noteRepository, times(1)).existsById(id);
        verify(noteRepository, times(1)).deleteById(id);
    }

    // deleteNote — should throw NoteNotFoundException when note does not exist
    @Test
    void deleteNote_shouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(noteRepository.existsById(id)).thenReturn(false);

        assertThrows(NoteNotFoundException.class,
                () -> noteService.deleteNote(id));
        verify(noteRepository, times(1)).existsById(id);
        verify(noteRepository, never()).deleteById(id);
    }
}