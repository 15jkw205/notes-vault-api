package com.bluestaq.notesvault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluestaq.notesvault.exception.NoteNotFoundException;
import com.bluestaq.notesvault.model.Note;
import com.bluestaq.notesvault.repository.NoteRepository;
import com.bluestaq.notesvault.service.NoteService;

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

    // updateNote — should update content and return the updated note
    @Test
    void updateNote_shouldUpdateAndReturnNote() {
        UUID id = UUID.randomUUID();
        Note existing = new Note();
        existing.setContent("Original content");

        Note updatedNote = new Note();
        updatedNote.setContent("Updated content");

        when(noteRepository.findById(id)).thenReturn(Optional.of(existing));
        when(noteRepository.save(existing)).thenReturn(existing);

        Note result = noteService.updateNote(id, updatedNote);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        verify(noteRepository, times(1)).findById(id);
        verify(noteRepository, times(1)).save(existing);
    }

    // updateNote — should throw NoteNotFoundException when note does not exist
    @Test
    void updateNote_shouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        Note updatedNote = new Note();
        updatedNote.setContent("Updated content");

        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class,
                () -> noteService.updateNote(id, updatedNote));
        verify(noteRepository, times(1)).findById(id);
        verify(noteRepository, never()).save(any());
    }
}