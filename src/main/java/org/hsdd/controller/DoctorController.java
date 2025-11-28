package org.hsdd.controller;

import org.hsdd.dto.LowConfidenceDto;
import org.hsdd.model.Doctor;
import org.hsdd.service.DoctorFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorFactory doctorFactory;

    public DoctorController(DoctorFactory doctorFactory) {
        this.doctorFactory = doctorFactory;
    }

    // -------------------------------------------------------
    //   LOW CONFIDENCE QUEUE (UML: Doctor → review queue)
    // -------------------------------------------------------

    @GetMapping("/low-confidence")
    public ResponseEntity<List<LowConfidenceDto>> getLowConfidence(Principal principal) {
        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        return ResponseEntity.ok(doctor.getAllLowConfidenceReports());
    }


    @GetMapping("/low-confidence/all")
    public ResponseEntity<List<LowConfidenceDto>> getAllLowConfidence(Principal principal) {

        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        return ResponseEntity.ok(doctor.getAllLowConfidenceReports());
    }

    @GetMapping("/low-confidence/{id}")
    public ResponseEntity<LowConfidenceDto> getSingleLowConfidence(
            @PathVariable Long id,
            Principal principal
    ) {
        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        return ResponseEntity.ok(doctor.getLowConfidenceDetails(id));
    }

    @PutMapping("/low-confidence/{id}/review")
    public ResponseEntity<Void> markLowConfidenceReviewed(
            @PathVariable Long id,
            Principal principal
    ) {
        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        doctor.markPredictionReviewed(id);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/low-confidence/{id}/pending")
    public ResponseEntity<Void> markPending(
            @PathVariable Long id,
            Principal principal
    ) {
        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        doctor.markPredictionPending(id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<LowConfidenceDto>> getFlagged(Principal principal) {
        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        return ResponseEntity.ok(doctor.getLowConfidenceQueue());
    }

}
