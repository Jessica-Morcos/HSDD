package org.hsdd.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SymptomDto(
        Long id,
        String text,
        List<String> tags,
        LocalDateTime submittedAt
) {}
