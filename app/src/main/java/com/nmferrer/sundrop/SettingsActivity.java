/*
 * Created by Noah Ferrer on 12/18/20 7:35 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 12/18/20 6:29 AM
 *
 */

package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/*
User sets profile information here.
DisplayName: Public username visible to all other users
Email: User account email
    TODO: allow user to update and reconfirm email.
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
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseRef;

    //UI
    private EditText editTextDisplayName;
    private EditText editTextEmail;
    private EditText editTextSeeking;
    private Button buttonConfirm;
    private Button buttonCancel;

    private EditText editTextSundayStart;
    private EditText editTextMondayStart;
    private EditText editTextTuesdayStart;
    private EditText editTextWednesdayStart;
    private EditText editTextThursdayStart;
    private EditText editTextFridayStart;
    private EditText editTextSaturdayStart;
    private EditText editTextSundayEnd;
    private EditText editTextMondayEnd;
    private EditText editTextTuesdayEnd;
    private EditText editTextWednesdayEnd;
    private EditText editTextThursdayEnd;
    private EditText editTextFridayEnd;
    private EditText editTextSaturdayEnd;

    //Pulled Data
    private UserInfo updateUserInfo;
    private String oldDisplayName;
    private String oldEmail;
    //Listeners
    private HashMap<DatabaseReference, ValueEventListener> mValueEventListenerMap;
    private HashMap<DatabaseReference, ChildEventListener> mChildEventListenerMap;
    //Debug
    private final String TAG = "SETTINGS_DEBUG";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //transparent notification bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mValueEventListenerMap = new HashMap<DatabaseReference, ValueEventListener>();
        mChildEventListenerMap = new HashMap<DatabaseReference, ChildEventListener>();

        //UI Setup
        editTextDisplayName = findViewById(R.id.editTextDisplayName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSeeking = findViewById(R.id.editTextSeeking);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonCancel = findViewById(R.id.buttonCancel);

        editTextSundayStart = findViewById(R.id.editTextSundayStart);
        editTextMondayStart = findViewById(R.id.editTextMondayStart);
        editTextTuesdayStart = findViewById(R.id.editTextTuesdayStart);
        editTextWednesdayStart = findViewById(R.id.editTextWednesdayStart);
        editTextThursdayStart = findViewById(R.id.editTextThursdayStart);
        editTextFridayStart = findViewById(R.id.editTextFridayStart);
        editTextSaturdayStart = findViewById(R.id.editTextSaturdayStart);

        editTextSundayEnd = findViewById(R.id.editTextSundayEnd);
        editTextMondayEnd = findViewById(R.id.editTextMondayEnd);
        editTextTuesdayEnd = findViewById(R.id.editTextTuesdayEnd);
        editTextWednesdayEnd = findViewById(R.id.editTextWednesdayEnd);
        editTextThursdayEnd = findViewById(R.id.editTextThursdayEnd);
        editTextFridayEnd = findViewById(R.id.editTextFridayEnd);
        editTextSaturdayEnd = findViewById(R.id.editTextSaturdayEnd);

        Log.d(TAG, "bindingEditTextOnClick");

        SetTime timeSunStart    = new SetTime(editTextSundayStart,      SettingsActivity.this);
        SetTime timeMonStart    = new SetTime(editTextMondayStart,      SettingsActivity.this);
        SetTime timeTueStart    = new SetTime(editTextTuesdayStart,     SettingsActivity.this);
        SetTime timeWedStart    = new SetTime(editTextWednesdayStart,   SettingsActivity.this);
        SetTime timeThursStart  = new SetTime(editTextThursdayStart,    SettingsActivity.this);
        SetTime timeFriStart    = new SetTime(editTextFridayStart,      SettingsActivity.this);
        SetTime timeSatStart    = new SetTime(editTextSaturdayStart,    SettingsActivity.this);

        SetTime timeSunEnd      = new SetTime(editTextSundayEnd,    SettingsActivity.this);
        SetTime timeMonEnd      = new SetTime(editTextMondayEnd,    SettingsActivity.this);
        SetTime timeTueEnd      = new SetTime(editTextTuesdayEnd,   SettingsActivity.this);
        SetTime timeWedEnd      = new SetTime(editTextWednesdayEnd, SettingsActivity.this);
        SetTime timeThursEnd    = new SetTime(editTextThursdayEnd,  SettingsActivity.this);
        SetTime timeFriEnd      = new SetTime(editTextFridayEnd,    SettingsActivity.this);
        SetTime timeSatEnd      = new SetTime(editTextSaturdayEnd,  SettingsActivity.this);

        //Listener Setup
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //validate forms
                if (!validateForm()) {
                    Toast.makeText(SettingsActivity.this, "Required fields must be filled.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    final String UID, updateDisplayName, updateEmail, updateSeeking, updateAvailability;
                    UID = currentUser.getUid();
                    updateDisplayName  = editTextDisplayName.getText().toString();
                    updateEmail = editTextEmail.getText().toString(); //TODO: ALLOW USER TO UPDATE ACCOUNT EMAIL
                    updateSeeking = editTextSeeking.getText().toString();
                    updateAvailability = generateAvailabilityString();

                    String updateConfirmationMessage =
                            "The following changes will be made to your profile:\n" +
                                    "Display Name: " + updateDisplayName + "\n" +
                                    "Looking For: " + updateSeeking +" \n" +
                                    "Availability:\n" + updateAvailability;

                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { //Alert Dialog Confirmed
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DatabaseReference updatedProfile = databaseRef.child("Users").child(UID);
                            updatedProfile.child("displayName").setValue(updateDisplayName);
                            updatedProfile.child("seeking").setValue(updateSeeking);
                            updatedProfile.child("availability").setValue(updateAvailability);

                            Log.d(TAG, "pushToRegUsers:Success");
                            Log.d(TAG, "pushToActiveUsers:Success");

                            //IF displayName IS CHANGED, THEN DELETE OLD ENTRY
                            if (!oldDisplayName.equals(updateDisplayName)) {
                                Log.d(TAG, "deletionChangedName:Success");
                            }
                            if(!oldEmail.equals(updateEmail)) {
                                //TODO: UPDATE mAuth CREDENTIALS
                            }
                            Log.d(TAG, "pushToDisplayNameUID:Success");
                            launchHome();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) { //Alert Dialog Denied
                        }
                    });

                    builder.setMessage(updateConfirmationMessage)
                            .setTitle("Confirm Profile Changes?");
                    AlertDialog dialog = builder.create();
                    dialog.show();
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
            editTextEmail.setText(mAuth.getCurrentUser().getEmail());
            ValueEventListener profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserInfo savedInfo = snapshot.getValue(UserInfo.class);
                    if (savedInfo.getDisplayName() != null) {
                        editTextDisplayName.setText(savedInfo.getDisplayName());
                    }
                    if (savedInfo.getSeeking() != null) {
                        editTextSeeking.setText(savedInfo.getSeeking());
                    }
                    if (snapshot.hasChild("availability")) {
                        //parse String and fill appropriate fields
                        String temp = savedInfo.getAvailability().trim();
                        String[] availabilityArr = temp.split("\n");
                        for (String s: availabilityArr) {
                            //ASSUMES STRINGS ARE GUARANTEED TO FOLLOW FORMAT (DAY: TIME AM|PM TO TIME AM|PM) AND BE OF SIZE 6
                            //e.g. Thursday: 10:00 AM to 11:00 PM
                            s = s.trim();
                            Log.d(TAG, s);
                            String[] dayAndTime = s.split(" ");
                            String day = dayAndTime[0];
                            String startTime = dayAndTime[1] + " " + dayAndTime[2];
                            String endTime = dayAndTime[4] + " " + dayAndTime[5];

                            fillDateAndTimeFields(day, startTime, endTime);
                        }
                    }
                    //POSSIBLE UPDATES
                    oldDisplayName = savedInfo.getDisplayName(); //pull old displayName for comparison
                    oldEmail = mAuth.getCurrentUser().getEmail(); //pull account email for comparison
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            databaseRef.child("Users").child(UID).addValueEventListener(profileListener);
            mValueEventListenerMap.put(databaseRef.child("Users").child(UID), profileListener);
        }
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

    private boolean validateForm() {
        //TODO: Make call to validate times.
        boolean valid = true;
        String email = editTextEmail.getText().toString();
        String displayName = editTextDisplayName.getText().toString();

        //time checks here?
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Required.");
            valid = false;
        } else {
            editTextEmail.setError(null);
        }
        if (TextUtils.isEmpty(displayName)) {
            editTextDisplayName.setError("Required.");
            valid = false;
        } else {
            editTextDisplayName.setError(null);
        }
        return valid;
    }

    private boolean validateTimes(EditText startTime, EditText endTime) {
        //TODO: LOGIC ASSUMES USER INPUTS "FRIENDLY" FORMAT
        //MODIFY REGEX TO BE MORE VERSATILE

        //LOGIC ASSUMES TEXT FOLLOWS FORMAT XX:XX [AM|PM]
        String[] timeAmPmStart = startTime.getText().toString().split(" ");
        String[] timeAmPmEnd = endTime.getText().toString().split(" ");
        String[] timeStart = timeAmPmStart[0].split(":");
        String[] timeEnd = timeAmPmEnd[0].split(":");
        String amOrPmStart = timeAmPmStart[1];
        String amOrPmEnd = timeAmPmEnd[1];
        int startTimeHour = Integer.parseInt(timeStart[0]);
        int startTimeMin = Integer.parseInt(timeStart[1]);
        int endTimeHour = Integer.parseInt(timeEnd[0]);
        int endTimeMin = Integer.parseInt(timeEnd[1]);

        if (startTimeHour > 12) {
            startTime.setError("Invalid range.");
            return false;
        }
        if (endTimeHour > 12) {
            endTime.setError("Invalid range");
            return false;
        }
        if (amOrPmStart.equals("PM") && amOrPmEnd.equals("AM")) { //PM TO AM
            startTime.setError("Start time must be later than end time.");
            return false;
        }
        if (amOrPmStart.equals(amOrPmEnd)) { //BOTH AM OR BOTH PM
            if (startTimeHour != 12 && startTimeHour > endTimeHour) {
                startTime.setError("Start time must be later than end time.");
                return false;
            } else if (startTimeHour == endTimeHour) {
                if (startTimeMin > endTimeMin) {
                    startTime.setError("Start time must be later than end time.");
                }
            }
        }
        if (!amOrPmStart.equals(amOrPmEnd)) { //AM TO PM
            //DO NOTHING
        }
        startTime.setError(null);
        endTime.setError(null);
        return true;
    }

    private String generateAvailabilityString() {
        String userAvailability = "";
        //check checkboxes
        //append appropriate times
        if (!TextUtils.isEmpty(editTextSundayStart.getText().toString())  && !TextUtils.isEmpty(editTextSundayEnd.getText().toString()))
            userAvailability += "\tSunday: "
                    + editTextSundayStart.getText().toString() + " to "
                    + editTextSundayEnd.getText().toString()   + "\n";

        if (!TextUtils.isEmpty(editTextMondayStart.getText().toString()) && !TextUtils.isEmpty(editTextMondayEnd.getText().toString()))
            userAvailability += "\tMonday: "
                    + editTextMondayStart.getText().toString() + " to "
                    + editTextMondayEnd.getText().toString()   +"\n";

        if (!TextUtils.isEmpty(editTextTuesdayStart.getText().toString()) && !TextUtils.isEmpty(editTextTuesdayEnd.getText().toString()))
            userAvailability += "\tTuesday: "
                    + editTextTuesdayStart.getText().toString() + " to "
                    + editTextTuesdayEnd.getText().toString()   + "\n";

        if (!TextUtils.isEmpty(editTextWednesdayStart.getText().toString())  && !TextUtils.isEmpty(editTextWednesdayEnd.getText().toString()))
            userAvailability += "\tWednesday: "
                    + editTextWednesdayStart.getText().toString()   + " to "
                    + editTextWednesdayEnd.getText().toString()     + "\n";

        if (!TextUtils.isEmpty(editTextThursdayStart.getText().toString())  && !TextUtils.isEmpty(editTextThursdayEnd.getText().toString()))
            userAvailability += "\tThursday: "
                    + editTextThursdayStart.getText().toString()+ " to "
                    + editTextThursdayEnd.getText().toString()  + "\n";

        if (!TextUtils.isEmpty(editTextFridayStart.getText().toString())  && !TextUtils.isEmpty(editTextFridayEnd.getText().toString()))
            userAvailability += "\tFriday: "
                    + editTextFridayStart.getText().toString() + " to "
                    + editTextFridayEnd.getText().toString()   +"\n";

        if (!TextUtils.isEmpty(editTextSaturdayStart.getText().toString()) && !TextUtils.isEmpty(editTextSaturdayEnd.getText().toString()))
            userAvailability += "\tSaturday: "
                    + editTextSaturdayStart.getText().toString() + " to "
                    + editTextSaturdayEnd.getText().toString()   + "\n";

        return userAvailability;
    }

    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void fillDateAndTimeFields(String day, String timeStart, String timeEnd) {
        switch (day) {
            case "Sunday:":
                editTextSundayStart.setText(timeStart);
                editTextSundayEnd.setText(timeEnd);
                Log.d(TAG,"fillSunday");
                break;
            case "Monday:":
                editTextMondayStart.setText(timeStart);
                editTextMondayEnd.setText(timeEnd);
                Log.d(TAG,"fillMonday");
                break;
            case "Tuesday:":
                editTextTuesdayStart.setText(timeStart);
                editTextTuesdayEnd.setText(timeEnd);
                Log.d(TAG,"fillTuesday");
                break;
            case "Wednesday:":
                editTextWednesdayStart.setText(timeStart);
                editTextWednesdayEnd.setText(timeEnd);
                Log.d(TAG,"fillWednesday");
                break;
            case "Thursday:":
                editTextThursdayStart.setText(timeStart);
                editTextThursdayEnd.setText(timeEnd);
                Log.d(TAG,"fillThursday");
                break;
            case "Friday:":
                editTextFridayStart.setText(timeStart);
                editTextFridayEnd.setText(timeEnd);
                Log.d(TAG,"fillFriday");
                break;
            case "Saturday:":
                editTextSaturdayStart.setText(timeStart);
                editTextSaturdayEnd.setText(timeEnd);
                Log.d(TAG,"fillSaturday");
                break;
            default:
        }
    }
}