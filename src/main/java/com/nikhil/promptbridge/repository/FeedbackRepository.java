package com.nikhil.promptbridge.repository;

import com.nikhil.promptbridge.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}