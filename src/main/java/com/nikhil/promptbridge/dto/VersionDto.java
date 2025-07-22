package com.nikhil.promptbridge.dto;

import java.util.List;

public record VersionDto(
    Long id, // <-- Add this ID field
    int versionNumber,
    String content,
    List<FeedbackDto> feedback
) {}