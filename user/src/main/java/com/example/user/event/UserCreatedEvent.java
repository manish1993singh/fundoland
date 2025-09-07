package com.example.user.event;

import java.io.Serializable;

public class UserCreatedEvent implements Serializable {
    private String name;
    private String email;

    public UserCreatedEvent() {}

    public UserCreatedEvent(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
