package com.nikhil.promptbridge.controller;

import com.nikhil.promptbridge.model.Feedback;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.service.FeedbackService;
import com.nikhil.promptbridge.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// DTO for submitting feedback
class FeedbackRequest {
    public int rating;
    public String comment;
}

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private PromptService promptService;

    @PostMapping("/{versionId}")
    public ResponseEntity<String> submitFeedback( // <-- Changed return type to String
            @PathVariable Long versionId,
            @RequestBody FeedbackRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        User currentUser = promptService.getCurrentUser(userEmail);

        feedbackService.addFeedback(
                request.rating,
                request.comment,
                versionId,
                currentUser
        );
        return ResponseEntity.ok("Feedback submitted successfully"); // <-- Return simple message
    }
}