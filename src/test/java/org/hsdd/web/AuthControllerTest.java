package org.hsdd.web;


import org.hsdd.domain.Patient;
import org.hsdd.domain.User;
import org.hsdd.dto.AuthResponse;
import org.hsdd.dto.LoginRequest;
import org.hsdd.dto.SignupRequest;
import org.hsdd.security.TokenAuthFilter;
import org.hsdd.service.AuthService;
import org.hsdd.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenAuthFilter tokenAuthFilter;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    // =========================
    //        SIGNUP TESTS
    // =========================

    @Test
    void signup_returns201AndJson() throws Exception {

        User user = new User();
        user.setId(10L);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setPatientId("PAT-XYZ");

        when(userService.registerPatient(any()))
                .thenReturn(patient);

        String body = """
        {
            "username":"jess",
            "email":"jess@example.com",
            "password":"Strong123!",
            "firstName":"Jess",
            "lastName":"Morcos",
            "dateOfBirth":"2001-05-22",
            "phone":"1234567890"
        }
        """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                    {"userId":10,"patientId":"PAT-XYZ"}
                """));
    }

    @Test
    void signup_passesCorrectSignupRequestToService() throws Exception {

        Patient fake = new Patient();
        fake.setUser(new User());
        fake.setPatientId("XX");

        when(userService.registerPatient(any()))
                .thenReturn(fake);

        String body = """
        {
            "username":"jess",
            "email":"jess@example.com",
            "password":"Strong123!",
            "firstName":"Jess",
            "lastName":"Morcos",
            "dateOfBirth":"2001-05-22",
            "phone":"1234567890"
        }
        """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        ArgumentCaptor<SignupRequest> captor = ArgumentCaptor.forClass(SignupRequest.class);
        verify(userService).registerPatient(captor.capture());

        SignupRequest sent = captor.getValue();

        org.junit.jupiter.api.Assertions.assertEquals("jess", sent.username());
        org.junit.jupiter.api.Assertions.assertEquals("jess@example.com", sent.email());
        org.junit.jupiter.api.Assertions.assertEquals("Jess", sent.firstName());
        org.junit.jupiter.api.Assertions.assertEquals("Morcos", sent.lastName());
        org.junit.jupiter.api.Assertions.assertEquals("2001-05-22", sent.dateOfBirth());
        org.junit.jupiter.api.Assertions.assertEquals("1234567890", sent.phone());
    }

    @Test
    void signup_ifServiceThrows_returns500() throws Exception {

        when(userService.registerPatient(any()))
                .thenThrow(new RuntimeException("fail"));

        String body = """
        {
            "username":"jess",
            "email":"jess@example.com",
            "password":"Strong123!",
            "firstName":"Jess",
            "lastName":"Morcos",
            "dateOfBirth":"2001-05-22",
            "phone":"1234567890"
        }
        """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError());
    }

    // =========================
    //        LOGIN TESTS
    // =========================

    @Test
    void login_returnsAuthResponse() throws Exception {

        AuthResponse res = new AuthResponse(
                "jwt-token",
                "jess",
                "USER",
                10L,
                "PAT-XYZ"
        );

        when(authService.login(any()))
                .thenReturn(res);

        String body = """
        {
            "username":"jess",
            "password":"Strong123!"
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("jess"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.patientId").value("PAT-XYZ"));
    }

    @Test
    void login_passesCorrectDataToService() throws Exception {

        when(authService.login(any()))
                .thenReturn(new AuthResponse(
                        null,
                        null,
                        null,
                        null,
                        null
                ));

        String body = """
        {
            "username":"jess",
            "password":"Strong123!"
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).login(captor.capture());

        LoginRequest sent = captor.getValue();

        org.junit.jupiter.api.Assertions.assertEquals("jess", sent.username());
        org.junit.jupiter.api.Assertions.assertEquals("Strong123!", sent.password());
    }

    @Test
    void login_whenServiceThrows_returns500() throws Exception {

        when(authService.login(any()))
                .thenThrow(new RuntimeException("bad"));

        String body = """
        {
            "username":"jess",
            "password":"wrong"
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError());
    }
}
