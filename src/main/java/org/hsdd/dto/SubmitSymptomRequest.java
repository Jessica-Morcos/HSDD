package org.hsdd.dto;

import java.util.List;

public record SubmitSymptomRequest(String patientId, String text, List<String> tags) {}