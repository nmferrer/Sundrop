package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    private EditText fieldDisplayName;
    private EditText fieldEmail;
    private EditText fieldStatus;
    private Button buttonConfirm;
    private Button buttonCancel;

    private UserInfo updatedUserInfoInfo;
    private final String TAG = "LISTENER_DEBUG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        //UI Setup
        fieldDisplayName = findViewById(R.id.edittextDisplayName);
        fieldEmail = findViewById(R.id.edittextEmail);
        fieldStatus = findViewById(R.id.edittextStatus);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonCancel = findViewById(R.id.buttonCancel);

        //Listener Setup
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //validate forms
                if (!validateForm()) {
                    Toast.makeText(SettingsActivity.this, "Email field must be filled.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: UPDATE USER ENTRY IN DB
                    //create updated user info struct
                    //push updated info to db
                    //confirm
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchHome();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        //query UID values to fill fields
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String UID = currentUser.getUid();
            ValueEventListener profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserInfo savedInfo = snapshot.getValue(UserInfo.class);
                    fieldEmail.setText(savedInfo.getEmail());

                    if (savedInfo.getDisplayName() != null) {
                        fieldDisplayName.setText(savedInfo.getDisplayName());
                    }
                    if (savedInfo.getStatus() != null) {
                        fieldStatus.setText(savedInfo.getStatus());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //DATA ACCESS CANCELLED
                }
            };
            databaseRef.child("Registered Users").child(UID).addValueEventListener(profileListener);
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        String email = fieldEmail.getText().toString();
        //String displayName = fieldDisplayName.getText().toString();
        //String status = fieldStatus.getText().toString();

        if (TextUtils.isEmpty(email)) {
            fieldEmail.setError("Required.");
            valid = false;
        } else {
            fieldEmail.setError(null);
        }
        //OPTIONAL: displayName and Status
        return valid;
    }

    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}