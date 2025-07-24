package com.nikhil.promptbridge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AIService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    // A popular and powerful open-source model for text generation
    private final String modelUrl = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2";

    public String getEnhancedPrompt(String originalPrompt) throws IOException, InterruptedException {
        
        String instruction = "Critique the following user-submitted prompt and suggest a more effective version. "
            + "Provide a brief explanation of why your version is better. "
            + "The user's prompt is: \"" + originalPrompt + "\"";

        // Create a JSON payload for the API request
        String jsonPayload = "{\"inputs\":\"" + instruction.replace("\"", "\\\"") + "\"}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(modelUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Basic parsing to extract the generated text from the JSON response
        String responseBody = response.body();
        if (responseBody.contains("\"generated_text\":\"")) {
             return responseBody.split("\"generated_text\":\"")[1].split("\"")[0];
        } else {
            return "Error: Could not parse AI response. Full response: " + responseBody;
        }
    }
}