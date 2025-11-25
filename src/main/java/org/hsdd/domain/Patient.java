package org.hsdd.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name = "patients")
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional=false) @JoinColumn(name="user_id", unique = true)
    private User user;

    @Column(name="patient_id", nullable=false, unique=true, length=8)
    private String patientId; // 8-digit string

    @Column(name="first_name", nullable=false, length=80)
    private String firstName;

    @Column(name="last_name", nullable=false, length=80)
    private String lastName;

    @Column(name="date_of_birth") private LocalDate dateOfBirth;
    @Column(name="phone", length=32) private String phone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
