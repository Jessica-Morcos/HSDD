package org.hsdd.dto;

// Simple aggregate: label + count
public record TrendDataDto(
        String label,
        Long count
) {}
