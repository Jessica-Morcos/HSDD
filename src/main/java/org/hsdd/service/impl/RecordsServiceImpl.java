package org.hsdd.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hsdd.dto.PredictionWithAnnotationDto;
import org.hsdd.dto.SymptomDto;
import org.hsdd.repo.PredictionRepository;
import org.hsdd.repo.SymptomRepository;
import org.hsdd.service.RecordsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecordsServiceImpl implements RecordsService {

    private final SymptomRepository symptoms;
    private final PredictionRepository preds;
    private final ObjectMapper mapper = new ObjectMapper();

    public RecordsServiceImpl(SymptomRepository symptoms, PredictionRepository preds) {
        this.symptoms = symptoms;
        this.preds = preds;
    }

    @Override
    public List<SymptomDto> listSymptoms(String patientId) {
        return symptoms.findTop200ByPatientIdOrderBySubmittedAtDesc(patientId)
                .stream()
                .map(s -> {
                    List<String> tags = new ArrayList<>();
                    try {
                        if (s.getTags() != null) {
                            tags = mapper.readValue(s.getTags(), new TypeReference<List<String>>() {});
                        }
                    } catch (Exception ignored) {}

                    return new SymptomDto(
                            s.getId(),
                            s.getDescription(),
                            tags,
                            s.getSubmittedAt()
                    );
                })
                .toList();
    }

    @Override
    public List<PredictionWithAnnotationDto> listPredictions(String patientId) {
        return preds.findTop200ByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(p -> {
                    double conf = p.getConfidence();

                    // NEW CONFIDENCE LEVEL LOGIC (matches screenshot)
                    String level;
                    if (conf >= 0.90) {
                        level = "high";
                    } else if (conf >= 0.75) {
                        level = "moderate";
                    } else {
                        level = "low";
                    }

                    // find MOST RECENT doctor annotation
                    var latestAnn = (p.getAnnotations() == null || p.getAnnotations().isEmpty())
                            ? null
                            : p.getAnnotations()
                            .stream()
                            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                            .findFirst()
                            .orElse(null);

                    String doctorUsername = latestAnn != null ? latestAnn.getDoctor().getUsername() : null;
                    String doctorNotes = latestAnn != null ? latestAnn.getNotes() : null;
                    String correctedLabel = latestAnn != null ? latestAnn.getCorrectedLabel() : null;

                    return new PredictionWithAnnotationDto(
                            p.getId(),
                            p.getSymptomId(),
                            p.getLabel(),
                            p.getConfidence(),
                            level,                 // NEW — confidence label
                            p.getCreatedAt(),
                            doctorUsername,
                            doctorNotes,
                            correctedLabel
                    );
                })
                .toList();
    }

}
