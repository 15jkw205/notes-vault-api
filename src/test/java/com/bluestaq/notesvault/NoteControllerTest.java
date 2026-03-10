package com.bluestaq.notesvault;

import com.bluestaq.notesvault.model.Note;
import com.bluestaq.notesvault.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API level tests for NoteController.
 * Uses MockMvc to simulate HTTP requests against all four endpoints.
 * Runs against an H2 in-memory database defined in src/test/resources/application.properties.
 * The real PostgreSQL database is never touched during testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerTest {

    // MockMvc simulates HTTP requests without starting a real server
    @Autowired
    private MockMvc mockMvc;

    // Direct access to the repository for test setup and teardown
    @Autowired
    private NoteRepository noteRepository;

    // Wipe the database clean before each test to ensure test isolation
    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();
    }

    // POST /notes — happy path, should return 201 Created with the new note
    @Test
    void createNote_shouldReturn201() throws Exception {
        String body = "{\"content\": \"Test note\"}";

        mockMvc.perform(post("/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Test note"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    // POST /notes — blank content should return 400 Bad Request
    @Test
    void createNote_withBlankContent_shouldReturn400() throws Exception {
        String body = "{\"content\": \"\"}";

        mockMvc.perform(post("/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // GET /notes — should return 200 OK with a JSON array of all notes
    @Test
    void getAllNotes_shouldReturn200() throws Exception {
        Note note = new Note();
        note.setContent("Test note");
        noteRepository.save(note);

        mockMvc.perform(get("/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // GET /notes/{id} — valid ID should return 200 OK with the correct note
    @Test
    void getNoteById_shouldReturn200() throws Exception {
        Note note = new Note();
        note.setContent("Test note");
        Note saved = noteRepository.save(note);

        mockMvc.perform(get("/notes/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test note"));
    }

    // GET /notes/{id} — non-existent ID should return 404 Not Found
    @Test
    void getNoteById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/notes/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // DELETE /notes/{id} — valid ID should return 204 No Content
    @Test
    void deleteNote_shouldReturn204() throws Exception {
        Note note = new Note();
        note.setContent("Note to delete");
        Note saved = noteRepository.save(note);

        mockMvc.perform(delete("/notes/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    // DELETE /notes/{id} — non-existent ID should return 404 Not Found
    @Test
    void deleteNote_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/notes/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}