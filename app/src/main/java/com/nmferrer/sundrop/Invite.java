/*
 * Created by Noah Ferrer on 12/18/20 11:35 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 11:34 PM
 *
 */

package com.nmferrer.sundrop;

import java.util.Calendar;
import java.util.Date;

public class Invite {
    private String partyUID;
    private String partyName;
    private String senderUID;
    private String senderDisplayName;
    private String recipientUID;
    private String recipientDisplayName;
    private String time;
    private String date;
    private Date timeLogged;

    private String sender_recipient; //COMBINED CHILD TO HELP WITH QUERY

    public Invite() {
    }

    public Invite(String partyUID, String partyName, String senderUID, String senderDisplayName, String recipientUID, String recipientDisplayName, String time, String date) {
        this.partyUID = partyUID;
        this.partyName = partyName;
        this.senderUID = senderUID;
        this.senderDisplayName = senderDisplayName;
        this.recipientUID = recipientUID;
        this.recipientDisplayName = recipientDisplayName;
        this.time = time;
        this.date = date;
        this.timeLogged = Calendar.getInstance().getTime();
        this.sender_recipient = senderUID + "_" + recipientUID;
    }

    public String getPartyUID() {
        return partyUID;
    }

    public void setPartyUID(String partyUID) {
        this.partyUID = partyUID;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getSenderUID() {
        return senderUID;
    }

    public void setSenderUID(String senderUID) {
        this.senderUID = senderUID;
    }

    public String getSenderDisplayName() {
        return senderDisplayName;
    }

    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }

    public String getRecipientUID() {
        return recipientUID;
    }

    public void setRecipientUID(String recipientUID) {
        this.recipientUID = recipientUID;
    }

    public String getRecipientDisplayName() {
        return recipientDisplayName;
    }

    public void setRecipientDisplayName(String recipientDisplayName) {
        this.recipientDisplayName = recipientDisplayName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Date getTimeLogged() {
        return timeLogged;
    }

    public void setTimeLogged(Date timeLogged) {
        this.timeLogged = timeLogged;
    }

    public String getSender_recipient() {
        return sender_recipient;
    }

    public void setSender_recipient(String sender_recipient) {
        this.sender_recipient = sender_recipient;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s\n%s\n%s %s", senderDisplayName, recipientDisplayName, partyName, date, time);
    }
}
