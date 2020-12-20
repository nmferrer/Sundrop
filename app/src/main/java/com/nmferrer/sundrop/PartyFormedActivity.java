/*
 * Created by Noah Ferrer on 12/18/20 7:34 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 2:54 AM
 *
 */

package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/*
The main bulk of the application.
Upon confirming and invitation, the user will have access to a party with other users.
Users can:
    View the details regarding the scheduled party
    Chat
    Postpone, cancel, or leave the party
    com.nmferrer.sundrop.Invite new members
 */
public class PartyFormedActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    //UI
    private TextView textViewPartyTitle;
    private ListView listViewChatLog;
    private Button seeUsersButton;
    private Button inviteUsersButton;
    private Button leavePartyButton;
    private EditText chatMessageEditText;
    private Button sendButton;

    //Pulled Data
    private UserInfo currentUserInfo;
    private String currentUID;
    private String partyUID;

    //Listeners
    private ChildEventListener partyChatLogListener;
    private HashMap<DatabaseReference, ChildEventListener> mDatabaseRefChildEventListenerMap;
    private HashMap<DatabaseReference, ValueEventListener> mDatabaseRefValEventListenerMap;
    private HashMap<Query, ChildEventListener> mQueryChildListenerMap;
    private HashMap<Query, ValueEventListener> mQueryValListenerMap;

    //Debug
    private final String TAG = "PARTY_ACTIVITY_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_formed);

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUID = currentUser.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        //Listener Setup
        mDatabaseRefChildEventListenerMap = new HashMap<>();
        mDatabaseRefValEventListenerMap = new HashMap<>();
        mQueryChildListenerMap = new HashMap<>();
        mQueryValListenerMap = new HashMap<>();

        //UI Setup
        textViewPartyTitle = findViewById(R.id.textViewPartyTitle);
        listViewChatLog = findViewById(R.id.listViewChatLog);
        seeUsersButton = findViewById(R.id.seeUsersButton);
        inviteUsersButton = findViewById(R.id.inviteMembersButton);
        leavePartyButton = findViewById(R.id.leavePartyButton);
        chatMessageEditText = findViewById(R.id.editTextChatMessage);
        sendButton = findViewById(R.id.sendMessageButton);

        //Listener Setup
        seeUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        inviteUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        leavePartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = sendButton.getText().toString();
                sendMessage(partyUID, currentUID, message);
            }
        });
        //Adapter Setup

        //ListView Setup: On Click Query?
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void sendMessage(String partyUID, String senderUID, String message) {
        DatabaseReference senderRef = databaseRef.child("Users").child(senderUID);
        DatabaseReference partyRef = databaseRef.child("Parties").child(partyUID);
        senderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInfo senderUserInfo = snapshot.getValue(UserInfo.class);
                assert senderUserInfo != null; //THIS ACTIVITY CAN ONLY EXECUTE WHEN USER VALID
                String senderDisplayName = senderUserInfo.getDisplayName();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}