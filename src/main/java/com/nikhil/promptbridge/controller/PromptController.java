package com.nikhil.promptbridge.controller;

import com.nikhil.promptbridge.dto.PromptDetailsDto;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.service.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/prompts")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    // *** 1. Correctly placed /search endpoint, ABOVE /{id} mapping ***

    @GetMapping("/search")
    public ResponseEntity<List<PromptDetailsDto>> searchPrompts(
            @RequestParam(value = "query") String query,
            Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);

        // Perform search
        List<com.nikhil.promptbridge.model.Prompt> prompts = promptService.searchUserPrompts(currentUser, query);

        // Convert entities to DTOs
        List<PromptDetailsDto> dtos = prompts.stream()
                .map(promptService::convertToDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // *** 2. Create prompt endpoint ***

    @PostMapping
    public ResponseEntity<PromptDetailsDto> createPrompt(@RequestBody CreatePromptRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);

        // Null-safe tags handling
        Set<String> tags = request.tags != null ? request.tags : Set.of();

        // Create prompt
        var newPrompt = promptService.createPrompt(
                request.title,
                request.description,
                request.content,
                currentUser.getId(),
                tags
        );

        // Fetch full DTO after save
        PromptDetailsDto dto = promptService.getPromptDtoById(newPrompt.getId());
        return ResponseEntity.ok(dto);
    }

    // *** 3. Get prompt by ID ***

    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailsDto> getPromptById(@PathVariable Long id) {
        PromptDetailsDto dto = promptService.getPromptDtoById(id);
        return ResponseEntity.ok(dto);
    }

    // *** 4. Get current user's prompts ***

    @GetMapping("/my-prompts")
    public ResponseEntity<List<PromptDetailsDto>> getMyPrompts(Authentication authentication) {
        String userEmail = authentication.getName();
        List<PromptDetailsDto> dtos = promptService.getPromptDtosForUser(userEmail);
        return ResponseEntity.ok(dtos);
    }

    // *** 5. Add version to prompt ***

    @PostMapping("/{promptId}/versions")
    public ResponseEntity<PromptDetailsDto> addVersion(@PathVariable Long promptId, @RequestBody AddVersionRequest request) {
        var updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);
        PromptDetailsDto dto = promptService.getPromptDtoById(updatedPrompt.getId());
        return ResponseEntity.ok(dto);
    }

    // *** 6. Delete prompt ***

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        promptService.deletePrompt(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // *** 7. Update prompt version ***

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

    // --- Request DTO classes ---

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
}
