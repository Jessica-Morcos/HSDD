package org.hsdd.service;

import org.hsdd.domain.Patient;
import org.hsdd.domain.User;
import org.hsdd.dto.AuthResponse;
import org.hsdd.dto.LoginRequest;
import org.hsdd.repo.PatientRepository;   // ⭐ ADD THIS
import org.hsdd.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PatientRepository patients;   // ⭐ ADD THIS FIELD
    private final PasswordEncoder encoder;
    private final AuditService audit;

    // ⭐ UPDATED CONSTRUCTOR (add PatientRepository)
    public AuthService(UserRepository users,
                       PatientRepository patients,
                       PasswordEncoder encoder,
                       AuditService audit) {

        this.users = users;
        this.patients = patients;               // ⭐ SAVE IT
        this.encoder = encoder;
        this.audit = audit;
    }

    public AuthResponse login(LoginRequest req) {
        User u = users.findByUsername(req.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/password"));

        String token = "TOKEN-" + u.getId();

        // ⭐ LOOK UP PATIENT RECORD ONLY IF ROLE = patient
        Patient p = null;
        if ("patient".equals(u.getRole())) {
            p = patients.findByUser(u).orElse(null);
        }

        return new AuthResponse(
                token,
                u.getUsername(),
                u.getRole(),
                u.getId(),
                p != null ? p.getPatientId() : null
        );
    }
}
