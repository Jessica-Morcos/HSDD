package org.hsdd.model;

public class Admin {

    private final User user;

    public Admin(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Long getId() { return user.getId(); }
    public String getUsername() { return user.getUsername(); }
    public String getEmail() { return user.getEmail(); }
}
