package com.nmferrer.sundrop;

public class UserInfo {
    private String UID;
    private String displayName;
    private String email;
    private String seeking;
    private String availability;

    public UserInfo() {
        UID = null;
        displayName = null;
        email = null;
        seeking = null;
    }
    public UserInfo(String UID, String email, String displayName) {
        this.UID = UID;
        this.email = email;
        this.displayName = displayName;
        seeking = null;
        availability = null;
    }
    public UserInfo(String UID, String email, String displayName, String seeking, String availability) {
        this.UID = UID;
        this.email = email;
        this.displayName = displayName;
        this.seeking = seeking;
        this.availability = availability;
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

    public String getSeeking() {
        return seeking;
    }

    public String getAvailability() {
        return availability;
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

    public void setSeeking(String seeking) {
        this.seeking = seeking;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    @Override
    public String toString() {
        return displayName + '\n' +
                "Looking For: " + seeking + '\n' +
                "Available:\n" + availability + '\n';
    }
}
