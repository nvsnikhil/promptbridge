package com.nikhil.promptbridge.service;

import com.nikhil.promptbridge.dto.FeedbackDto;
import com.nikhil.promptbridge.dto.PromptDetailsDto;
import com.nikhil.promptbridge.dto.UserDto;
import com.nikhil.promptbridge.dto.VersionDto;
import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.PromptVersion;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.repository.PromptRepository;
import com.nikhil.promptbridge.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PromptService {

    private final PromptRepository promptRepository;
    private final UserRepository userRepository;

    @Autowired
    public PromptService(PromptRepository promptRepository, UserRepository userRepository) {
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Prompt createPrompt(String title, String description, String content, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
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
        return promptRepository.findById(promptId).orElseThrow(() -> new EntityNotFoundException("Prompt not found with id: " + promptId));
    }

    public List<Prompt> getPromptsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + userEmail));
        return promptRepository.findByUser(user);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public Prompt addVersionToPrompt(Long promptId, String newContent) {
        Prompt prompt = promptRepository.findById(promptId).orElseThrow(() -> new EntityNotFoundException("Prompt not found with id: " + promptId));
        PromptVersion newVersion = new PromptVersion();
        newVersion.setContent(newContent);
        
        // Robust version numbering
        int nextVersion = prompt.getVersions().stream()
                .mapToInt(PromptVersion::getVersionNumber)
                .max().orElse(0) + 1;
        newVersion.setVersionNumber(nextVersion);
        newVersion.setPrompt(prompt);
        prompt.getVersions().add(newVersion);
        return promptRepository.save(prompt);
    }

    // This is the new, more secure method for deleting a prompt
    @Transactional
    public void deletePrompt(Long promptId, User currentUser) {
        Prompt promptToDelete = promptRepository.findById(promptId)
                .orElseThrow(() -> new EntityNotFoundException("Prompt not found with id: " + promptId));

        // Security Check: Ensure the user owns the prompt
        if (!Objects.equals(promptToDelete.getUser().getId(), currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this prompt.");
        }
        
        promptRepository.delete(promptToDelete);
    }
    
    public PromptDetailsDto convertToDto(Prompt prompt) {
        List<VersionDto> versionDtos = prompt.getVersions().stream()
            .map(version -> {
                List<FeedbackDto> feedbackDtos = Optional.ofNullable(version.getFeedback())
                    .orElse(List.of())
                    .stream()
                    .map(feedback -> new FeedbackDto(
                        feedback.getRating(),
                        feedback.getComment(),
                        new UserDto(feedback.getUser().getName())
                    ))
                    .collect(Collectors.toList());
                return new VersionDto(version.getId(), version.getVersionNumber(), version.getContent(), feedbackDtos);
            })
            .sorted((v1, v2) -> Integer.compare(v2.versionNumber(), v1.versionNumber()))
            .collect(Collectors.toList());
        return new PromptDetailsDto(prompt.getId(), prompt.getTitle(), prompt.getDescription(), versionDtos);
    }
}
