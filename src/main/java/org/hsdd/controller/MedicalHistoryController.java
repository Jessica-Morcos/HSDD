package org.hsdd.controller;

import org.hsdd.domain.MedicalHistoryEntry;
import org.hsdd.service.MedicalHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class MedicalHistoryController {

    private final MedicalHistoryService historyService;

    public MedicalHistoryController(MedicalHistoryService historyService) {
        this.historyService = historyService;
    }

    // -------------------------------------------------------
    // 1) Upload
    // -------------------------------------------------------
    @PostMapping("/upload")
    public ResponseEntity<?> uploadHistory(
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        if (patientId == null || patientId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing patientId");
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing file");
        }

        try {
            historyService.uploadHistoryFile(patientId, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // 2) List
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> listHistory(
            @RequestParam(value = "patientId", required = false) String patientId
    ) {
        if (patientId == null || patientId.isBlank()) {
            return ResponseEntity.badRequest().body("Missing patientId");
        }

        try {
            List<MedicalHistoryEntry> list = historyService.listHistory(patientId);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Service error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // 3) Add entry
    // -------------------------------------------------------
    @PostMapping("/add")
    public ResponseEntity<?> addHistoryEntry(
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "details", required = false) String details
    ) {
        if (patientId == null || patientId.isBlank()
                || title == null || title.isBlank()
                || details == null || details.isBlank()) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

        try {
            MedicalHistoryEntry entry = new MedicalHistoryEntry();
            entry.setPatientId(patientId);
            entry.setTitle(title);
            entry.setDetails(details);

            MedicalHistoryEntry saved = historyService.save(entry);
            return ResponseEntity.ok(saved);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Save failed: " + e.getMessage());
        }
    }
}
