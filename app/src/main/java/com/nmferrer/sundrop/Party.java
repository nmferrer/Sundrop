/*
 * Created by Noah Ferrer on 12/18/20 9:52 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 9:52 PM
 *
 */

package com.nmferrer.sundrop;
//TODO: SIGNIFY A "HOST" MEMBER (USER THAT CREATED PARTY)
public class Party {
    private String partyName;
    private String time;
    private String date;

    //STORE INFORMATION TO AID CREATION OF NEW INVITES

    public Party() {
    }

    public Party(String partyName, String time, String date) {
        this.partyName = partyName;
        this.time = time;
        this.date = date;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
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
}
