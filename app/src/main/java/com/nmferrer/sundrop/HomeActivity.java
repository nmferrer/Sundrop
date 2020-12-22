package com.nmferrer.sundrop;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.Map;

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
        setContentView(R.layout.activity_home_revised);

        //transparent notification bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        onlineButton = findViewById(R.id.onlineButton);
        profileButton = findViewById(R.id.profileButton);
        invitationsButton = findViewById(R.id.invitationsButton);
        gamerButton = findViewById(R.id.gamerButton);
        signOutButton = findViewById(R.id.debugSignOutButton);

        partySelectSpinner = findViewById(R.id.partySelectSpinner);
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
                    Toast.makeText(HomeActivity.this, "DEBUG: User signed out.",
                            Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    launchLogin();
                } else {
                    Toast.makeText(HomeActivity.this, "DEBUG: No user logged in.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        //TODO: BECAUSE I CLEAR AND REPOPULATE DURING onStart(), SPINNER WILL RESET. CAN I MAKE THIS PERSISTENT?
        //Adapter setup
        partyOptionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listPartyOptions);
        partySelectSpinner.setAdapter(partyOptionsAdapter);
        partyOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        partySelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //TODO: TEXT RETURNS TO BLACK WHEN PRESSING BACK. CHANGE AT XML?
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView)adapterView.getChildAt(0)).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ((TextView)adapterView.getChildAt(0)).setTextColor(Color.WHITE);
            }
        });

        partyConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (partySelectSpinner.getCount() == 0) {
                    Toast.makeText(HomeActivity.this,
                            "Currently not a member of any party.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    String partyName = listPartyOptions.get(partySelectSpinner.getSelectedItemPosition());
                    String partyKey = partyOptionsFullKey.get(partyName);

                    //bundle key and launch partyFormedActivity
                    Log.d(TAG, partyKey);
                    launchPartyWithKey(partyKey);
                }
            }
        });


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

    private void launchLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    private void launchSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    private void launchOnline() {
        Log.d(TAG, "User signed in. Launching party search.");
        Log.d(TAG, "Email: " + mAuth.getCurrentUser().getEmail());
        Intent intent = new Intent(this, ViewActiveUsersActivity.class);
        //BUNDLE DOES NOT SUPPORT HASH MAPS? WORKAROUND:
        //ITERATE THROUGH NAME_KEY HASH MAP AND FILL PARTY NAMES DURING onCreate AND onStart
        ArrayList<String> partyNames = new ArrayList<>();
        ArrayList<String> lookupKeys = new ArrayList<>();
        for (Map.Entry<String, String> nameToFullKey : partyOptionsFullKey.entrySet()) {
            String partyName = nameToFullKey.getKey();
            String fullKey = nameToFullKey.getValue();
            partyNames.add(partyName);
            lookupKeys.add(fullKey);
        }
        intent.putExtra("partyNames", partyNames);
        intent.putExtra("partyLookupKeys", lookupKeys);
        startActivity(intent);
    }

    private void launchInvitations() {
        Log.d(TAG, "User signed in. Launching invitations.");
        Intent intent = new Intent(this, ViewInvitations.class);
        startActivity(intent);
    }

    private void launchPartyWithKey(String partyKey) {
        Intent intent = new Intent(this, PartyChatActivity.class);
        intent.putExtra("partyID", partyKey);
        startActivity(intent);
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
