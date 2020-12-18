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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

//TODO: CALL TO USERINFO IS ASYNCHRONOUS, I.E. NOT GUARANTEED UNLESS ACTED IN LISTENER
//TODO: SET DISPLAY NAME UPON ACCOUNT VERIFICATION?
//TODO: NOTE THAT VALUE OF INVITATION IS DISPLAYNAME AT TIME OF SENDING
//TODO: REDUNDANCY CHECK, i.e. reject action if invitation already

//TODO: PROMPT TIME AND DATE WHEN SENDING INVITATION

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
    private ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    //Pulled Data
    private UserInfo currentUserInfo;
    private String currentUID;

   //Listeners
    private ChildEventListener isOnlineListener;
    private ValueEventListener profileListener;
    private HashMap<DatabaseReference, ValueEventListener> mDatabaseRefValEventListenerMap;
    private HashMap<Query, ChildEventListener> mQueryChildListenerMap;
    private HashMap<Query, ValueEventListener> mQueryValListenerMap;

    //Debug
    private final String TAG = "SEEKING_USERS_DEBUG";
    private boolean debugFlagOnline = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_active_users);

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        //Listener Setup
        mDatabaseRefValEventListenerMap = new HashMap<>();
        mQueryChildListenerMap = new HashMap<>();
        mQueryValListenerMap = new HashMap<>();

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
                Log.d(TAG, "isOnlineStatus:"+ debugFlagOnline);
                if (currentUser != null) {
                    databaseRef.child("Registered Users")
                            .child(currentUID)
                            .child("online")
                            .setValue(true);

                    debugFlagOnline = true;
                    Log.d(TAG, "isOnlineStatus:"+ debugFlagOnline + " user:" + currentUID);
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
                if (debugFlagOnline) {
                    databaseRef.child("Registered Users").child(currentUser.getUid()).child("online").setValue(false);
                    Log.d(TAG, "databaseSignOutSuccessful");
                    debugFlagOnline = false;
                }
                mAuth.signOut();
                Log.d(TAG, "mAuthSignOutSuccessful");
                Log.d(TAG, "profileSignOut:success");
                launchHome();
            }
        });

        //Adapter setup
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        lv.setAdapter(adapter);

    }

    //IF USER DOES NOT SET displayName, THEN TRIM EMAIL BY DEFAULT

    @Override
    public void onStart() {
        super.onStart();
        listItems.clear(); //listItems must update on activity start

        //QUERY CURRENT USER'S PROFILE
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUID = currentUser.getUid();
            profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "queryCurrentUserProfileValueSet");
                    currentUserInfo = snapshot.getValue(UserInfo.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //DATA ACCESS CANCELLED
                }
            };
            databaseRef.child("Registered Users").child(currentUID).addValueEventListener(profileListener); //attaches listener to current user
            mDatabaseRefValEventListenerMap.put(databaseRef.child("Registered Users").child(currentUID), profileListener);
        }

        //QUERY ALL ONLINE USERS
        Query queryIsOnline = databaseRef.child("Registered Users").orderByChild("online").equalTo(true);
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
                if(!userInfo.isOnline()) {
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

        //ListView Setup
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String pressedDisplayName = ((TextView) view).getText().toString();

                Log.d(TAG, "queryDisplayNameToUserInfoAttempt:" + pressedDisplayName);
                Query queryUID = databaseRef.child("Registered Users").orderByChild("displayName").equalTo(pressedDisplayName).limitToFirst(1);
                ValueEventListener queryUIDListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "queryDisplayNameToUserInfoAttemptDataChange");
                        Log.d(TAG, "queryDisplayNameToUserInfoAttemptHasChildren " + snapshot.getChildrenCount());
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) { //should only yield one result

                            UserInfo qUserInfo = dataSnapshot.getValue(UserInfo.class);
                            Log.d(TAG, "accessQueriedUserAttempt");
                            Log.d(TAG, ""+ qUserInfo.toString());
                            Log.d(TAG, "accessQueriedUserSuccess");
                            Log.d(TAG, "accessCurrentUserAttempt");
                            Log.d(TAG, ""+ currentUserInfo.toString());
                            Log.d(TAG, "accessCurrentUserSuccess");
                            //BECAUSE ONLY ONE RESULT SHOULD OCCUR, SHOULD ONLY BE HANDLED ONCE

                            Log.d(TAG,"generateAlertDialogAttempt");
                            if (currentUserInfo.getUID().equals(qUserInfo.getUID())) {
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

    private void generateAlertDialogUserInfo(String userInfoString,
                                             final String senderUID,
                                             final String recipientUID,
                                             final String senderDisplayName,
                                             final String recipientDisplayName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewActiveUsersActivity.this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { //Alert Dialog Confirmed
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                databaseRef.child("Registered Users").child(senderUID).child("sentInviteTo").child(recipientUID).setValue(recipientDisplayName); //set dname?
                databaseRef.child("Registered Users").child(recipientUID).child("receivedInviteFrom").child(senderUID).setValue(senderDisplayName); //set dname?

                Toast.makeText(ViewActiveUsersActivity.this,
                        "Invitation Sent!",
                        Toast.LENGTH_SHORT).show();
                //TODO: NOTE THAT THIS WILL ALWAYS SET VALUE BUT WILL NOT PRODUCE REDUNDANT ENTRIES
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { //Alert Dialog Denied
            }
        });

        builder.setMessage(userInfoString)
                .setTitle("Invite to Party?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void onStop() {
        super.onStop();
        for (Map.Entry<DatabaseReference, ValueEventListener> entry: mDatabaseRefValEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        for (Map.Entry<Query, ChildEventListener> entry: mQueryChildListenerMap.entrySet()) {
            Query query = entry.getKey();
            ChildEventListener listener = entry.getValue();
            query.removeEventListener(listener);
        }
        for (Map.Entry<Query, ValueEventListener> entry: mQueryValListenerMap.entrySet()) {
            Query query = entry.getKey();
            ValueEventListener listener = entry.getValue();
            query.removeEventListener(listener);
        }
    }

    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}