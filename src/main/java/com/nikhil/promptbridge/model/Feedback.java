package com.nikhil.promptbridge.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;
    private String comment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_version_id")
    private PromptVersion promptVersion;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    // --- Getters and Setters ---
    
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public int getRating() { return rating; }
	public void setRating(int rating) { this.rating = rating; }
	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public PromptVersion getPromptVersion() { return promptVersion; }
	public void setPromptVersion(PromptVersion promptVersion) { this.promptVersion = promptVersion; }
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
}