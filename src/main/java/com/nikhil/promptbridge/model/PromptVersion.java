package com.nikhil.promptbridge.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "prompt_versions")
public class PromptVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TEXT content, no @Lob needed for Postgres TEXT
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "version_number")
    private int versionNumber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    // Changed from List to Set to fix MultipleBagFetchException
    @OneToMany(mappedBy = "promptVersion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Feedback> feedback = new HashSet<>();

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Prompt getPrompt() { return prompt; }
    public void setPrompt(Prompt prompt) { this.prompt = prompt; }

    public Set<Feedback> getFeedback() { return feedback; }
    public void setFeedback(Set<Feedback> feedback) { this.feedback = feedback; }
}
