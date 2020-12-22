/*
 * Created by Noah Ferrer on 12/18/20 7:35 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 6:57 PM
 *
 */

package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//TODO: CALL TO USERINFO IS ASYNCHRONOUS, I.E. NOT GUARANTEED UNLESS ACTED IN LISTENER
//TODO: TRACK INVITES AWAY FROM USER INFO

public class ViewInvitations extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String currentUID;

    //UI
    private ListView lvSent, lvRecv;
    private Button homeButton;
    private ArrayList<String> listSent = new ArrayList<>();
    private ArrayList<String> listRecv = new ArrayList<>();
    ArrayAdapter<String> adapterSent;
    ArrayAdapter<String> adapterRecv;

    //Pulled Data

    //Listeners
    private ValueEventListener profileListener;
    private HashMap<Query, ValueEventListener> mQueryValListenerMap;
    private HashMap<Query, ChildEventListener> mQueryChildListenerMap;
    private HashMap<DatabaseReference, ValueEventListener> mDatabaseRefValEventListenerMap;
    private HashMap<DatabaseReference, ChildEventListener> mDatabaseRefChildEventListenerMap;

    private HashMap<String, String> displayNameUIDMap;
    //Debug
    private final String TAG = "VIEW_INVITATIONS_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invitations);

        //transparent notification bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        currentUID = mAuth.getCurrentUser().getUid();

        //Listener Setup
        mQueryValListenerMap = new HashMap<>();
        mQueryChildListenerMap = new HashMap<>();
        mDatabaseRefValEventListenerMap = new HashMap<>();
        mDatabaseRefChildEventListenerMap = new HashMap<>();

        displayNameUIDMap = new HashMap<>();

        //UI Setup
        lvSent = findViewById(R.id.listViewSent);
        lvRecv = findViewById(R.id.listViewReceived);
        homeButton = findViewById(R.id.homeButton);
        lvSent.setClickable(true);
        lvRecv.setClickable(true);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchHome();
            }
        });

        //Adapter setup
        adapterSent = new ArrayAdapter<String>(this,
                R.layout.list_white_text,
                listSent);
        lvSent.setAdapter(adapterSent);

        adapterRecv = new ArrayAdapter<String>(this,
                R.layout.list_white_text,
                listRecv);
        lvRecv.setAdapter(adapterRecv);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUID = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "UID:" + currentUID);
        listSent.clear(); //listSent must update on activity start
        listRecv.clear(); //listRecv must update on activity start
        displayNameUIDMap = new HashMap<>();

        Query sentInvitesQuery =
                databaseRef.child("Invites").orderByChild("senderUID").equalTo(currentUID);
        Query recvInvitesQuery =
                databaseRef.child("Invites").orderByChild("recipientUID").equalTo(currentUID);

        Log.d(TAG, "sentReferenceInitialized");

        //TODO: IMPLEMENT CANCEL INVITE onClick
        //ListView Fills: Sent Invites
        ChildEventListener sentInvitesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Invite invite = snapshot.getValue(Invite.class);
                listSent.add(invite.toString());
                adapterSent.notifyDataSetChanged();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Invite invite = snapshot.getValue(Invite.class);
                listSent.remove(invite.toString());
                adapterSent.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        sentInvitesQuery.addChildEventListener(sentInvitesListener);

        //ListView Fills: Received Invites
        ChildEventListener recvInvitesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Invite invite = snapshot.getValue(Invite.class);
                    listRecv.add(invite.toString());
                    Log.d(TAG, "addingMapping:" + invite.getSenderDisplayName() + ":" + invite.getSenderUID());
                    displayNameUIDMap.put(invite.getSenderDisplayName(), invite.getSenderUID());
                    adapterRecv.notifyDataSetChanged();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Invite invite = snapshot.getValue(Invite.class);
                listRecv.remove(invite.toString());
                adapterRecv.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        recvInvitesQuery.addChildEventListener(recvInvitesListener);

        mQueryChildListenerMap.put(sentInvitesQuery, sentInvitesListener);
        mQueryChildListenerMap.put(recvInvitesQuery, recvInvitesListener);

        lvRecv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String pressedInvite = ((TextView) view).getText().toString();
                Log.d(TAG, "clickedListView:\n" + pressedInvite);
                //BUILD KEY VIA PARSING
                //TODO: QUERY BY partyName_senderUID_recipientUID
                String[] inputLines = pressedInvite.split("\n");
                String[] sender_recipient = inputLines[0].split(" -> "); // GUARANTEED FORMAT sender -> recipient
                String partyName = inputLines[1];
                String timeAndDate = inputLines[2];
                String senderDisplayName = sender_recipient[0];
                String senderUID = displayNameUIDMap.get(senderDisplayName);
                Log.d(TAG, "clickedInviteFrom:" + senderDisplayName);

                final String inviteKey = partyName + "_" + senderUID + "_" + currentUID;
                Log.d(TAG, "generatingKey:" + inviteKey);

                //ON RECEIVED LIST FILLS, MAP SENDER DISPLAY NAME TO UID FOR EASY AND EFFICIENT LOOKUP

                DatabaseReference receivedInviteRef = databaseRef.child("Invites/" + inviteKey);
                Log.d(TAG, "refAccessSuccess:" + inviteKey);
                receivedInviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "refHasChildren:" + snapshot.hasChildren());
                        Invite queriedInvite = snapshot.getValue(Invite.class);
                        Log.d(TAG, "refAccessToString:" + queriedInvite.toString());
                        String party_sender_recipient = queriedInvite.getPartyName() + "_" + queriedInvite.getSender_recipient();
                        Log.d(TAG, "PSR:" + party_sender_recipient);
                        generateAlertDialogPartyCreation(
                                queriedInvite.getTime(), queriedInvite.getDate(),
                                queriedInvite.getSenderUID(), queriedInvite.getRecipientUID(),
                                queriedInvite.getSenderDisplayName(), queriedInvite.getRecipientDisplayName(),
                                party_sender_recipient);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        for (Map.Entry<Query, ValueEventListener> entry: mQueryValListenerMap.entrySet()) {
            Query query = entry.getKey();
            ValueEventListener listener = entry.getValue();
            query.removeEventListener(listener);
        }
        for (Map.Entry<Query, ChildEventListener> entry: mQueryChildListenerMap.entrySet()) {
            Query query = entry.getKey();
            ChildEventListener listener = entry.getValue();
            query.removeEventListener(listener);
        }
        for (Map.Entry<DatabaseReference, ValueEventListener> entry: mDatabaseRefValEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        for (Map.Entry<DatabaseReference, ChildEventListener> entry: mDatabaseRefChildEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
    }

    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void generateAlertDialogPartyCreation(final String time, final String date,
                                                  final String senderUID, final String recipientUID,
                                                  final String senderDisplayName, final String recipientDisplayName,
                                                  final String party_sender_recipient) {

        Log.d(TAG, time + "\n" + date + "\n" + senderUID + "\n" + recipientUID + "\n" + senderDisplayName + "\n" + recipientDisplayName);
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewInvitations.this);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { //Delete invitation and create party entry
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference queriedInvite = databaseRef.child("Invites/" + party_sender_recipient);
                ValueEventListener queriedInviteListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Invite inviteInfo = snapshot.getValue(Invite.class);
                        String inviteInfoString = inviteInfo.toString();

                        //construct party key from partyName_senderUID
                        String partyName = inviteInfo.getPartyName();
                        String senderUID = inviteInfo.getSenderUID();
                        String partyUID  = inviteInfo.getPartyUID() ;

                        //create party entry RECIPIENT ONLY. PARTY INITIALIZED ON CREATION, i.e. NEW INVITE
                        DatabaseReference acceptedInviteRef = databaseRef.child("Parties").child(partyUID);
                        acceptedInviteRef.child("members").child(recipientUID).setValue(true);

                        //update user membership RECIPIENT ONLY. SENDER BECOMES MEMBER ON CREATION
                        databaseRef.child("Users/" + recipientUID + "/inParty/" + partyUID).setValue(true);

                        //REMOVE INVITE toString FROM listRecv
                        listRecv.remove(inviteInfoString);
                        adapterRecv.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                queriedInvite.addListenerForSingleValueEvent(queriedInviteListener);
                databaseRef.child("Invites").child(party_sender_recipient).removeValue();

                //notify party creation
                Toast.makeText(ViewInvitations.this,
                        "Party Created!",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { //Delete invitation only
                DatabaseReference senderRef = databaseRef.child("Users").child(senderUID);
                DatabaseReference recipientRef = databaseRef.child("Users").child(recipientUID);
                senderRef.child("sentInviteTo").child(recipientUID).removeValue();
                recipientRef.child("receivedInviteFrom").child(senderUID).removeValue();

                //notify party creation
                Toast.makeText(ViewInvitations.this,
                        "Invite deleted.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        String timeAndDate = String.format("%s %s", time, date);
        builder.setMessage("Invitation received for party at: " + timeAndDate)
                .setTitle("Form Party?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}