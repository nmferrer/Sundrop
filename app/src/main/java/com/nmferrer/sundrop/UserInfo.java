package com.nmferrer.sundrop;

import java.util.List;
import java.util.Objects;

public class UserInfo {
    private String UID;
    private String displayName;
    private String email;
    private String seeking;
    private String availability;
    private boolean isOnline;

    private List<String> sentInvites;
    private List<String> receivedInvites;
    private List<String> formedParty;

    //FULL STRUCT
    public UserInfo(String UID, String displayName, String email, String seeking, String availability, boolean isOnline, List<String> sentInvites, List<String> receivedInvites, List<String> formedParty) {
        this.UID = UID;
        this.displayName = displayName;
        this.email = email;
        this.seeking = seeking;
        this.availability = availability;
        this.isOnline = isOnline;
        this.sentInvites = sentInvites;
        this.receivedInvites = receivedInvites;
        this.formedParty = formedParty;
    }

    public UserInfo() {
        this.UID = null;
        this.displayName = null;
        this.email = null;
        this.seeking = null;
        this.availability = null;
        this.isOnline = false;
    }

    public UserInfo(String UID, String email, String displayName) {
        this.UID = UID;
        this.email = email;
        this.displayName = displayName;
        this.seeking = null;
        this.availability = null;
        this.isOnline = false;
    }

    public UserInfo(String UID, String displayName, String email, String seeking, String availability, boolean isOnline) {
        this.UID = UID;
        this.displayName = displayName;
        this.email = email;
        this.seeking = seeking;
        this.availability = availability;
        this.isOnline = isOnline;
        //NEED TO KEEP LISTS?
    }


    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSeeking() {
        return seeking;
    }

    public void setSeeking(String seeking) {
        this.seeking = seeking;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public List<String> getSentInvites() {
        return sentInvites;
    }

    public void setSentInvites(List<String> sentInvites) {
        this.sentInvites = sentInvites;
    }

    public List<String> getReceivedInvites() {
        return receivedInvites;
    }

    public void setReceivedInvites(List<String> receivedInvites) {
        this.receivedInvites = receivedInvites;
    }

    public List<String> getFormedParty() {
        return formedParty;
    }

    public void setFormedParty(List<String> formedParty) {
        this.formedParty = formedParty;
    }

    @Override
    public String toString() {
        return displayName + '\n' +
                "Looking For: " + seeking + '\n' +
                "Available:\n" + availability + '\n';
    }
}
