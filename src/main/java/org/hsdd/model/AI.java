package org.hsdd.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hsdd.ai.AiClient;
import org.hsdd.value.Prediction;
import org.hsdd.value.SymptomEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AI {

    private final AiClient aiClient;

    public AI(AiClient aiClient) {
        this.aiClient = aiClient;
    }


    public Prediction predict(SymptomEntry symptom) {

        // Extract tags
        List<String> tags = new ArrayList<>();
        try {
            if (symptom.getTags() != null) {
                tags = new ObjectMapper()
                        .readValue(symptom.getTags(), new TypeReference<List<String>>() {});
            }
        } catch (Exception ignored) {}

        // 🔥 AI CALL (Grok) — this stays exactly the same
        AiClient.Result result = aiClient.analyze(
                symptom.getDescription(),
                tags
        );

        // Return prediction object — service layer will save it
        return new Prediction(
                symptom.getPatientId(),
                symptom.getId(),
                result.label(),      // AI label
                result.confidence()  // AI confidence
        );
    }
}
