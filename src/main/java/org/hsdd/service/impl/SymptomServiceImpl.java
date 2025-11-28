package org.hsdd.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hsdd.model.Patient;
import org.hsdd.dto.PredictionDto;
import org.hsdd.dto.SubmitSymptomRequest;
import org.hsdd.dto.SubmitSymptomResponse;
import org.hsdd.dto.SymptomDto;
import org.hsdd.value.SymptomEntry;
import org.hsdd.repo.PatientRepository;
import org.hsdd.repo.SymptomRepository;
import org.hsdd.service.PredictionService;
import org.hsdd.service.SymptomService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SymptomServiceImpl implements SymptomService {

    private final SymptomRepository symptoms;
    private final PredictionService predictions;
    private final PatientRepository patients;
    private final ObjectMapper mapper = new ObjectMapper();

    public SymptomServiceImpl(SymptomRepository symptoms,
                              PredictionService predictions,
                              PatientRepository patients) {
        this.symptoms = symptoms;
        this.predictions = predictions;
        this.patients = patients;
    }

    @Override
    @Transactional
    public SubmitSymptomResponse submit(SubmitSymptomRequest req, String actor, String ip) {

        // resolve patient by 8-digit business ID
        Patient patient = patients.findByPatientId(req.patientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        String tagsJson = null;
        try {
            if (req.tags() != null) {
                tagsJson = mapper.writeValueAsString(req.tags());
            }
        } catch (Exception ignored) {}

        // save symptom (description = text)
        SymptomEntry saved = symptoms.save(
                new SymptomEntry(patient.getPatientId(), req.text(), tagsJson)
        );

        // call AI and save prediction
        var pred = predictions.inferAndSave(saved, actor, ip);

        // build DTOs
        List<String> tags = new ArrayList<>();
        try {
            if (tagsJson != null) {
                tags = mapper.readValue(tagsJson, new TypeReference<List<String>>() {});
            }
        } catch (Exception ignored) {}

        SymptomDto symptomDto = new SymptomDto(
                saved.getId(),
                saved.getDescription(),
                tags,
                saved.getSubmittedAt()
        );
        double conf = pred.getConfidence();

        String level;
        if (conf >= 0.9) level = "high";
        else if (conf >= 0.75) level = "moderate";
        else level = "low";

        PredictionDto predictionDto = new PredictionDto(
                pred.getId(),
                pred.getSymptomId(),
                pred.getLabel(),
                pred.getConfidence(),
                level,
                pred.getCreatedAt()
        );

        return new SubmitSymptomResponse(symptomDto, predictionDto);
    }
}