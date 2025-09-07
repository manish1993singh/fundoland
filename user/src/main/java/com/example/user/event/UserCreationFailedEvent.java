package com.example.user.event;

import java.io.Serializable;

import lombok.Data;

public class UserCreationFailedEvent implements Serializable{
    private String attemptedEmail;
    private String reason;

    public UserCreationFailedEvent() {}
    public UserCreationFailedEvent(String attemptedEmail, String reason) {
        this.attemptedEmail = attemptedEmail;
        this.reason = reason;
    }

    public String getAttemptedEmail() {
        return attemptedEmail;
    }

    public void setAttemptedEmail(String attemptedEmail) {
        this.attemptedEmail = attemptedEmail;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}