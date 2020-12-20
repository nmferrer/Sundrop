package com.nmferrer.sundrop;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmferrer.sundrop.experiments.RadialMenu;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    //UI
    private Button onlineButton;
    private Button profileButton;
    private Button invitationsButton;
    private Button gamerButton;
    private Button signOutButton;
    private Spinner partySelectSpinner;
    private Button partyConfirmButton;
    private ArrayList<String> listPartyOptions;
    private HashMap<String, String> partyOptionsFullKey;
    private ArrayAdapter<String> partyOptionsAdapter;

    //Debug
    private final String TAG = "HOME_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        onlineButton = findViewById(R.id.onlineButton);
        profileButton = findViewById(R.id.profileButton);
        invitationsButton = findViewById(R.id.invitationsButton);
        gamerButton = findViewById(R.id.gamerButton);
        signOutButton = findViewById(R.id.debugSignOutButton);

        partySelectSpinner = (Spinner)findViewById(R.id.partySelectSpinner);
        partyConfirmButton = findViewById(R.id.partyConfirmButton);
        listPartyOptions = new ArrayList<>();
        partyOptionsFullKey = new HashMap<>();


        onlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchOnline();
            }
        });
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSettings();
            }
        });
        gamerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateBackground();
            }
        });
        invitationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth.getCurrentUser() != null) {
                    launchInvitations();
                } else {
                    Toast.makeText(HomeActivity.this, "DEBUG: No user logged in.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth.getCurrentUser() != null) {
                    Toast.makeText(HomeActivity.this, "DEBUG: User " + mAuth.getCurrentUser().getUid() + " signed out.",
                            Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                } else {
                    Toast.makeText(HomeActivity.this, "DEBUG: No user logged in.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Adapter setup
        partyOptionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listPartyOptions);
        partySelectSpinner.setAdapter(partyOptionsAdapter);
        partyOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        partySelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView)adapterView.getChildAt(0)).setTextColor(Color.WHITE);
                Toast.makeText(HomeActivity.this, "Item selected",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(HomeActivity.this, "No item selected..",
                        Toast.LENGTH_SHORT).show();
            }
        });
        //TODO: CONFIRM PARTY (Pull key from hashmap and query)


    }

    @Override
    protected void onStart() {
        super.onStart();
        //prevent duplicate entries
        if (currentUser != null) { //USER MUST BE SIGNED IN
            listPartyOptions.clear();
            partyOptionsFullKey.clear();
            //query database and populate spinner
            String currentUID = mAuth.getCurrentUser().getUid();
            DatabaseReference partiesRef = databaseRef.child("Users/" + currentUID + "/inParty");
            Log.d(TAG, "referencePlacedOn:Users/" + currentUID + "/inParty");
            partiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot party : snapshot.getChildren()) {
                        String queriedPartyAndSender = party.getKey();
                        Log.d(TAG, "AddingParty:" + queriedPartyAndSender);
                        String queriedParty = queriedPartyAndSender.split("_")[0]; //index 1 is the sender, not needed for this action
                        listPartyOptions.add(queriedParty);
                        partyOptionsFullKey.put(queriedParty, queriedPartyAndSender);
                        partyOptionsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void launchSinglePlayer() {
        Intent intent = new Intent(this, RadialMenu.class);
        startActivity(intent);
    }
    private void launchSettings() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(HomeActivity.this, "No user logged in.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    private void launchOnline() {
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "No user signed in. Launching login.");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Log.d(TAG, "User signed in. Launching party search.");
            Log.d(TAG, "Email: " + mAuth.getCurrentUser().getEmail());
            Intent intent = new Intent(this, ViewActiveUsersActivity.class);
            startActivity(intent);
        }
    }

    private void launchInvitations() {
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "No user signed in. Launching login.");

        } else {
            Log.d(TAG, "User signed in. Launching invitations.");
            Intent intent = new Intent(this, ViewInvitations.class);
            startActivity(intent);
        }
    }

    private void animateBackground() {
        int colorFrom = getResources().getColor(R.color.homePink);
        int colorTo = getResources().getColor(R.color.homeBlue);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                gamerButton.setBackgroundColor((int) valueAnimator.getAnimatedValue());
            }
        });
        colorAnimation.start();
        colorAnimation.setRepeatCount(Animation.INFINITE);
    }
}
