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

public class ViewActiveUsersActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    private ListView lv;
    private Button optInButton;
    private Button homeButton;
    private Button signOutButton;

    private ChildEventListener activeUsersListener;
    private ValueEventListener profileListener;
    private ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    private final String TAG = "SEEKING_USERS_DEBUG";

    private boolean isOnline = false;

    private UserInfo currentUserInfo;
    private String currentUID;

    private HashMap<DatabaseReference, ValueEventListener> mValueEventListenerMap;
    private HashMap<DatabaseReference, ChildEventListener> mChildEventListenerMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_active_users);

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mValueEventListenerMap = new HashMap<DatabaseReference, ValueEventListener>();
        mChildEventListenerMap = new HashMap<DatabaseReference, ChildEventListener>();

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
                Log.d(TAG, "isOnlineStatus:"+isOnline);
                if (currentUser != null) {
                    databaseRef.child("Active Users")
                            .child(currentUID)
                            .setValue(currentUserInfo);

                    isOnline = true;
                    Log.d(TAG, "isOnlineStatus:"+isOnline+ " user:" + currentUID);
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
                if (isOnline) {
                    DatabaseReference toRemove = FirebaseDatabase.getInstance().getReference().child("Active Users").child(currentUID);
                    Log.d(TAG, "databaseAccessSuccessful");
                    toRemove.removeValue();
                    Log.d(TAG, "databaseSignOutSuccessful");
                    isOnline = false;
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
    //TODO: SET DISPLAY NAME UPON ACCOUNT VERIFICATION?

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
                    currentUserInfo = snapshot.getValue(UserInfo.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //DATA ACCESS CANCELLED
                }
            };
            databaseRef.child("Registered Users").child(currentUID).addValueEventListener(profileListener); //attaches listener to current user
            mValueEventListenerMap.put(databaseRef.child("Registered Users").child(currentUID), profileListener);
        }

        activeUsersListener = new ChildEventListener() {
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
                //Should I care for the change case? All transactions go by UID, which are constant.
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        databaseRef.child("Active Users").addChildEventListener(activeUsersListener); //attaches real-time listener to Active Users table
        mChildEventListenerMap.put(databaseRef.child("Active Users"), activeUsersListener);

        //ListView Setup
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String pressedDisplayName = ((TextView) view).getText().toString();

                Log.d(TAG, "queryDisplayNameToUserInfoAttempt:" + pressedDisplayName);
                Query queryUID = databaseRef.child("Registered Users").orderByChild("displayName").equalTo(pressedDisplayName).limitToFirst(1);
                queryUID.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "queryDisplayNameToUserInfoAttemptDataChange");
                        Log.d(TAG, "queryDisplayNameToUserInfoAttemptHasChildren " + snapshot.hasChildren());

                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) { //should only yield one result
                            UserInfo qUserInfo = dataSnapshot.getValue(UserInfo.class);
                            Log.d(TAG, "queryDisplayNameToUserInfoAttempt:Success " + qUserInfo.getUID());

                            //BECAUSE ONLY ONE RESULT SHOULD OCCUR, SHOULD ONLY BE HANDLED ONCE
                            generateAlertDialogUserInfo(qUserInfo.toString(), currentUser.getUid(), qUserInfo.getUID());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void generateAlertDialogUserInfo(String userInfoString, final String senderUID, final String recipientUID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewActiveUsersActivity.this);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { //Alert Dialog Confirmed
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //CHECK if invite already exists (SENDER_UID + _ + RECIPIENT_UID)
                String currentSenderRecipient = senderUID + "_" + recipientUID;
                String currentSenderRecipientReversed = recipientUID + "_" + senderUID;

                /*
                //TODO: SET REDUNDANCY CHECKS!
                Query queryInvitation = databaseRef.child("Invitations").orderByChild("senderRecipient").equalTo(currentSenderRecipient);
                queryInvitation.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "querySenderRecipientCheckHasChildren A" + snapshot.hasChildren());
                        if (snapshot.hasChildren()) {
                            Log.d(TAG, "querySenderRecipientCheckHasChildren A forceStop");
                            Toast.makeText(ViewActiveUsersActivity.this,
                                    "You have already sent an invitation to this user.",
                                    Toast.LENGTH_SHORT).show();
                            return; //STOP, INVITATION EXISTS
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                Log.d(TAG, "querySenderRecipientCheckComplete");

                Query queryInvitationReversed = databaseRef.child("Invitations").orderByChild("senderRecipient").equalTo(currentSenderRecipientReversed);
                queryInvitationReversed.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "queryRecipientSenderCheckHasChildren B" + snapshot.hasChildren());
                        if (snapshot.hasChildren()) {
                            Log.d(TAG, "queryRecipientSenderCheckHasChildren B forceStop");
                            Toast.makeText(ViewActiveUsersActivity.this,
                                    "This user has already sent you an invitation!",
                                    Toast.LENGTH_SHORT).show();
                            return; //STOP, INVITATION EXISTS
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                Log.d(TAG, "queryRecipientSenderCheckComplete");
                */

                //CREATE invitation relationship
                Invitation invitation = new Invitation(senderUID, recipientUID);
                databaseRef.child("Invitations").child(currentSenderRecipient).setValue(invitation);

                Toast.makeText(ViewActiveUsersActivity.this,
                        "Invitation Sent!",
                        Toast.LENGTH_SHORT).show();
                //TODO: NOTE THAT THIS WILL ALWAYS SET VALUE BUT WILL NOT PRODUCE REDUNDANT ENTRIES
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
        for (Map.Entry<DatabaseReference, ValueEventListener> entry: mValueEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ValueEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }
        for (Map.Entry<DatabaseReference, ChildEventListener> entry: mChildEventListenerMap.entrySet()) {
            DatabaseReference ref = entry.getKey();
            ChildEventListener listener = entry.getValue();
            ref.removeEventListener(listener);
        }

    }

    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}