package org.hsdd.controller;

import org.hsdd.dto.PredictionDto;
import org.hsdd.dto.PredictionWithAnnotationDto;
import org.hsdd.dto.SymptomDto;
import org.hsdd.service.RecordsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class RecordsController {

    private final RecordsService records;

    public RecordsController(RecordsService records) {
        this.records = records;
    }

    @GetMapping("/symptoms")
    public ResponseEntity<List<SymptomDto>> listSymptoms(@RequestParam String patientId) {
        // returns last 200 symptoms for this patientId
        return ResponseEntity.ok(records.listSymptoms(patientId));
    }


    @GetMapping("/predictions")
    public ResponseEntity<List<PredictionWithAnnotationDto>> listPredictions(@RequestParam String patientId) {
        return ResponseEntity.ok(records.listPredictions(patientId));
    }


}