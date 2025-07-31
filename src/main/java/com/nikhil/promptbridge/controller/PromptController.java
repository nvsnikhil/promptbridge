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

class CreatePromptRequest {
    public String title;
    public String description;
    public String content;
    public Set<String> tags;
}

class AddVersionRequest {
    public String content;
}

class UpdateVersionRequest {
    public String content;
}

@RestController
@RequestMapping("/prompts")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping
    public ResponseEntity<PromptDetailsDto> createPrompt(@RequestBody CreatePromptRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        
        Prompt newPrompt = promptService.createPrompt(
            request.title,
            request.description,
            request.content,
            currentUser.getId(),
            request.tags
        );

        // Use the new method to get a fully loaded prompt
        Prompt savedPrompt = promptService.getPromptByIdWithDetails(newPrompt.getId());
        
        PromptDetailsDto dto = promptService.convertToDto(savedPrompt);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/my-prompts")
    public ResponseEntity<List<PromptDetailsDto>> getMyPrompts(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Prompt> prompts = promptService.getPromptsForUser(userEmail);
        List<PromptDetailsDto> promptDtos = prompts.stream().map(promptService::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(promptDtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PromptDetailsDto>> searchMyPrompts(
            @RequestParam(value = "query", required = false, defaultValue = "") String query, 
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        List<Prompt> prompts;

        if (query == null || query.trim().isEmpty()) {
            prompts = promptService.getPromptsForUser(userEmail);
        } else {
            prompts = promptService.searchUserPrompts(currentUser, query);
        }
        
        List<PromptDetailsDto> promptDtos = prompts.stream().map(promptService::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(promptDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailsDto> getPromptById(@PathVariable Long id) {
        Prompt prompt = promptService.getPromptByIdWithDetails(id); // Use the new method
        PromptDetailsDto dto = promptService.convertToDto(prompt);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{promptId}/versions")
    public ResponseEntity<PromptDetailsDto> addVersion(@PathVariable Long promptId, @RequestBody AddVersionRequest request) {
        Prompt updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);
        
        // Re-fetch for consistency
        Prompt savedPrompt = promptService.getPromptByIdWithDetails(updatedPrompt.getId());

        PromptDetailsDto dto = promptService.convertToDto(savedPrompt);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        promptService.deletePrompt(id, currentUser);
        return ResponseEntity.noContent().build();
    }

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
