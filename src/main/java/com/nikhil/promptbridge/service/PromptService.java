package com.nikhil.promptbridge.service;

import com.nikhil.promptbridge.dto.FeedbackDto;
import com.nikhil.promptbridge.dto.PromptDetailsDto;
import com.nikhil.promptbridge.dto.TagDto;
import com.nikhil.promptbridge.dto.UserDto;
import com.nikhil.promptbridge.dto.VersionDto;
import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.PromptVersion;
import com.nikhil.promptbridge.model.Tag;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.repository.PromptRepository;
import com.nikhil.promptbridge.repository.PromptVersionRepository;
import com.nikhil.promptbridge.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate; // Import Hibernate
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PromptService {

    private final PromptRepository promptRepository;
    private final UserRepository userRepository;
    private final PromptVersionRepository promptVersionRepository;
    private final TagService tagService;

    @Autowired
    public PromptService(PromptRepository promptRepository, UserRepository userRepository, PromptVersionRepository promptVersionRepository, TagService tagService) {
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
        this.promptVersionRepository = promptVersionRepository;
        this.tagService = tagService;
    }

    @Transactional
    public Prompt createPrompt(String title, String description, String content, Long userId, Set<String> tagNames) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Set<Tag> tags = tagService.findOrCreateTags(tagNames);
        Prompt prompt = new Prompt();
        prompt.setTitle(title);
        prompt.setDescription(description);
        prompt.setUser(user);
        prompt.setTags(tags);
        PromptVersion firstVersion = new PromptVersion();
        firstVersion.setContent(content);
        firstVersion.setVersionNumber(1);
        firstVersion.setPrompt(prompt);
        prompt.getVersions().add(firstVersion);
        return promptRepository.save(prompt);
    }

    // This is the new, robust method for getting a fully loaded prompt
    @Transactional(readOnly = true)
    public Prompt getPromptByIdWithDetails(Long promptId) {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new EntityNotFoundException("Prompt not found with id: " + promptId));
        // Explicitly initialize the lazy-loaded collections
        Hibernate.initialize(prompt.getTags());
        Hibernate.initialize(prompt.getVersions());
        for (PromptVersion version : prompt.getVersions()) {
            Hibernate.initialize(version.getFeedback());
        }
        return prompt;
    }

    @Transactional(readOnly = true)
    public List<Prompt> getPromptsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + userEmail));
        List<Prompt> prompts = promptRepository.findByUser(user);
        // Eagerly load the necessary collections for the entire list
        prompts.forEach(p -> {
            Hibernate.initialize(p.getTags());
            Hibernate.initialize(p.getVersions());
        });
        return prompts;
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public Prompt addVersionToPrompt(Long promptId, String newContent) {
        Prompt prompt = getPromptByIdWithDetails(promptId); // Use the new method
        PromptVersion newVersion = new PromptVersion();
        newVersion.setContent(newContent);
        int nextVersion = prompt.getVersions().stream().mapToInt(PromptVersion::getVersionNumber).max().orElse(0) + 1;
        newVersion.setVersionNumber(nextVersion);
        newVersion.setPrompt(prompt);
        prompt.getVersions().add(newVersion);
        return promptRepository.save(prompt);
    }

    @Transactional
    public void deletePrompt(Long promptId, User currentUser) {
        Prompt promptToDelete = getPromptByIdWithDetails(promptId); // Use the new method
        if (!Objects.equals(promptToDelete.getUser().getId(), currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this prompt.");
        }
        promptRepository.delete(promptToDelete);
    }

    @Transactional
    public PromptVersion updatePromptVersion(Long versionId, String newContent, User currentUser) {
        PromptVersion versionToUpdate = promptVersionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Prompt version not found"));
        if (!Objects.equals(versionToUpdate.getPrompt().getUser().getId(), currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this prompt version.");
        }
        versionToUpdate.setContent(newContent);
        return promptVersionRepository.save(versionToUpdate);
    }
    
    @Transactional(readOnly = true)
    public List<Prompt> searchUserPrompts(User user, String query) {
        List<Prompt> prompts = promptRepository.searchUserPrompts(user, query);
        // Eagerly load the necessary collections
        prompts.forEach(p -> {
            Hibernate.initialize(p.getTags());
            Hibernate.initialize(p.getVersions());
        });
        return prompts;
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
        
        Set<TagDto> tagDtos = prompt.getTags().stream()
                .map(tag -> new TagDto(tag.getId(), tag.getName()))
                .collect(Collectors.toSet());

        return new PromptDetailsDto(prompt.getId(), prompt.getTitle(), prompt.getDescription(), versionDtos, tagDtos);
    }
}
