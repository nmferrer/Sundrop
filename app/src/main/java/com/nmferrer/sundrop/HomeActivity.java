package com.nmferrer.sundrop;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.nmferrer.sundrop.experiments.RadialMenu;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button onlineButton;
    private Button profileButton;
    private Button invitationsButton;
    private Button gamerButton;

    private Button debugSignOutButton;

    private final String TAG = "HOME_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        onlineButton = findViewById(R.id.onlineButton);
        profileButton = findViewById(R.id.profileButton);
        invitationsButton = findViewById(R.id.invitationsButton);
        gamerButton = findViewById(R.id.gamerButton);
        debugSignOutButton = findViewById(R.id.debugSignOutButton);

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
        debugSignOutButton.setOnClickListener(new View.OnClickListener() {
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
