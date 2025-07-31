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

        Prompt savedPrompt = promptService.getPromptByIdWithDetails(newPrompt.getId());
        
        // --- DETAILED LOGGING FOR DEBUGGING ---
        System.out.println("--- DEBUGGING CREATED PROMPT ---");
        System.out.println("Saved Prompt ID: " + savedPrompt.getId());
        System.out.println("Saved Prompt Title: " + savedPrompt.getTitle());
        System.out.println("Saved Prompt Description: " + savedPrompt.getDescription());
        System.out.println("Saved Prompt Tags: " + savedPrompt.getTags().stream().map(t -> t.getName()).collect(Collectors.joining(", ")));
        System.out.println("Saved Prompt Versions Count: " + savedPrompt.getVersions().size());
        System.out.println("---------------------------------");
        
        PromptDetailsDto dto = promptService.convertToDto(savedPrompt);
        
        // --- DETAILED LOGGING FOR DTO ---
        System.out.println("--- DEBUGGING DTO ---");
        System.out.println("DTO Title: " + dto.title());
        System.out.println("DTO Description: " + dto.description());
        System.out.println("DTO Tags Count: " + dto.tags().size());
        System.out.println("DTO Versions Count: " + dto.versions().size());
        System.out.println("----------------------");

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
        Prompt prompt = promptService.getPromptByIdWithDetails(id);
        PromptDetailsDto dto = promptService.convertToDto(prompt);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{promptId}/versions")
    public ResponseEntity<PromptDetailsDto> addVersion(@PathVariable Long promptId, @RequestBody AddVersionRequest request) {
        Prompt updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);
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
