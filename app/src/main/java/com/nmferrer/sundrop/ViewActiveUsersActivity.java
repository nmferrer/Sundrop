/*
 * Created by Noah Ferrer on 12/18/20 7:35 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 7:25 PM
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;


public class ViewActiveUsersActivity extends AppCompatActivity {


    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    //UI
    private ListView lv;
    private Button optInButton;
    private Button homeButton;
    private Button signOutButton;
    private ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;

    //Pulled Data
    private UserInfo currentUserInfo;
    private String currentUID;

    //Listeners
    private ChildEventListener isOnlineListener;
    private ValueEventListener profileListener;
    private HashMap<DatabaseReference, ValueEventListener> mDatabaseRefValEventListenerMap;
    private HashMap<Query, ChildEventListener> mQueryChildListenerMap;

    //Debug
    private final String TAG = "SEEKING_USERS_DEBUG";
    private boolean debugFlagOnline = false;

    //Bundle
    private HashMap<String, String> partyNameToLookupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_active_users_relative);

        //transparent notification bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }

        //generate nameToLookupKey Hash Map
        partyNameToLookupKey = new HashMap<>();
        if (getIntent().getExtras() != null) {
            Log.d(TAG, "bundlePassedToActivity:true");
            ArrayList<String> partyNames = getIntent().getStringArrayListExtra("partyNames");
            Log.d(TAG, "bundlePassedToActivity:partyNames");
            ArrayList<String> lookupKeys = getIntent().getStringArrayListExtra("partyLookupKeys");
            Log.d(TAG, "bundlePassedToActivity:partyLookupKeys");

            for (int index = 0; index < partyNames.size(); index++) {
                partyNameToLookupKey.put(partyNames.get(index), lookupKeys.get(index));
                Log.d(TAG, "addingPair:" + partyNames.get(index) + ":" + lookupKeys.get(index));
            }
        } else {
            Log.d(TAG, "bundlePassedToActivity:false");
        }
        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUID = currentUser.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        //Hash Map Setup
        mDatabaseRefValEventListenerMap = new HashMap<>();
        mQueryChildListenerMap = new HashMap<>();

        //QUERY CURRENT USER'S PROFILE
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUID = currentUser.getUid();
            profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUserInfo = snapshot.getValue(UserInfo.class);
                    Log.d(TAG, "onCreateQueryCurrentUserProfileValueSet:" + currentUserInfo.getDisplayName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //DATA ACCESS CANCELLED
                }
            };
            databaseRef.child("Users/" + currentUID).addValueEventListener(profileListener); //attaches listener to current user
            mDatabaseRefValEventListenerMap.put(databaseRef.child("Users/" + currentUID), profileListener);
        }

        //UI Setup
        lv = findViewById(R.id.userDynamicList);
        optInButton = findViewById(R.id.optIn);
        homeButton = findViewById(R.id.home);
        signOutButton = findViewById(R.id.signOut);

        lv.setClickable(true);


        //Listener Setup
        optInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ADD ENTRY TO DB
                Log.d(TAG, "isOnlineStatus:" + debugFlagOnline);
                if (currentUser != null) {
                    databaseRef.child("Users/" + currentUID + "/online").setValue(true);

                    debugFlagOnline = true;
                    Log.d(TAG, "isOnlineStatus:" + debugFlagOnline + " user:" + currentUID);
                } else {
                    Log.d(TAG, "Opt-in failure.");
                }
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchHome();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "signOutAttempt");
                databaseRef.child("Users/" + currentUID + "/online").setValue(false);
                mAuth.signOut();
                Log.d(TAG, "mAuthSignOutSuccessful");
                Log.d(TAG, "profileSignOut:success");
                launchLogin();
            }
        });

        //Adapter setup
        adapter = new ArrayAdapter<>(this,
                R.layout.list_white_text,
                listItems);
        lv.setAdapter(adapter);

        //ListView Setup
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String pressedDisplayName = ((TextView) view).getText().toString();

                Log.d(TAG, "queryDisplayNameToUserInfoAttempt:" + pressedDisplayName);
                Query queryUID = databaseRef.child("Users").orderByChild("displayName").equalTo(pressedDisplayName).limitToFirst(1);
                ValueEventListener queryUIDListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "queryDisplayNameToUserInfoAttemptDataChange");
                        Log.d(TAG, "queryDisplayNameToUserInfoAttemptHasChildren " + snapshot.getChildrenCount());
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) { //should only yield one result
                            UserInfo qUserInfo = dataSnapshot.getValue(UserInfo.class);
                            Log.d(TAG, "accessQueriedUserAttempt");
                            Log.d(TAG, "" + qUserInfo.toString());
                            Log.d(TAG, "accessQueriedUserSuccess");
                            Log.d(TAG, "accessCurrentUserAttempt");
                            Log.d(TAG, "" + currentUserInfo.toString());
                            Log.d(TAG, "accessCurrentUserSuccess");

                            //TODO: IMPLEMENT LOGIC CHECKS
                            //1) Already awaiting invite from you.
                            //2) Already in party with user.
                            //TODO: IMPLEMENT LOGIC CHECKS
                            if (currentUserInfo.getUID().equals(qUserInfo.getUID())) { //CHECK: USER PRESSED SELF
                                Toast.makeText(ViewActiveUsersActivity.this,
                                        "Can't start a party by yourself!",
                                        Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "generateAlertDialogRefusal:Call on Self");
                            } else {
                                generateAlertDialogUserInfo(qUserInfo.toString(),
                                        currentUID, qUserInfo.getUID(),
                                        currentUserInfo.getDisplayName(),
                                        qUserInfo.getDisplayName());
                                Log.d(TAG, "generateAlertDialogSuccess");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                };
                queryUID.addListenerForSingleValueEvent(queryUIDListener); //SINGLE USE LISTENER, OTHERWISE REDUNDANT CALLS OCCUR
            }
        });
    }

    //IF USER DOES NOT SET displayName, THEN TRIM EMAIL BY DEFAULT

    @Override
    public void onStart() {
        super.onStart();
        listItems.clear(); //listItems must update on activity start
        partyNameToLookupKey.clear(); //wipe clear to overwrite + leftover data may cause issues if new user signs in

        //generate nameToLookupKey Hash Map
        if (getIntent().getExtras() != null) {
            Log.d(TAG, "bundlePassedToActivity:true");
            ArrayList<String> partyNames = getIntent().getStringArrayListExtra("partyNames");
            ArrayList<String> lookupKeys = getIntent().getStringArrayListExtra("partyLookupKeys");
            for (int i = 0; i < partyNames.size(); i++) {
                partyNameToLookupKey.put(partyNames.get(i), lookupKeys.get(i));
                Log.d(TAG, "addingPair:" + partyNames.get(i) + ":" + lookupKeys.get(i));
            }
        } else {
            Log.d(TAG, "bundlePassedToActivity:false");
        }

        //QUERY CURRENT USER'S PROFILE
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUID = currentUser.getUid();
            profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUserInfo = snapshot.getValue(UserInfo.class);
                    Log.d(TAG, "onStartQueryCurrentUserProfileValueSet:" + currentUserInfo.getDisplayName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //DATA ACCESS CANCELLED
                }
            };
            databaseRef.child("Users/" + currentUID).addValueEventListener(profileListener); //attaches listener to current user
            mDatabaseRefValEventListenerMap.put(databaseRef.child("Users/" + currentUID), profileListener);
        }

        //QUERY ALL ONLINE USERS
        Query queryIsOnline = databaseRef.child("Users").orderByChild("online").equalTo(true);
        isOnlineListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //pull added user
                String UID = snapshot.getKey();
                UserInfo userInfo = snapshot.getValue(UserInfo.class);
                Log.d(TAG, "ADD KEY: " + UID);
                Log.d(TAG, "ADD VAL: " + userInfo.getDisplayName());

                //update view
                listItems.add(userInfo.getDisplayName());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //pull removed user
                String UID = snapshot.getKey();
                UserInfo userInfo = snapshot.getValue(UserInfo.class);
                Log.d(TAG, "REM KEY: " + UID);
                Log.d(TAG, "REM VAL: " + userInfo.getDisplayName());

                //update view
                listItems.remove(userInfo.getDisplayName());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String UID = snapshot.getKey();
                UserInfo userInfo = snapshot.getValue(UserInfo.class);
                Log.d(TAG, UID + " isOnline: " + userInfo.isOnline());
                if (!userInfo.isOnline()) {
                    listItems.remove(userInfo.getDisplayName());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        queryIsOnline.addChildEventListener(isOnlineListener); //attaches real-time listener to pull online users
        mQueryChildListenerMap.put(queryIsOnline, isOnlineListener);
    }

    private void generateAlertDialogUserInfo(String userInfoString,
                                             final String senderUID,
                                             final String recipientUID,
                                             final String senderDisplayName,
                                             final String recipientDisplayName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewActiveUsersActivity.this);
        builder.setPositiveButton(R.string.newParty, new DialogInterface.OnClickListener() { //Alert Dialog Confirmed
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                generateAlertDialogNewPartyCreation(senderUID, recipientUID, senderDisplayName, recipientDisplayName);
            }
        });
        builder.setNegativeButton(R.string.existingParty, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                generateAlertDialogExistingPartyInvite(senderUID, recipientUID, senderDisplayName, recipientDisplayName);
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { //Alert Dialog Denied
            }
        });

        builder.setMessage(userInfoString)
                .setTitle("Invite to Party?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void generateAlertDialogNewPartyCreation(final String senderUID, final String recipientUID, final String senderDisplayName, final String recipientDisplayName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewActiveUsersActivity.this);
        LayoutInflater inflater = getLayoutInflater().from(ViewActiveUsersActivity.this);
        View dialogView = inflater.inflate(R.layout.date_time_dialog, null);
        builder.setView(dialogView);

        final EditText editTextPartyName = dialogView.findViewById(R.id.editTextPartyName);
        final EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        final EditText editTextTime = dialogView.findViewById(R.id.editTextTime);
        SetDate dateInput = new SetDate(editTextDate, ViewActiveUsersActivity.this);
        SetTime timeInput = new SetTime(editTextTime, ViewActiveUsersActivity.this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String partyName = editTextPartyName.getText().toString();
                String date = editTextDate.getText().toString();
                String time = editTextTime.getText().toString();

                //TODO: INVITES ARE SOLELY HANDLED THROUGH INVITES TABLE. NO NEED FOR USER WRITES
                //IS THIS EFFICIENT?
                String inviteUID = partyName + "_" + senderUID + "_" + recipientUID; //allows for duplicate party names to exist and users can send invites to the same user for different parties
                String partyUID = partyName + "_" + senderUID; //identifies creator of party as host
                Invite newInvite = new Invite(partyUID, partyName, senderUID, senderDisplayName, recipientUID, recipientDisplayName, time, date);
                databaseRef.child("Invites/" + inviteUID).setValue(newInvite);
                //RESOLVED: MODIFY SENT/RECEIVED KEYS TO ALLOW USERS TO SEND MULTIPLE INVITES IF PARTY IS DIFFERENT

                //UPON NEW PARTY INVITE, CREATE PARTY ENTRY
                Party newParty = new Party(partyName, time, date);
                databaseRef.child("Parties/" + partyUID).setValue(newParty);
                databaseRef.child("Parties/" + partyUID).child("members").child(senderUID).setValue(true);
                partyNameToLookupKey.put(partyName, partyUID);

                //update user membership SENDER ONLY. RECIPIENT BECOMES MEMBER ON ACCEPTANCE
                databaseRef.child("Users/" + senderUID + "/inParty/" + partyUID).setValue(true);
                Toast.makeText(ViewActiveUsersActivity.this,
                        "Party created and invitation sent!",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setTitle("Scheduling an Appointment");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void generateAlertDialogExistingPartyInvite(final String senderUID, final String recipientUID, final String senderDisplayName, final String recipientDisplayName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewActiveUsersActivity.this);
        LayoutInflater inflater = getLayoutInflater().from(ViewActiveUsersActivity.this);
        View dialogView = inflater.inflate(R.layout.party_select_spinner_dialog, null);
        builder.setView(dialogView);

        final ArrayList<String> listPartyOptions = new ArrayList<>();
        for (Map.Entry<String, String> entry : partyNameToLookupKey.entrySet()) {
            Log.d(TAG, "partyFound:" + entry.getKey());
            listPartyOptions.add(entry.getKey());
        }
        //VALUE IS PASSED FROM HOME TO THIS ACTIVITY ON CREATION

        //Spinner Dialog Setup
        final Spinner partySelectSpinner = dialogView.findViewById(R.id.partySelectSpinnerDialog);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listPartyOptions);
        partySelectSpinner.setAdapter(arrayAdapter);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (listPartyOptions.isEmpty()) {
            Toast.makeText(ViewActiveUsersActivity.this, "Not currently in any parties.", Toast.LENGTH_SHORT).show();
        } else {
            builder.setView(dialogView);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //GIVEN PARTY NAME, GENERATE INVITE
                    final String partyName = listPartyOptions.get(partySelectSpinner.getSelectedItemPosition());
                    final String partyUID = partyNameToLookupKey.get(partyName);
                    final String party_sender_recipient = partyName + "_" + senderUID + "_" + recipientUID;
                    databaseRef.child("Parties/" + partyUID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Party queriedParty = snapshot.getValue(Party.class);
                            Log.d(TAG, "queriedPartyReceived");
                            //PERFORM LOGIC CHECK TO SEE IF USER IS ALREADY IN PARTY. IF NOT, THEN CREATE AND SEND INVITE.
                            DataSnapshot memberList = snapshot.child("members"); //CHECK MEMBER LIST
                            if (memberList.hasChild(recipientUID)) {
                                Toast.makeText(ViewActiveUsersActivity.this, "User is already in that party.", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "userInPartyAlready");
                            } else {
                                Invite existingPartyInvite = new Invite(partyUID, partyName, currentUserInfo.getUID(), currentUserInfo.getDisplayName(), recipientUID, recipientDisplayName, queriedParty.getTime(), queriedParty.getDate());
                                databaseRef.child("Invites/" + party_sender_recipient).setValue(existingPartyInvite);
                                Toast.makeText(ViewActiveUsersActivity.this, "Invite sent!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        }
        builder.setTitle("Select a party to invite user to:");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry : mDatabaseRefValEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        for (Map.Entry<Query, ChildEventListener> entry : mQueryChildListenerMap.entrySet()) {
            Query query = entry.getKey();
            ChildEventListener listener = entry.getValue();
            query.removeEventListener(listener);
        }
    }

    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void launchLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}