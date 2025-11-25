package org.hsdd.service;

import org.hsdd.model.Prediction;
import org.hsdd.model.Symptom;

public interface PredictionService {
    // Infer (via stub) and persist, then return the saved Prediction entity
    Prediction inferAndSave(Symptom symptom, String actor, String ip);
}
