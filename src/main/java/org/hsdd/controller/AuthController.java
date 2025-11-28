package org.hsdd.controller;

import org.hsdd.dto.*;
import org.hsdd.model.Patient;
import org.hsdd.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users;
    private final AuthService auth;

    public AuthController(UserService users, AuthService auth) {
        this.users = users; this.auth = auth;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req){
        Patient p = users.registerPatient(req);
        String body = """
      {"userId":%d,"patientId":"%s"}
      """.formatted(p.getUser().getId(), p.getPatientId());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req){
        return auth.login(req);
    }


    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<?> resetPassword(@PathVariable Long userId,
                                           @RequestBody ResetPasswordRequest req) {
        auth.resetPassword(userId, req.newPassword());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        auth.logout(authHeader);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
    }
}