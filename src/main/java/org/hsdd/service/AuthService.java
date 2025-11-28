package org.hsdd.service;

import org.hsdd.model.Patient;
import org.hsdd.model.User;
import org.hsdd.dto.AuthResponse;
import org.hsdd.dto.LoginRequest;
import org.hsdd.repo.PatientRepository;
import org.hsdd.repo.UserRepository;
import org.hsdd.value.SessionToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class AuthService {

    private final UserRepository users;
    private final PatientRepository patients;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public AuthService(UserRepository users,
                       PatientRepository patients,
                       PasswordEncoder encoder,
                       AuditService audit) {
        this.users = users;
        this.patients = patients;
        this.encoder = encoder;
        this.audit = audit;
    }


    public AuthResponse login(LoginRequest req) {
        System.out.println("---- LOGIN DEBUG START ----");
        System.out.println("username = " + req.username());

        try {
            User u = users.findByUsername(req.username())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username/password"));

            System.out.println("USER FOUND: id=" + u.getId() + " role=" + u.getRole());


            if (!u.isActive()) {
                System.out.println("LOGIN FAILED — USER INACTIVE");
                throw new IllegalArgumentException("Account is inactive. Contact admin.");
            }


            System.out.println("RAW PASSWORD = [" + req.password() + "]");
            System.out.println("DB HASH = [" + u.getPasswordHash() + "]");


// ⭐ Check match
            boolean passwordMatch = encoder.matches(req.password(), u.getPasswordHash());
            System.out.println("PASSWORD MATCH = " + passwordMatch);


            if (!passwordMatch) {
                throw new IllegalArgumentException("Invalid username/password");
            }

            System.out.println("AUDIT LOGGING...");
            audit.log(u.getUsername(), "LOGIN_SUCCESS", "userId=" + u.getId());
            System.out.println("AUDIT DONE!");

            String token = "TOKEN-" + u.getId();
            Patient p = null;
            if ("patient".equals(u.getRole())) {
                p = patients.findByUser(u).orElse(null);
            }

            System.out.println("---- LOGIN DEBUG END ----");
            return new AuthResponse(
                    token,
                    u.getUsername(),
                    u.getRole(),
                    u.getId(),
                    p != null ? p.getPatientId() : null
            );

        } catch (Exception e) {
            System.out.println("---- LOGIN ERROR ----");
            e.printStackTrace();  // ⭐ PRINTS REAL ERROR EVEN IF HANDLER HIDES IT ⭐
            System.out.println("---- END ERROR ----");

            throw e; // rethrow so handler returns 500
        }
    }

    public void resetPassword(Long userId, String newPassword) {
        User u = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // Encode the new password before storing
        u.setPasswordHash(encoder.encode(newPassword));
        users.save(u);
        audit.log(u.getUsername(), "RESET_PASSWORD", "userId=" + userId);
    }

    public void logout(String token) {
        if (token == null || !token.startsWith("Bearer TOKEN-")) {
            return;
        }
        try {
            Long userId = Long.parseLong(token.substring("Bearer TOKEN-".length()));
            User u = users.findById(userId).orElse(null);
            if (u != null) {
                audit.log(u.getUsername(), "LOGOUT", "userId=" + userId);
            }
        } catch (Exception ignored) {
        }
    }
}