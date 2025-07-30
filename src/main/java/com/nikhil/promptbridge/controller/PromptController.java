package com.nikhil.promptbridge.controller;

import com.nikhil.promptbridge.dto.PromptDetailsDto;
import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.service.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prompts")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    // DTO classes representing request bodies
    static class CreatePromptRequest {
        public String title;
        public String description;
        public String content;
        public Set<String> tags;
    }

    static class AddVersionRequest {
        public String content;
    }

    static class UpdateVersionRequest {
        public String content;
    }

    // --- 1. Search Endpoint (Placed before /{id}) ---
    @GetMapping("/search")
    public ResponseEntity<List<PromptDetailsDto>> searchMyPrompts(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        List<Prompt> prompts;

        if (query == null || query.trim().isEmpty()) {
            List<PromptDetailsDto> allDtos = promptService.getPromptDtosForUser(userEmail);
            return ResponseEntity.ok(allDtos);
        } else {
            prompts = promptService.searchUserPrompts(currentUser, query);
            List<PromptDetailsDto> dtos = prompts.stream()
                    .map(promptService::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        }
    }

    // --- 2. Create Prompt with Detailed Logging ---
    @PostMapping
    public ResponseEntity<PromptDetailsDto> createPrompt(@RequestBody CreatePromptRequest request,
                                                         Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);

        Prompt newPrompt = promptService.createPrompt(
                request.title,
                request.description,
                request.content,
                currentUser.getId(),
                request.tags
        );

        // Re-fetch prompt to load all associated data eagerly
        Prompt savedPrompt = promptService.getPromptById(newPrompt.getId());

        // --- Logging for verification ---
        System.out.println("SavedPrompt id: " + savedPrompt.getId());
        System.out.println("Title: " + savedPrompt.getTitle());
        System.out.println("Description: " + savedPrompt.getDescription());
        System.out.println("Tags: " + (savedPrompt.getTags() == null ? "null" :
                savedPrompt.getTags().stream()
                        .map(tag -> tag.getName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none")));
        System.out.println("Versions count: " + (savedPrompt.getVersions() == null ? "null" : savedPrompt.getVersions().size()));

        PromptDetailsDto dto = promptService.convertToDto(savedPrompt);

        System.out.println("DTO Title: " + dto.title());
        System.out.println("DTO Description: " + dto.description());
        System.out.println("DTO Tags count: " + (dto.tags() == null ? "null" : dto.tags().size()));
        System.out.println("DTO Versions count: " + (dto.versions() == null ? "null" : dto.versions().size()));
        // -------------------------------

        return ResponseEntity.ok(dto);
    }

    // --- 3. Get Current User's Prompts ---
    @GetMapping("/my-prompts")
    public ResponseEntity<List<PromptDetailsDto>> getMyPrompts(Authentication authentication) {
        String userEmail = authentication.getName();
        List<PromptDetailsDto> promptDtos = promptService.getPromptDtosForUser(userEmail);
        return ResponseEntity.ok(promptDtos);
    }

    // --- 4. Get Prompt by ID ---
    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailsDto> getPromptById(@PathVariable Long id) {
        PromptDetailsDto dto = promptService.getPromptDtoById(id);
        return ResponseEntity.ok(dto);
    }

    // --- 5. Add Version to Prompt with Re-fetch ---
    @PostMapping("/{promptId}/versions")
    public ResponseEntity<PromptDetailsDto> addVersion(@PathVariable Long promptId,
                                                       @RequestBody AddVersionRequest request) {

        Prompt updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);

        Prompt savedPrompt = promptService.getPromptById(updatedPrompt.getId());

        PromptDetailsDto dto = promptService.convertToDto(savedPrompt);
        return ResponseEntity.ok(dto);
    }

    // --- 6. Delete Prompt ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        promptService.deletePrompt(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- 7. Update Prompt Version ---
    @PutMapping("/versions/{versionId}")
    public ResponseEntity<Void> updateVersion(@PathVariable Long versionId,
                                              @RequestBody UpdateVersionRequest request,
                                              Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        promptService.updatePromptVersion(versionId, request.content, currentUser);
        return ResponseEntity.ok().build();
    }
}
