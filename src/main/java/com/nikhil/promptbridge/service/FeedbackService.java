package com.nikhil.promptbridge.service;

import com.nikhil.promptbridge.model.Feedback;
import com.nikhil.promptbridge.model.PromptVersion;
import com.nikhil.promptbridge.model.User;
import com.nikhil.promptbridge.repository.FeedbackRepository;
import com.nikhil.promptbridge.repository.PromptVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- ADD THIS IMPORT

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private PromptVersionRepository promptVersionRepository;

    @Transactional // <-- ADD THIS ANNOTATION
    public Feedback addFeedback(int rating, String comment, Long versionId, User user) {
        PromptVersion promptVersion = promptVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("PromptVersion not found"));

        Feedback feedback = new Feedback();
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setPromptVersion(promptVersion);
        feedback.setUser(user);

        // This is the crucial new line that updates the parent object
        promptVersion.getFeedback().add(feedback);
        
        System.out.println("Feedback added. New feedback count: " + promptVersion.getFeedback().size());


        return feedbackRepository.save(feedback);
    }
}