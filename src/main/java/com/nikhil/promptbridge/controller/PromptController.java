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

// Updated to include a set of tag names
class CreatePromptRequest {
    public String title;
    public String description;
    public String content;
    public Set<String> tags; // New field for tags
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

    // Updated to handle tags
    @PostMapping
    public ResponseEntity<PromptDetailsDto> createPrompt(@RequestBody CreatePromptRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        Prompt newPrompt = promptService.createPrompt(
            request.title,
            request.description,
            request.content,
            currentUser.getId(),
            request.tags // Pass the new tags to the service
        );
        return ResponseEntity.ok(promptService.convertToDto(newPrompt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailsDto> getPromptById(@PathVariable Long id) {
        Prompt prompt = promptService.getPromptById(id);
        return ResponseEntity.ok(promptService.convertToDto(prompt));
    }
    
    @GetMapping("/my-prompts")
    public ResponseEntity<List<PromptDetailsDto>> getMyPrompts(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Prompt> prompts = promptService.getPromptsForUser(userEmail);
        List<PromptDetailsDto> promptDtos = prompts.stream().map(promptService::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(promptDtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PromptDetailsDto>> searchMyPrompts(@RequestParam("query") String query, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        List<Prompt> prompts = promptService.searchUserPrompts(currentUser, query);
        List<PromptDetailsDto> promptDtos = prompts.stream().map(promptService::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(promptDtos);
    }

    @PostMapping("/{promptId}/versions")
    public ResponseEntity<PromptDetailsDto> addVersion(@PathVariable Long promptId, @RequestBody AddVersionRequest request) {
        Prompt updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);
        return ResponseEntity.ok(promptService.convertToDto(updatedPrompt));
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
