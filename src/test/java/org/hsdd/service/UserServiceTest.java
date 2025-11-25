package org.hsdd.service;

import org.hsdd.domain.Patient;
import org.hsdd.domain.User;
import org.hsdd.dto.CreateUserRequest;
import org.hsdd.dto.UpdateUserRequest;
import org.hsdd.repo.PatientRepository;
import org.hsdd.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository users;
    @Mock private PatientRepository patients;
    @Mock private PasswordEncoder encoder;
    @Mock private AuditService audit;

    @InjectMocks
    private UserService service;

    // -------------------------------------------------------------------------
    // 1. registerUser() — creates user + patient correctly
    // -------------------------------------------------------------------------
    @Test
    void registerUser_createsUserAndPatientCorrectly() {

        CreateUserRequest req = new CreateUserRequest(
                "jess",
                "jess@example.com",
                "pw123",
                "patient",
                "Jessica",
                "Morcos",
                "2002-01-10",
                "1234567890"
        );

        when(users.existsByUsername("jess")).thenReturn(false);
        when(users.existsByEmail("jess@example.com")).thenReturn(false);
        when(encoder.encode("pw123")).thenReturn("ENCODED");

        // simulate first generated ID is unused
        when(patients.existsByPatientId(any())).thenReturn(false);

        // mock save(User)
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        }).when(users).save(any(User.class));

        // mock save(Patient)
        doAnswer(inv -> {
            Patient p = inv.getArgument(0);
            p.setId(100L);
            return p;
        }).when(patients).save(any(Patient.class));

        var dto = service.registerUser(req, "adminUser");

        assertEquals(10L, dto.userId());
        assertEquals("jess", dto.username());
        assertEquals("jess@example.com", dto.email());
        assertEquals("patient", dto.role());
        assertNotNull(dto.patientId());
        assertEquals("Jessica", dto.firstName());
        assertEquals("Morcos", dto.lastName());
        assertEquals(LocalDate.parse("2002-01-10"), dto.dateOfBirth());
        assertEquals("1234567890", dto.phone());

        verify(audit).log("adminUser", "ADMIN_CREATE_USER", "userId=10, role=patient");
    }

    // -------------------------------------------------------------------------
    // 2. updateUser() — updates user + patient + logs audit
    // -------------------------------------------------------------------------
    @Test
    void updateUser_updatesUserAndPatientCorrectly() {

        User existingUser = new User();
        existingUser.setId(5L);
        existingUser.setUsername("oldUser");
        existingUser.setEmail("old@test.com");
        existingUser.setRole("patient");
        existingUser.setActive(true);

        Patient patient = new Patient();
        patient.setId(77L);
        patient.setUser(existingUser);
        patient.setFirstName("OldF");
        patient.setLastName("OldL");

        when(users.findById(5L)).thenReturn(Optional.of(existingUser));
        when(patients.findByUser(existingUser)).thenReturn(Optional.of(patient));
        when(encoder.encode("newPw")).thenReturn("ENC_NEW");

        UpdateUserRequest req = new UpdateUserRequest(
                "new@test.com",
                "newPw",
                "doctor",
                true,
                "NewF",
                "NewL",
                "2001-06-06",
                "9998887777"
        );

        var dto = service.updateUser(5L, req, "adminActor");

        assertEquals("new@test.com", dto.email());
        assertEquals("doctor", dto.role());
        assertEquals("NewF", dto.firstName());
        assertEquals("NewL", dto.lastName());
        assertEquals(LocalDate.parse("2001-06-06"), dto.dateOfBirth());
        assertEquals("9998887777", dto.phone());

        verify(users).save(existingUser);
        verify(patients).save(patient);
        verify(audit).log("adminActor", "ADMIN_UPDATE_USER", "userId=5");
    }

    // -------------------------------------------------------------------------
    // 3. reactivateUser() — reactivates + logs + returns DTO
    // -------------------------------------------------------------------------
    @Test
    void reactivateUser_reactivatesAndLogsAudit() {

        User u = new User();
        u.setId(22L);
        u.setUsername("test");
        u.setRole("doctor");
        u.setActive(false);
        u.setCreatedAt(Instant.now());

        Patient patient = new Patient();
        patient.setUser(u);
        patient.setPatientId("00000099");

        when(users.findById(22L)).thenReturn(Optional.of(u));
        when(patients.findByUser(u)).thenReturn(Optional.of(patient));

        var dto = service.reactivateUser(22L, "adminA");

        assertTrue(dto.active());
        assertEquals("00000099", dto.patientId());

        verify(users).save(u);
        verify(audit).log("adminA", "ADMIN_REACTIVATE_USER", "userId=22");
    }

}
