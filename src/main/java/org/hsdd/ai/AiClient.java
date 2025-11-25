package org.hsdd.ai;

import java.util.List;

public interface AiClient {

    // description = free-text symptoms
    // tags = extra keywords (can be empty list)
    Result analyze(String description, List<String> tags);

    record Result(String label, double confidence) {}
}