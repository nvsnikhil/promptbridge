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

    // DTO classes for request bodies
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

    // --- 1. Search endpoint (Place BEFORE /{id}) ---
    @GetMapping("/search")
    public ResponseEntity<List<PromptDetailsDto>> searchMyPrompts(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        List<Prompt> prompts;

        if (query == null || query.trim().isEmpty()) {
            // Use DTO-returning method for full data
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

    // --- 2. Create prompt ---
    @PostMapping
    public ResponseEntity<PromptDetailsDto> createPrompt(
            @RequestBody CreatePromptRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);

        // Null-safe tags set
        Set<String> tags = request.tags != null ? request.tags : Set.of();

        // Create prompt
        Prompt newPrompt = promptService.createPrompt(
                request.title,
                request.description,
                request.content,
                currentUser.getId(),
                tags
        );

        // Re-fetch full prompt for complete data
        Prompt savedPrompt = promptService.getPromptById(newPrompt.getId());

        PromptDetailsDto dto = promptService.convertToDto(savedPrompt);
        return ResponseEntity.ok(dto);
    }

    // --- 3. Get current user's prompts ---
    @GetMapping("/my-prompts")
    public ResponseEntity<List<PromptDetailsDto>> getMyPrompts(Authentication authentication) {
        String userEmail = authentication.getName();
        List<PromptDetailsDto> promptDtos = promptService.getPromptDtosForUser(userEmail);
        return ResponseEntity.ok(promptDtos);
    }

    // --- 4. Get prompt by ID ---
    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailsDto> getPromptById(@PathVariable Long id) {
        PromptDetailsDto dto = promptService.getPromptDtoById(id);
        return ResponseEntity.ok(dto);
    }

    // --- 5. Add version to prompt ---
    @PostMapping("/{promptId}/versions")
    public ResponseEntity<PromptDetailsDto> addVersion(
            @PathVariable Long promptId,
            @RequestBody AddVersionRequest request) {

        Prompt updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);

        // Re-fetch full prompt for complete DTO
        Prompt savedPrompt = promptService.getPromptById(updatedPrompt.getId());

        PromptDetailsDto dto = promptService.convertToDto(savedPrompt);
        return ResponseEntity.ok(dto);
    }

    // --- 6. Delete prompt ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        promptService.deletePrompt(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- 7. Update prompt version ---
    @PutMapping("/versions/{versionId}")
    public ResponseEntity<Void> updateVersion(
            @PathVariable Long versionId,
            @RequestBody UpdateVersionRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        promptService.updatePromptVersion(versionId, request.content, currentUser);
        return ResponseEntity.ok().build();
    }
}
