package com.nmferrer.sundrop;

public class Invitation {
    private String pairSenderRecipient;
    private UserInfo sender;
    private UserInfo recipient;
    private String day;
    private String time;

    public Invitation() {
        this.pairSenderRecipient = null;
        this.sender = null;
        this.recipient = null;
        this.day = null;
        this.time = null;
    }

    public Invitation(String pairSenderRecipient, UserInfo sender, UserInfo recipient, String day, String time) {
        this.pairSenderRecipient = pairSenderRecipient;
        this.sender = sender;
        this.recipient = recipient;
        this.day = day;
        this.time = time;
    }

    public UserInfo getSender() {
        return sender;
    }

    public void setSender(UserInfo sender) {
        this.sender = sender;
    }

    public UserInfo getRecipient() {
        return recipient;
    }

    public void setRecipient(UserInfo recipient) {
        this.recipient = recipient;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s %s %s", sender, recipient, day, time);
    }
}
