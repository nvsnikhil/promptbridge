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

        // Re-fetch the prompt to ensure all data is loaded
        Prompt savedPrompt = promptService.getPromptById(newPrompt.getId());
        
        PromptDetailsDto dto = promptService.convertToDto(savedPrompt);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailsDto> getPromptById(@PathVariable Long id) {
        Prompt prompt = promptService.getPromptById(id);
        PromptDetailsDto dto = promptService.convertToDto(prompt);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/my-prompts")
    public ResponseEntity<List<PromptDetailsDto>> getMyPrompts(Authentication authentication) {
        String userEmail = authentication.getName();
        // Updated to use DTO-returning method for eager loading and completeness
        List<PromptDetailsDto> promptDtos = promptService.getPromptDtosForUser(userEmail);
        return ResponseEntity.ok(promptDtos);
    }

    @PostMapping("/{promptId}/versions")
    public ResponseEntity<PromptDetailsDto> addVersion(@PathVariable Long promptId, @RequestBody AddVersionRequest request) {
        Prompt updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);
        PromptDetailsDto dto = promptService.convertToDto(updatedPrompt);
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
