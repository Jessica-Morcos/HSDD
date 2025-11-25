package org.hsdd.service;

import org.hsdd.domain.Patient;
import org.hsdd.domain.User;
import org.hsdd.dto.LoginRequest;
import org.hsdd.repo.PatientRepository;
import org.hsdd.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PatientRepository patients = mock(PatientRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuditService audit = mock(AuditService.class);

    private final AuthService service = new AuthService(
            users, patients, encoder, audit
    );

    // -----------------------------------------------------------------------
    // 1. SUCCESSFUL LOGIN RETURNS CORRECT TOKEN + USER INFO
    // -----------------------------------------------------------------------
    @Test
    void login_returnsTokenAndUserData() {

        User u = new User();
        u.setId(55L);
        u.setUsername("jess");
        u.setRole("doctor");

        when(users.findByUsername("jess"))
                .thenReturn(Optional.of(u));

        var res = service.login(new LoginRequest("jess", "pass"));

        assertEquals("TOKEN-55", res.token());
        assertEquals("jess", res.username());
        assertEquals("doctor", res.role());
        assertEquals(55L, res.userId());
        assertNull(res.patientId()); // doctor should NOT return patientId
    }

    // -----------------------------------------------------------------------
    // 2. LOGIN THROWS IF USER DOES NOT EXIST
    // -----------------------------------------------------------------------
    @Test
    void login_invalidUsernameThrows() {

        when(users.findByUsername("nope"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.login(new LoginRequest("nope", "whatever"))
        );
    }

    // -----------------------------------------------------------------------
    // 3. PATIENT ROLE SHOULD RETURN A PATIENT ID
    // -----------------------------------------------------------------------
    @Test
    void login_patientRoleReturnsPatientId() {

        User u = new User();
        u.setId(22L);
        u.setUsername("patientUser");
        u.setRole("patient");

        Patient p = new Patient();
        p.setPatientId("PAT-777");
        p.setUser(u);

        when(users.findByUsername("patientUser"))
                .thenReturn(Optional.of(u));

        when(patients.findByUser(u))
                .thenReturn(Optional.of(p));

        var res = service.login(new LoginRequest("patientUser", "pw"));

        assertEquals("TOKEN-22", res.token());
        assertEquals("patientUser", res.username());
        assertEquals("patient", res.role());
        assertEquals(22L, res.userId());
        assertEquals("PAT-777", res.patientId());
    }
}
