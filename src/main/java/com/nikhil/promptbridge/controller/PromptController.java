package com.nikhil.promptbridge.controller;

import com.nikhil.promptbridge.dto.PromptDetailsDto;
import com.nikhil.promptbridge.model.Prompt;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

// DTO for creating a new prompt
class CreatePromptRequest {
    public String title;
    public String description;
    public String content;
}

// DTO for adding a new version
class AddVersionRequest {
    public String content;
}

@RestController
@RequestMapping("/prompts")
public class PromptController {

    @Autowired
    private PromptService promptService;

    @PostMapping
    public ResponseEntity<Prompt> createPrompt(@RequestBody CreatePromptRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);
        Prompt newPrompt = promptService.createPrompt(request.title, request.description, request.content, currentUser.getId());
        return ResponseEntity.ok(newPrompt);
    }

    // THIS METHOD IS NOW UPDATED TO RETURN A DTO
    @GetMapping("/{id}")
    public ResponseEntity<PromptDetailsDto> getPromptById(@PathVariable Long id) {
        Prompt prompt = promptService.getPromptById(id);
        PromptDetailsDto dto = promptService.convertToDto(prompt);
        return ResponseEntity.ok(dto);
    }
    
    // THIS METHOD IS NOW UPDATED TO RETURN A LIST OF DTOS
    @GetMapping("/my-prompts")
    public ResponseEntity<List<PromptDetailsDto>> getMyPrompts(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Prompt> prompts = promptService.getPromptsForUser(userEmail);
        
        // Convert each prompt to a DTO
        List<PromptDetailsDto> promptDtos = prompts.stream()
                .map(prompt -> promptService.convertToDto(prompt))
                .collect(Collectors.toList());
            
        return ResponseEntity.ok(promptDtos);
    }

    @PostMapping("/{promptId}/versions")
    public ResponseEntity<Prompt> addVersion(@PathVariable Long promptId, @RequestBody AddVersionRequest request) {
        Prompt updatedPrompt = promptService.addVersionToPrompt(promptId, request.content);
        return ResponseEntity.ok(updatedPrompt);
    }
}