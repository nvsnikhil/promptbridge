package com.nikhil.promptbridge.dto;
import java.util.List;
public record PromptDetailsDto(Long id, String title, String description, List<VersionDto> versions) {}