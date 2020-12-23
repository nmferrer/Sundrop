package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    private EditText fieldEmail;
    private EditText fieldPassword;
    private Button createAccountButton;
    private Button signInButton;
    private Button resendConfirmationButton;
    private ConstraintLayout constraintLayout;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //transparent notification bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }

        //animation currently does nothing?
        constraintLayout = (ConstraintLayout) findViewById(R.id.loginConstraintLayout);
        animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1000);
        animationDrawable.setExitFadeDuration(1000);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            launchHome();
        }

        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        Log.d(TAG, "initializeFirebase:success");

        fieldEmail = findViewById(R.id.editTextEmail);
        fieldPassword = findViewById(R.id.editTextPassword);
        createAccountButton = findViewById(R.id.createAccountButton);
        signInButton = findViewById(R.id.signInButton);
        resendConfirmationButton = findViewById(R.id.resendConfirmationButton);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(fieldEmail.getText().toString(), fieldPassword.getText().toString());
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(fieldEmail.getText().toString(), fieldPassword.getText().toString());
            }
        });

        //handle case where user creates account but no email received, disappears otherwise
        if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isEmailVerified()) {
            resendConfirmationButton.setVisibility(View.VISIBLE);
        }
        resendConfirmationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmailVerification();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            launchHome();
        }//handle case where user creates account but no email received

    }

    //ACCOUNT HANDLING
    private void createAccount(final String email, String password) {
        Log.d(TAG, "createAccount: " + email);
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");

                            UserInfo userInfo = new UserInfo(mAuth.getUid(), trimEmail(email));
                            Log.d(TAG, "userInfoCreated:success");

                            databaseRef.child("Users").child(mAuth.getUid()).setValue(userInfo);
                            Log.d(TAG, "databasePushUser:success");
                            sendEmailVerification();
                            Toast.makeText(LoginActivity.this, "Account created successfully! A confirmation email will be arriving shortly.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user.isEmailVerified()) {
                        Log.d(TAG, "isUserVerified:true");
                        Toast.makeText(LoginActivity.this, "Sign-in successful.", Toast.LENGTH_SHORT).show();
                        launchHome();
                    } else {
                        Log.d(TAG, "isUserVerified:false");
                        generateResendConfirmationDialog();
                    }
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void sendEmailVerification() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    private void generateResendConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendEmailVerification();
                Toast.makeText(LoginActivity.this, "A confirmation email will be arriving shortly.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setTitle("Resend confirmation email?");
        builder.setMessage("Your account has not been verified yet. Would you like to resend the confirmation email?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //VALIDATE FORM
    private boolean validateForm() {
        boolean valid = true;
        String email = fieldEmail.getText().toString();
        String password = fieldPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            fieldEmail.setError("Required.");
            valid = false;
        } else {
            fieldEmail.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            fieldPassword.setError("Required.");
            valid = false;
        } else {
            fieldPassword.setError(null);
        }
        return valid;
    }

    private String trimEmail(String email) {
        int endIndex = email.indexOf('@');
        if (endIndex != -1)
            return email.substring(0, endIndex);
        else
            return email;
    }

}