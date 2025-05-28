package com.vpnservice.dto;

public class UserResponse {
    private String email;
    private Long id;


    // --- Constructors ---
    public UserResponse() {}

    public UserResponse(String email, Long id) {
        this.email = email;
        this.id = id;
    }

    // --- Getters and Setters ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
