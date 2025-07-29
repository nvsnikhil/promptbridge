package com.nikhil.promptbridge.dto;

import java.util.List;
import java.util.Set; // Import Set

public record PromptDetailsDto(
    Long id,
    String title,
    String description,
    List<VersionDto> versions,
    Set<TagDto> tags // This is the new field
) {}