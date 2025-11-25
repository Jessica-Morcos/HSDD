package org.hsdd.service;

import org.hsdd.domain.*;
import org.hsdd.dto.SignupRequest;
import org.hsdd.repo.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hsdd.dto.AdminUserDto;
import org.hsdd.dto.CreateUserRequest;
import java.time.Instant;
import org.hsdd.dto.UpdateUserRequest;
import java.util.List;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserService {
    private final UserRepository users;
    private final PatientRepository patients;
    private final PasswordEncoder encoder;
    private final AuditService audit;              // <-- NEW

    public UserService(UserRepository u, PatientRepository p,
                       PasswordEncoder e, AuditService audit) {  // <-- NEW
        this.users = u;
        this.patients = p;
        this.encoder = e;
        this.audit = audit;
    }

    @Transactional
    public Patient registerPatient(SignupRequest req) {
        if (users.existsByUsername(req.username())) throw new IllegalArgumentException("Username already exists");
        if (users.existsByEmail(req.email())) throw new IllegalArgumentException("Email already exists");

        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole("patient");          // 🔹 ensure role is set
        users.save(u);

        Patient p = new Patient();
        p.setUser(u);
        p.setPatientId(generateUniquePatientId());
        p.setFirstName(req.firstName());
        p.setLastName(req.lastName());
        if (req.dateOfBirth() != null && !req.dateOfBirth().isBlank()) {
            p.setDateOfBirth(LocalDate.parse(req.dateOfBirth()));
        }
        p.setPhone(req.phone());
        patients.save(p);

        audit.log(req.username(), "SIGNUP_SUCCESS", "patientId=" + p.getPatientId());
        return p;
    }

    @Transactional
    public AdminUserDto registerUser(CreateUserRequest req, String actor) {
        if (users.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (users.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(req.role().toLowerCase());  // "patient", "doctor", "admin"
        u.setCreatedAt(Instant.now());
        u.setActive(true);
        users.save(u);

        Patient p = null;
        if ("patient".equals(u.getRole())) {
            p = new Patient();
            p.setUser(u);
            p.setPatientId(generateUniquePatientId());
            p.setFirstName(req.firstName());
            p.setLastName(req.lastName());
            if (req.dateOfBirth() != null && !req.dateOfBirth().isBlank()) {
                p.setDateOfBirth(LocalDate.parse(req.dateOfBirth()));
            }
            p.setPhone(req.phone());
            patients.save(p);
        }

        audit.log(actor, "ADMIN_CREATE_USER",
                "userId=" + u.getId() + ", role=" + u.getRole());

        return toAdminDto(u, p);
    }

    @Transactional
    public AdminUserDto updateUser(Long userId, UpdateUserRequest req, String actor) {
        User u = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Patient patient = patients.findByUser(u).orElse(null);

        if (req.email() != null) {
            u.setEmail(req.email());
        }
        if (req.password() != null) {
            u.setPasswordHash(encoder.encode(req.password()));
        }
        if (req.role() != null) {
            u.setRole(req.role().toLowerCase());
        }
        if (req.active() != null) {
            u.setActive(req.active());
        }
        users.save(u);

        if (patient != null) {
            if (req.firstName() != null) patient.setFirstName(req.firstName());
            if (req.lastName() != null) patient.setLastName(req.lastName());
            if (req.dateOfBirth() != null && !req.dateOfBirth().isBlank()) {
                patient.setDateOfBirth(LocalDate.parse(req.dateOfBirth()));
            }
            if (req.phone() != null) patient.setPhone(req.phone());
            patients.save(patient);
        }

        audit.log(actor, "ADMIN_UPDATE_USER", "userId=" + userId);
        return toAdminDto(u, patient);
    }

    @Transactional
    public void deleteUser(Long userId, String actor) {
        User u = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        u.setActive(false);
        users.save(u);

        audit.log(actor, "ADMIN_DEACTIVATE_USER", "userId=" + userId);
    }


    @Transactional
    public AdminUserDto reactivateUser(Long userId, String actor) {

        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // If already active, return directly
        if (Boolean.TRUE.equals(user.isActive())) {
            Patient existing = patients.findByUser(user).orElse(null);
            return toAdminDto(user, existing);
        }

        user.setActive(true);
        users.save(user);

        // Log Reactivation (correct 3-arg signature)
        audit.log(actor, "ADMIN_REACTIVATE_USER", "userId=" + userId);

        Patient patient = patients.findByUser(user).orElse(null);
        return toAdminDto(user, patient);
    }



    public List<AdminUserDto> getAllUsers() {
        return users.findAll().stream()
                .map(u -> {
                    Patient p = patients.findByUser(u).orElse(null);
                    return toAdminDto(u, p);
                })
                .toList();
    }

    public AdminUserDto getUser(Long id) {
        User u = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Patient p = patients.findByUser(u).orElse(null);

        return toAdminDto(u, p);
    }


    private String generateUniquePatientId() {
        while (true) {
            String id = String.format("%08d", ThreadLocalRandom.current().nextInt(0, 100_000_000));
            if (!patients.existsByPatientId(id)) return id;
        }
    }
    private AdminUserDto toAdminDto(User user, Patient patient) {
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                patient != null ? patient.getPatientId() : null,
                patient != null ? patient.getFirstName() : null,
                patient != null ? patient.getLastName() : null,
                patient != null ? patient.getDateOfBirth() : null,
                patient != null ? patient.getPhone() : null,
                user.getCreatedAt()
        );
    }

}
