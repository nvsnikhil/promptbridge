package com.nikhil.promptbridge.controller;

import com.nikhil.promptbridge.dto.PromptDetailsDto;
import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.service.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

class CreatePromptRequest {
    public String title;
    public String description;
    public String content;
}

class AddVersionRequest {
    public String content;
}

@RestController
@RequestMapping("/prompts")
public class PromptController {

    private final PromptService promptService;

    // Using Constructor Injection (Best Practice)
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
            currentUser.getId()
        );
        // Convert to DTO before returning
        PromptDetailsDto dto = promptService.convertToDto(newPrompt);
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
        List<Prompt> prompts = promptService.getPromptsForUser(userEmail);
        
        List<PromptDetailsDto> promptDtos = prompts.stream()
                .map(promptService::convertToDto) // Using method reference for cleaner code
                .collect(Collectors.toList());
            
        return ResponseEntity.ok(promptDtos);
    }

    @PostMapping("/{promptId}/versions")
    public ResponseEntity<PromptDetailsDto> addVersion(@PathVariable Long promptId, @RequestBody AddVersionRequest request) {
        Prompt updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);
        // Convert to DTO before returning
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
}
