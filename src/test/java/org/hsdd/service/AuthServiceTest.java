package org.hsdd.service;

import org.hsdd.model.Patient;
import org.hsdd.model.User;
import org.hsdd.dto.LoginRequest;
import org.hsdd.repo.PatientRepository;
import org.hsdd.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>These tests verify that the authentication service correctly returns token and user
 * information for successful logins, properly throws on invalid credentials, and returns
 * patient-specific data when the authenticated user is a patient.</p>
 */
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
        u.setPasswordHash("hashedPass");

        when(users.findByUsername("jess"))
                .thenReturn(Optional.of(u));
        // stub password verification to succeed
        when(encoder.matches(eq("pass"), eq("hashedPass"))).thenReturn(true);

        var res = service.login(new LoginRequest("jess", "pass"));

        assertEquals("TOKEN-55", res.token());
        assertEquals("jess", res.username());
        assertEquals("doctor", res.role());
        assertEquals(55L, res.userId());
        assertNull(res.patientId()); // doctor should NOT return patientId
    }

    // -----------------------------------------------------------------------
    // 2. LOGIN THROWS IF USER DOES NOT EXIST OR PASSWORD INCORRECT
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

    @Test
    void login_incorrectPasswordThrows() {
        User u = new User();
        u.setId(1L);
        u.setUsername("jane");
        u.setRole("doctor");
        u.setPasswordHash("hashed");

        when(users.findByUsername("jane"))
                .thenReturn(Optional.of(u));
        // stub password verification to fail
        when(encoder.matches(eq("wrong"), eq("hashed"))).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.login(new LoginRequest("jane", "wrong"))
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
        u.setPasswordHash("hashedPw");

        Patient p = new Patient();
        p.setPatientId("PAT-777");
        p.setUser(u);

        when(users.findByUsername("patientUser"))
                .thenReturn(Optional.of(u));

        when(encoder.matches(eq("pw"), eq("hashedPw"))).thenReturn(true);
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