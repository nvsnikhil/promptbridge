package com.nikhil.promptbridge.controller;

import com.nikhil.promptbridge.model.PromptVersion;
import com.nikhil.promptbridge.repository.PromptVersionRepository;
import com.nikhil.promptbridge.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private PromptVersionRepository promptVersionRepository;

    @PostMapping("/enhance/{versionId}")
    public ResponseEntity<String> enhancePrompt(@PathVariable Long versionId) {
        PromptVersion version = promptVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Prompt version not found"));

        try {
            String enhancedContent = aiService.getEnhancedPrompt(version.getContent());
            return ResponseEntity.ok(enhancedContent);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error communicating with AI service.");
        }
    }
}