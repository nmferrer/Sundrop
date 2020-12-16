package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
User sets profile information here.
DisplayName: Public username visible to all other users
Email: User account email TODO: allow user to update and reconfirm email.
Seeking: What does the user want to do?
Availability: Times and days user is available.

WORKFLOW:
If user is signed in, query database to load in current profile information.
    Appropriate fields will be filled, e.g. DisplayName will show the current user's display name.
Else, all fields will be empty.
Upon pressing "Confirm Changes," store all fields into a UserInfo class, push UserInfo to database,
    and update existing entry.
 */
public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    private EditText editTextDisplayName;
    private EditText editTextEmail;
    private EditText editTextSeeking;
    private Button buttonConfirm;
    private Button buttonCancel;

    private CheckBox checkBoxSunday;
    private CheckBox checkBoxMonday;
    private CheckBox checkBoxTuesday;
    private CheckBox checkBoxWednesday;
    private CheckBox checkBoxThursday;
    private CheckBox checkBoxFriday;
    private CheckBox checkBoxSaturday;
    private EditText editTextSunday;
    private EditText editTextMonday;
    private EditText editTextTuesday;
    private EditText editTextWednesday;
    private EditText editTextThursday;
    private EditText editTextFriday;
    private EditText editTextSaturday;

    private UserInfo updateUserInfo;
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
        editTextDisplayName = findViewById(R.id.editTextDisplayName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSeeking = findViewById(R.id.editTextSeeking);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonCancel = findViewById(R.id.buttonCancel);

        checkBoxSunday = findViewById(R.id.checkBoxSunday);
        checkBoxMonday = findViewById(R.id.checkBoxMonday);
        checkBoxTuesday = findViewById(R.id.checkBoxTuesday);
        checkBoxWednesday = findViewById(R.id.checkBoxWednesday);
        checkBoxThursday = findViewById(R.id.checkBoxThursday);
        checkBoxFriday = findViewById(R.id.checkBoxFriday);
        checkBoxSaturday = findViewById(R.id.checkBoxSaturday);

        //TODO: LET THESE BE TimePickerDialogs
        editTextSunday = findViewById(R.id.editTextSunday);
        editTextMonday = findViewById(R.id.editTextMonday);
        editTextTuesday = findViewById(R.id.editTextTuesday);
        editTextWednesday = findViewById(R.id.editTextWednesday);
        editTextThursday = findViewById(R.id.editTextThursday);
        editTextFriday = findViewById(R.id.editTextSaturday);
        editTextSaturday = findViewById(R.id.editTextSaturday);


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
                    String UID, updateDisplayName, updateEmail, updateSeeking, updateAvailability;
                    UID = currentUser.getUid();
                    updateDisplayName  = editTextDisplayName.getText().toString();
                    updateEmail = editTextEmail.getText().toString(); //TODO: ALLOW USER TO UPDATE ACCOUNT EMAIL
                    updateSeeking = editTextSeeking.getText().toString();
                    updateAvailability = generateAvailabilityString();

                    Toast.makeText(SettingsActivity.this, "Confirm pressed.",
                            Toast.LENGTH_SHORT).show();

                    //Trigger Alert Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(SettingsActivity.this, "Pog.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(SettingsActivity.this, "Not Pog.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    String updateConfirmationMessage =
                            "The following changes will be made to your profile:\n" +
                                    "Display Name: " + updateDisplayName + "\n" +
                                    "Email: " + updateEmail + "\n" +
                                    "Looking For: " + updateSeeking +" \n" +
                                    "Availability:\n " + updateAvailability;
                    builder.setMessage(updateConfirmationMessage)
                            .setTitle("Confirm Profile Changes?");
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    //Alert Dialog Confirmed
                    updateUserInfo = new UserInfo(UID, updateEmail, updateDisplayName, updateSeeking, updateAvailability);//Create UserInfo Struct
                    databaseRef.child("Registered Users").child(UID).setValue(updateUserInfo); //Push updated info to respective entry
                    databaseRef.child("Active Users").child(UID).setValue(updateUserInfo); //Push updated info to respective entry
                    //Alert Dialog Denied
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
                    editTextEmail.setText(savedInfo.getEmail());

                    if (savedInfo.getDisplayName() != null) {
                        editTextDisplayName.setText(savedInfo.getDisplayName());
                    }
                    if (savedInfo.getSeeking() != null) {
                        editTextSeeking.setText(savedInfo.getSeeking());
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
        String email = editTextEmail.getText().toString();
        //String displayName = fieldDisplayName.getText().toString();
        //String status = fieldStatus.getText().toString();

        //add time checks here?
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Required.");
            valid = false;
        } else {
            editTextEmail.setError(null);
        }
        //OPTIONAL: displayName and Status
        return valid;
    }

    private String generateAvailabilityString() {
        String userAvailability = "";
        //check checkboxes
        //append appropriate times
        if (checkBoxSunday.isChecked())
            userAvailability += "Sunday: " + editTextSunday.getText().toString() + "\n";
        if (checkBoxMonday.isChecked())
            userAvailability += "Monday: " + editTextMonday.getText().toString() + "\n";
        if (checkBoxTuesday.isChecked())
            userAvailability += "Tuesday: " + editTextTuesday.getText().toString() + "\n";
        if (checkBoxWednesday.isChecked())
            userAvailability += "Wednesday: " + editTextWednesday.getText().toString() + "\n";
        if (checkBoxThursday.isChecked())
            userAvailability += "Thursday: " + editTextThursday.getText().toString() + "\n";
        if (checkBoxFriday.isChecked())
            userAvailability += "Friday: " + editTextFriday.getText().toString() + "\n";
        if (checkBoxSaturday.isChecked())
            userAvailability += "Saturday: " + editTextSaturday.getText().toString() + "\n";
        return userAvailability;
    }

    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}