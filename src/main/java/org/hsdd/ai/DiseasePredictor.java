package org.hsdd.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hsdd.model.Prediction;
import org.hsdd.model.Symptom;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiseasePredictor {

    private final AiClient aiClient;

    public DiseasePredictor(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    /**
     * Predict using the AI model WITHOUT saving.
     */
    public Prediction predict(Symptom symptom) {

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
