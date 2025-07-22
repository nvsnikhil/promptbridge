package com.nikhil.promptbridge.repository;

import com.nikhil.promptbridge.model.PromptVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptVersionRepository extends JpaRepository<PromptVersion, Long> {
}