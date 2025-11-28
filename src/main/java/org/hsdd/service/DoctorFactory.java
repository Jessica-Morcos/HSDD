package org.hsdd.service;

import org.hsdd.model.User;
import org.hsdd.model.Doctor;
import org.hsdd.repo.UserRepository;

import org.hsdd.service.RecordsService;

import org.springframework.stereotype.Component;

@Component
public class DoctorFactory {

    private final UserRepository users;
    private final DoctorService doctorService;
    private final RecordsService recordsService;

    public DoctorFactory(UserRepository users,
                         DoctorService doctorService,
                         RecordsService recordsService) {
        this.users = users;
        this.doctorService = doctorService;
        this.recordsService = recordsService;
    }


    public Doctor fromUser(User user) {
        return new Doctor(user, doctorService, recordsService);
    }


    public Doctor fromUsername(String username) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return fromUser(user);
    }
}
