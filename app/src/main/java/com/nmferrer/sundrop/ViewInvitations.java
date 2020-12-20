/*
 * Created by Noah Ferrer on 12/18/20 7:35 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 6:57 PM
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
import android.view.View;
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

    //Debug
    private final String TAG = "VIEW_INVITATIONS_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invitations);

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        currentUID = mAuth.getCurrentUser().getUid();

        //Listener Setup
        mQueryValListenerMap = new HashMap<>();
        mQueryChildListenerMap = new HashMap<>();
        mDatabaseRefValEventListenerMap = new HashMap<>();
        mDatabaseRefChildEventListenerMap = new HashMap<>();

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
                android.R.layout.simple_list_item_1,
                listSent);
        lvSent.setAdapter(adapterSent);

        adapterRecv = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listRecv);
        lvRecv.setAdapter(adapterRecv);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "UID:" + currentUID);
        listSent.clear(); //listSent must update on activity start
        listRecv.clear(); //listRecv must update on activity start

        Query sentInvitesQuery =
                databaseRef.child("Invites").orderByChild("senderUID").equalTo(currentUID);
        Query recvInvitesQuery =
                databaseRef.child("Invites").orderByChild("recipientUID").equalTo(currentUID);

        Log.d(TAG, "sentReferenceInitialized");
        //TODO: SHOULD I USE A CHILD LISTENER?
        //ListView Fills: Sent Invites
        ValueEventListener sentInvitesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Invite invite = dataSnapshot.getValue(Invite.class);
                    listSent.add(invite.toString());
                    adapterSent.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        sentInvitesQuery.addValueEventListener(sentInvitesListener);

        //ListView Fills: Received Invites
        ValueEventListener recvInvitesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Invite invite = dataSnapshot.getValue(Invite.class);
                    listRecv.add(invite.toString());
                    adapterRecv.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        recvInvitesQuery.addValueEventListener(recvInvitesListener);

        mQueryValListenerMap.put(sentInvitesQuery, sentInvitesListener);
        mQueryValListenerMap.put(recvInvitesQuery, recvInvitesListener);

        lvRecv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String pressedInvite = ((TextView) view).getText().toString();
                Log.d(TAG, "clickedListView:\n" + pressedInvite);
                //do a little string parsing
                String[] sender_recipient_arr = pressedInvite.split("\\s+");
                String sender_recipient_key = sender_recipient_arr[0] + "_" + sender_recipient_arr[2];
                Log.d(TAG, "clickedInvite:" + sender_recipient_key);

                //Access based on sender_recipient
                DatabaseReference receivedInviteRef = databaseRef.child("Invites").child(sender_recipient_key);
                receivedInviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /*(final String timeAndDate,
                        final String senderUID, final String recipientUID,
                        final String senderDisplayName, final String recipientDisplayName)*/
                        Invite queriedInvite = snapshot.getValue(Invite.class);
                        String timeAndDate = String.format("%s %s", queriedInvite.getTime(), queriedInvite.getDate());
                        generateAlertDialogPartyCreation(
                                timeAndDate,
                                queriedInvite.getSenderUID(), queriedInvite.getRecipientUID(),
                                queriedInvite.getSenderDisplayName(), queriedInvite.getRecipientDisplayName());
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

    private void generateAlertDialogPartyCreation(final String timeAndDate,
                                                  final String senderUID, final String recipientUID,
                                                  final String senderDisplayName, final String recipientDisplayName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewInvitations.this);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { //Delete invitation and create party entry
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //delete invitation
                final DatabaseReference senderRef = databaseRef.child("Users").child(senderUID);
                senderRef.child("sentInviteTo").child(recipientUID).removeValue();



                final DatabaseReference recipientRef = databaseRef.child("Users").child(recipientUID);
                recipientRef.child("receivedInviteFrom").child(senderUID).removeValue();

                String sender_recipient = senderDisplayName + "_" + recipientDisplayName;

                //notify adapter
                DatabaseReference queriedInvite = databaseRef.child("Invites").child(sender_recipient);
                ValueEventListener queriedInviteListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Invite inviteInfo = snapshot.getValue(Invite.class);
                        String inviteInfoString = inviteInfo.toString();

                        //construct party key from partyName_senderUID
                        String partyName = inviteInfo.getPartyName();
                        String senderUID = inviteInfo.getSenderUID();
                        String partyUID  = partyName + "_" + senderUID;
                        //TODO: NOTE THAT THIS ASSUMES SENDER IS HOST, WILL BREAK IF NON-HOST INVITES ANOTHER USER

                        //create party entry
                        Party newParty = new Party(inviteInfo.getPartyName(), timeAndDate);
                        DatabaseReference newPartyRef = databaseRef.child("Parties").child(partyUID);
                        newPartyRef.setValue(newParty);
                        newPartyRef.child("members").child(senderUID).setValue(true);
                        newPartyRef.child("members").child(recipientUID).setValue(true);

                        //update user membership
                        senderRef.child("inParty").child(partyUID).setValue(true);
                        recipientRef.child("inParty").child(partyUID).setValue(true);

                        listRecv.remove(inviteInfoString);
                        adapterRecv.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                queriedInvite.addListenerForSingleValueEvent(queriedInviteListener);
                databaseRef.child("Invites").child(sender_recipient).removeValue(); //REMOVE INVITE toString FROM listRecv

                //notify party creation
                Toast.makeText(ViewInvitations.this,
                        "Party Created!",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

        builder.setMessage("Invitation received for party at: " + timeAndDate)
                .setTitle("Form Party?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}