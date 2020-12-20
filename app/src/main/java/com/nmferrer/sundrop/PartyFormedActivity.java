/*
 * Created by Noah Ferrer on 12/18/20 7:34 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 2:54 AM
 *
 */

package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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
    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    //UI
    private TextView textViewPartyTitle;
    private ListView listViewChatLog;
    private Button seeUsersButton;
    private Button inviteUsersButton;
    private Button leavePartyButton;
    private EditText chatMessageEditText;
    private FloatingActionButton sendButton;

    //Pulled Data
    private String currentUID;
    private UserInfo currentUserInfo;
    private String currentPartyID;

    //Listeners
    private FirebaseListAdapter<Message> adapter;
    //Debug
    private final String TAG = "PARTY_CHAT_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_formed);

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        currentUID = mAuth.getCurrentUser().getUid();
        currentPartyID = getIntent().getExtras().getString("partyID");

        databaseRef.child("Users/" + currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserInfo = snapshot.getValue(UserInfo.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //UI Setup
        textViewPartyTitle = findViewById(R.id.textViewPartyTitle);
        listViewChatLog = findViewById(R.id.listViewChatLog);
        seeUsersButton = findViewById(R.id.seeUsersButton);
        inviteUsersButton = findViewById(R.id.inviteMembersButton);
        leavePartyButton = findViewById(R.id.leavePartyButton);
        chatMessageEditText = findViewById(R.id.editTextChatMessage);
        sendButton = findViewById(R.id.sendButton);

        //Listener Setup
        seeUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateUsersListDialog();
            }
        });
        inviteUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inviteUserToExistingParty();
            }
        });
        leavePartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String partyName = currentPartyID.split("_")[0];
                generateLeavePartyDialog(partyName);
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(chatMessageEditText, currentPartyID, currentUID, currentUserInfo.getDisplayName());
            }
        });

        //TODO: WILL PROBABLY BREAK BECAUSE ID NOT SET ON CREATE
        //https://code.tutsplus.com/tutorials/how-to-create-an-android-chat-app-using-firebase--cms-27397

        Query queryCurrentParty = databaseRef.child("Messages").orderByChild("partyID").equalTo(currentPartyID);
        FirebaseListOptions<Message> options = new FirebaseListOptions.Builder<Message>()
                .setQuery(queryCurrentParty, Message.class)
                .setLayout(R.layout.message)
                .build();
        adapter = new FirebaseListAdapter<Message>(options) {
            @Override
            protected void populateView(View v, Message model, int position) {
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageText = (TextView)v.findViewById(R.id.message_text);

                messageUser.setText(model.getMessageSenderDisplayName());
                messageText.setText(model.getMessageText());
            }
        };
        listViewChatLog.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUID = mAuth.getCurrentUser().getUid();
        currentPartyID = getIntent().getExtras().getString("partyID");
        String partyName = currentPartyID.split("_")[0];
        textViewPartyTitle.setText(partyName);
        adapter.startListening();

        databaseRef.child("Users/" + currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserInfo = snapshot.getValue(UserInfo.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void sendMessage(EditText editTextChatMessage, String partyID, String senderUID, String senderDisplayName) {
        String messageText = editTextChatMessage.getText().toString();
        Message chatMessage = new Message(messageText, senderUID, senderDisplayName, partyID);

        DatabaseReference messagesRef = databaseRef.child("Messages");
        messagesRef.push().setValue(chatMessage);
        Log.d(TAG, "messageSentSuccess " + chatMessage.getMessageSenderDisplayName() + ": " + chatMessage.getMessageText());
        editTextChatMessage.setText("");
    }

    private void generateUsersListDialog() {

    }
    private void inviteUserToExistingParty() {

    }

    private void generateLeavePartyDialog(final String partyName)  {

        AlertDialog.Builder builder = new AlertDialog.Builder(PartyFormedActivity.this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { //Alert Dialog Confirmed
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                databaseRef.child("Users/" + currentUID + "/inParty/" + currentPartyID).removeValue();
                databaseRef.child("Parties/" + currentPartyID + "/members/" + currentUID).removeValue();
                Toast.makeText(PartyFormedActivity.this, "Leaving party: " + partyName, Toast.LENGTH_SHORT).show();
                launchHome();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { //Alert Dialog Denied
            }
        });

        builder.setMessage("Would you like to leave party: " + partyName + "?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}