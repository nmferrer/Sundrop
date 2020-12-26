/*
 * Created by Noah Ferrer on 12/20/20 2:46 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/20/20 2:46 AM
 *
 */

package com.nmferrer.sundrop;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class Message {
    private String messageText;
    private String messageSenderUID;
    private String messageSenderDisplayName;

    public Message() {
    }

    public Message(String messageText, String messageSenderUID, String messageSenderDisplayName) {
        this.messageText = messageText;
        this.messageSenderUID = messageSenderUID;
        this.messageSenderDisplayName = messageSenderDisplayName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageSenderUID() {
        return messageSenderUID;
    }

    public void setMessageSenderUID(String messageSenderUID) {
        this.messageSenderUID = messageSenderUID;
    }

    public String getMessageSenderDisplayName() {
        return messageSenderDisplayName;
    }

    public void setMessageSenderDisplayName(String messageSenderDisplayName) {
        this.messageSenderDisplayName = messageSenderDisplayName;
    }

}