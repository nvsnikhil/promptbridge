package com.nikhil.promptbridge.repository;

import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    // Eagerly load versions, tags, and feedback
    @EntityGraph(attributePaths = {"versions", "tags", "versions.feedback"})
    List<Prompt> findByUser(User user);

    // Same eager loading for search with query
    @EntityGraph(attributePaths = {"versions", "tags", "versions.feedback"})
    @Query("SELECT p FROM Prompt p WHERE p.user = :user AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Prompt> searchUserPrompts(@Param("user") User user, @Param("query") String query);
}
