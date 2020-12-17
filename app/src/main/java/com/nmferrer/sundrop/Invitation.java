package com.nmferrer.sundrop;

public class Invitation {
    private String senderRecipient;
    private String senderOnly;
    private String recipientOnly;

    public Invitation() {
        senderRecipient = null;
        senderOnly = null;
        recipientOnly = null;
    }
    public Invitation(String sender, String recipient) {
        senderRecipient = sender + "_" + recipient;
        senderOnly = sender;
        recipientOnly = recipient;
    }

    public String getSenderRecipient() {
        return senderRecipient;
    }

    public void setSenderRecipient(String senderRecipient) {
        this.senderRecipient = senderRecipient;
    }

    public String getSenderOnly() {
        return senderOnly;
    }

    public void setSenderOnly(String senderOnly) {
        this.senderOnly = senderOnly;
    }

    public String getRecipientOnly() {
        return recipientOnly;
    }

    public void setRecipientOnly(String recipientOnly) {
        this.recipientOnly = recipientOnly;
    }

    @Override
    public String toString() {
        return senderOnly + " ->\n" + recipientOnly + "\n@ TIME O'CLOCK";
    }
}
