package com.nmferrer.sundrop;

public class UserInfo {
    private String UID;
    private String displayName;
    private String email;
    private String status;

    public UserInfo() {
        UID = null;
        displayName = null;
        email = null;
        status = null;
    }
    public UserInfo(String UID, String email, String username) {
        this.UID = UID;
        this.email = email;
        this.displayName = username;
        status = null;
    }

    public String getUID() {
        return UID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
