package com.nmferrer.sundrop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/*
The main bulk of the application.
Upon confirming and invitation, the user will have access to a party with other users.
Users can:
    View the details regarding the scheduled party
    Chat
    Postpone, cancel, or leave the party
    Invite new members
 */
public class PartyFormedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_formed);
    }
}