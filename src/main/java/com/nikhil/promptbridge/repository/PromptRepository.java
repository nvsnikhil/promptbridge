package com.nikhil.promptbridge.repository;

import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- ADD THIS IMPORT

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    
    // v-- ADD THIS NEW METHOD --v
    List<Prompt> findByUser(User user);
}