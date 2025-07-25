package com.nikhil.promptbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AIService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    // Switched to a reliable, always-available model on the free tier
    private final String modelUrl = "https://api-inference.huggingface.co/models/distilgpt2";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getEnhancedPrompt(String originalPrompt) throws IOException {
        
        String instruction = "Critique the following user-submitted prompt and suggest a more effective version. "
            + "Provide a brief explanation of why your version is better. "
            + "The user's prompt is: \"" + originalPrompt.replace("\"", "\\\"") + "\"";

        String jsonPayload = "{\"inputs\":\"" + instruction + "\"}";

        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(modelUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + " Body: " + response.body().string());
            }
            
            String responseBody = response.body().string();
            
            // Use Jackson ObjectMapper for robust JSON parsing
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isArray() && root.size() > 0 && root.get(0).has("generated_text")) {
                String generatedText = root.get(0).get("generated_text").asText();
                // Clean up the response by removing the original instruction
                return generatedText.replace(instruction, "").trim();
            } else {
                return "Error: Could not parse AI response. Full response: " + responseBody;
            }
        }
    }
}
