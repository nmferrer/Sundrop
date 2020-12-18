package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: CALL TO USERINFO IS ASYNCHRONOUS, I.E. NOT GUARANTEED UNLESS ACTED IN LISTENER

public class ViewInvitations extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String currentUID;

    //UI
    private ListView lvSent, lvRecv;
    private Button homeButton;
    private ArrayList<String> listSent = new ArrayList<String>();
    private ArrayList<String> listRecv = new ArrayList<String>();
    ArrayAdapter<String> adapterSent;
    ArrayAdapter<String> adapterRecv;

    //Pulled Data

    //Listeners
    private ValueEventListener profileListener;
    private HashMap<Query, ValueEventListener> mQueryListenerMap;
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
        mQueryListenerMap = new HashMap<>();
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

        DatabaseReference sentInvitesRef =
                databaseRef.child("Registered Users")
                        .child(currentUID)
                        .child("sentInviteTo");

        Log.d(TAG, "sentReferenceInitialized");
        ChildEventListener sentInvitesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "eventListenerSentAdded");
                String recipient = snapshot.getValue(String.class);
                listSent.add(String.format("%s on DAY TIME", recipient));
                adapterSent.notifyDataSetChanged();
                Log.d(TAG, "eventListenerSentAddedSuccess");

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "eventListenerSentRemoved");
                String recipient = snapshot.getValue(String.class);
                listSent.remove(String.format("%s on DAY TIME", recipient));
                adapterSent.notifyDataSetChanged();
                Log.d(TAG, "eventListenerSentRemovedSuccess");

            }

            //WOULD IT BE MORE EFFECTIVE TO CHANGE OR DELETE AND RESEND?
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
        sentInvitesRef.addChildEventListener(sentInvitesListener);
        Log.d(TAG, "sentListenerAdded");
        mDatabaseRefChildEventListenerMap.put(sentInvitesRef,sentInvitesListener);

        DatabaseReference receivedInvitesRef =
                databaseRef.child("Registered Users")
                        .child(currentUID)
                        .child("receivedInviteFrom");

        Log.d(TAG, "recvReferenceInitialized");
        ChildEventListener receivedInvitesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "eventListenerReceivedAdded");
                String sender = snapshot.getValue(String.class);
                listRecv.add(String.format("%s on DAY TIME", sender));
                adapterSent.notifyDataSetChanged();
                Log.d(TAG, "eventListenerReceivedAddedSuccess");
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "eventListenerReceived");
                String sender = snapshot.getValue(String.class);
                listRecv.remove(String.format("%s on DAY TIME", sender));
                adapterSent.notifyDataSetChanged();
                Log.d(TAG, "eventListenerReceivedRemovedSuccess");
            }

            //WOULD IT BE MORE EFFECTIVE TO CHANGE OR DELETE AND RESEND?
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
        receivedInvitesRef.addChildEventListener(receivedInvitesListener);
        Log.d(TAG, "recvListenerAdded");
        mDatabaseRefChildEventListenerMap.put(receivedInvitesRef, receivedInvitesListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        for (Map.Entry<Query, ValueEventListener> entry: mQueryListenerMap.entrySet()) {
            Query query = entry.getKey();
            ValueEventListener listener = entry.getValue();
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

    private void generateAlertDialogPartyCreation(String invitationInfoString, String senderUID, String recipientUID) {
        final String senderRecipient = senderUID + "_" + recipientUID;
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewInvitations.this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { //Delete invitation and create party entry
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //databaseRef.child("Invitations").child(senderRecipient).removeValue();
                //create party entry
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { //Delete invitation
                //databaseRef.child("Invitations").child(senderRecipient).removeValue();
            }
        });

        builder.setMessage(invitationInfoString)
                .setTitle("Form Party?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}