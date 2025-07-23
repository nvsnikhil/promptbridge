package com.nikhil.promptbridge.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AIService {

    // This VertexAI object is automatically configured by Spring Boot
    private final VertexAI vertexAI;

    public AIService(VertexAI vertexAI) {
        this.vertexAI = vertexAI;
    }

    public String getEnhancedPrompt(String originalPrompt) throws IOException {
        String modelName = "gemini-1.5-flash-001";

        GenerativeModel model = new GenerativeModel(modelName, this.vertexAI);

        String instruction = "Critique the following user-submitted prompt and suggest a more effective version. "
            + "Provide a brief explanation of why your version is better. "
            + "The user's prompt is: \"" + originalPrompt + "\"";

        GenerateContentResponse response = model.generateContent(instruction);
        return ResponseHandler.getText(response);
    }
}