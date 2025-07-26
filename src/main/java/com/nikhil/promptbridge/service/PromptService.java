package com.nikhil.promptbridge.service;

import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.PromptVersion;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.repository.PromptRepository;
import com.nikhil.promptbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PromptService {

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Prompt createPrompt(String title, String description, String content, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Prompt prompt = new Prompt();
        prompt.setTitle(title);
        prompt.setDescription(description);
        prompt.setUser(user);

        PromptVersion firstVersion = new PromptVersion();
        firstVersion.setContent(content);
        firstVersion.setVersionNumber(1);
        firstVersion.setPrompt(prompt);

        prompt.getVersions().add(firstVersion);

        return promptRepository.save(prompt);
    }

    public Prompt getPromptById(Long promptId) {
        return promptRepository.findById(promptId)
                .orElseThrow(() -> new RuntimeException("Prompt not found with id: " + promptId));
    }

    public List<Prompt> getPromptsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        return promptRepository.findByUser(user);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Transactional
    public Prompt addVersionToPrompt(Long promptId, String newContent) {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new RuntimeException("Prompt not found with id: " + promptId));

        PromptVersion newVersion = new PromptVersion();
        newVersion.setContent(newContent);
        newVersion.setVersionNumber(prompt.getVersions().size() + 1);
        newVersion.setPrompt(prompt);

        prompt.getVersions().add(newVersion);

        return promptRepository.save(prompt);
    }
}
