package com.nikhil.promptbridge.dto;
public record FeedbackDto(int rating, String comment, UserDto user) {}