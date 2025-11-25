package org.hsdd.service.impl;

import org.hsdd.model.Prediction;
import org.hsdd.model.Symptom;
import org.hsdd.ai.DiseasePredictor;
import org.hsdd.repo.PredictionRepository;
import org.hsdd.service.NotificationService;
import org.hsdd.service.PredictionService;
import org.springframework.stereotype.Service;

@Service
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictions;
    private final NotificationService notificationService;
    private final DiseasePredictor predictor;

    private static final double LOW_CONFIDENCE_THRESHOLD = 0.50;

    public PredictionServiceImpl(
            PredictionRepository predictions,
            NotificationService notificationService,
            DiseasePredictor predictor
    ) {
        this.predictions = predictions;
        this.notificationService = notificationService;
        this.predictor = predictor;
    }

    @Override
    public Prediction inferAndSave(Symptom symptom, String actor, String ip) {

        // 1️⃣ Run Grok AI to generate prediction (DO NOT SAVE here)
        Prediction generated = predictor.predict(symptom);

        // 2️⃣ Save to database (ONLY HERE)
        Prediction saved = predictions.save(generated);

        // 3️⃣ Notify doctor if confidence is low
        notificationService.notifyLowConfidence(saved, LOW_CONFIDENCE_THRESHOLD);

        // 4️⃣ Return final saved prediction
        return saved;
    }
}
