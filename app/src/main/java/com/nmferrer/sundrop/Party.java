/*
 * Created by Noah Ferrer on 12/18/20 9:52 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 9:52 PM
 *
 */

package com.nmferrer.sundrop;

public class Party {
    private String partyName;
    private String timeAndDate;

    public Party() {
    }

    public Party(String partyName, String timeAndDate) {
        this.partyName = partyName;
        this.timeAndDate = timeAndDate;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getTimeAndDate() {
        return timeAndDate;
    }

    public void setTimeAndDate(String timeAndDate) {
        this.timeAndDate = timeAndDate;
    }
}
