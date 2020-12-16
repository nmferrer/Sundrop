package com.nmferrer.sundrop;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button onlineButton;
    private Button singlePlayerButton;
    private Button settingsButton;
    private Button gamerButton;

    private final String TAG = "HOME_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        onlineButton = findViewById(R.id.onlineButton);
        singlePlayerButton = findViewById(R.id.singlePlayerButton);
        settingsButton = findViewById(R.id.settingsButton);
        gamerButton = findViewById(R.id.gamerButton);

        onlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchOnline();
            }
        });
        singlePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSinglePlayer();
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
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
    }

    private void launchSinglePlayer() {
        Intent intent = new Intent(this, SinglePlayerActivity.class);
        startActivity(intent);
    }
    private void launchSettings() {
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
