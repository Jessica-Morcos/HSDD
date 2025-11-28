package org.hsdd.service;

import org.hsdd.value.Prediction;
import org.hsdd.value.SymptomEntry;

public interface PredictionService {
    // Infer (via stub) and persist, then return the saved Prediction entity
    Prediction inferAndSave(SymptomEntry symptom, String actor, String ip);
}
