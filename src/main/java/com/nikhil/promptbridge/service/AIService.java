package com.nikhil.promptbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AIService {

    @Value("${togetherai.api.key}")
    private String apiKey;

    // A powerful, reliable open-source model available on the free tier
    private final String modelUrl = "https://api.together.xyz/v1/completions";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getEnhancedPrompt(String originalPrompt) throws IOException {
        
        String instruction = "Critique the following user-submitted prompt and suggest a more effective version. "
            + "Provide a brief explanation of why your version is better. "
            + "The user's prompt is: \"" + originalPrompt + "\"";

        // Use ObjectMapper to safely create the JSON payload
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", "mistralai/Mixtral-8x7B-Instruct-v0.1");
        payload.put("prompt", instruction);
        payload.put("max_tokens", 250); // Limit the response length
        String jsonPayload = objectMapper.writeValueAsString(payload);

        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(modelUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                // Improved error handling to show the actual error from the server
                throw new IOException("Unexpected code " + response + " | Body: " + responseBody);
            }
            
            // Use Jackson ObjectMapper for robust JSON parsing
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                String generatedText = root.get("choices").get(0).get("text").asText();
                return generatedText.trim();
            } else {
                return "Error: Could not parse AI response. Full response: " + responseBody;
            }
        }
    }
}
