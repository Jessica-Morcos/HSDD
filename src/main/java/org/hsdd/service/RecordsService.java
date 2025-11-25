package org.hsdd.service;

import org.hsdd.dto.PredictionDto;
import org.hsdd.dto.PredictionWithAnnotationDto;
import org.hsdd.dto.SymptomDto;

import java.util.List;

public interface RecordsService {
    List<SymptomDto> listSymptoms(String patientId);
    List<PredictionWithAnnotationDto> listPredictions(String patientId);
}