package com.bluestaq.notesvault.repository;

import com.bluestaq.notesvault.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Repository interface for Note entities.
 * Extends JpaRepository to inherit standard CRUD operations —
 * save, findById, findAll, deleteById, existsById and more.
 * No custom queries needed for the current feature set.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
}