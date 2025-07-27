package com.nikhil.promptbridge.repository;

import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    List<Prompt> findByUser(User user);

    // This is the new method for searching prompts.
    // It performs a case-insensitive search on the title and description.
    @Query("SELECT p FROM Prompt p WHERE p.user = :user AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Prompt> searchUserPrompts(@Param("user") User user, @Param("query") String query);
}
