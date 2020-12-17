package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
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


    private UserInfo updateUserInfo;
    private final String TAG = "SETTINGS_DEBUG";

    private String oldDisplayName;

    private HashMap<DatabaseReference, ValueEventListener> mValueEventListenerMap;
    private HashMap<DatabaseReference, ChildEventListener> mChildEventListenerMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        checkBoxSunday = findViewById(R.id.checkBoxSunday);
        checkBoxMonday = findViewById(R.id.checkBoxMonday);
        checkBoxTuesday = findViewById(R.id.checkBoxTuesday);
        checkBoxWednesday = findViewById(R.id.checkBoxWednesday);
        checkBoxThursday = findViewById(R.id.checkBoxThursday);
        checkBoxFriday = findViewById(R.id.checkBoxFriday);
        checkBoxSaturday = findViewById(R.id.checkBoxSaturday);

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

        //make this reusable
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
                    Toast.makeText(SettingsActivity.this, "Email field must be filled.",
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
                                    "Email: " + updateEmail + "\n" +
                                    "Looking For: " + updateSeeking +" \n" +
                                    "Availability:\n" + updateAvailability;

                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { //Alert Dialog Confirmed
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            updateUserInfo = new UserInfo(UID, updateEmail, updateDisplayName, updateSeeking, updateAvailability);//Create UserInfo Struct
                            databaseRef.child("Registered Users").child(UID).setValue(updateUserInfo); //Push updated info to respective entry
                            Log.d(TAG, "pushToRegUsers:Success");
                            //TODO: THIS SHOULD ONLY EXECUTE IF USER IS "ONLINE"
                            databaseRef.child("Active Users").child(UID).setValue(updateUserInfo); //Push updated info to respective entry
                            Log.d(TAG, "pushToActiveUsers:Success");

                            //IF displayName IS CHANGED, THEN DELETE OLD ENTRY
                            if (!oldDisplayName.equals(updateDisplayName)) {
                                Log.d(TAG, "deletionChangedName:Success");
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

            //WRITES TO REGISTERED USERS
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
                    if (savedInfo.getAvailability() != null) {
                        //parse String and fill appropriate fields
                        String temp = savedInfo.getAvailability().trim();
                        String[] availabilityArr = temp.split("\n");
                        for (String s: availabilityArr) {
                            //STRINGS ARE GUARANTEED TO FOLLOW FORMAT (DAY: TIME TO TIME) AND BE OF SIZE 4
                            //e.g. Thursday: 10:00 to 11:00
                            s = s.trim();
                            Log.d(TAG, s);
                            String[] dayAndTime = s.split(" ");
                            String day = dayAndTime[0];
                            String startTime = dayAndTime[1];
                            String endTime = dayAndTime[3];

                            fillDateAndTimeFields(day, startTime, endTime);
                        }
                    }

                    oldDisplayName = savedInfo.getDisplayName(); //pull old displayName for comparison
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //DATA ACCESS CANCELLED
                }
            };
            databaseRef.child("Registered Users").child(UID).addValueEventListener(profileListener);
            mValueEventListenerMap.put(databaseRef.child("Registered Users").child(UID), profileListener);
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
        boolean valid = true;
        String email = editTextEmail.getText().toString();
        //String displayName = fieldDisplayName.getText().toString();
        //String status = fieldStatus.getText().toString();

        //time checks here?
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
            userAvailability += "\tSunday: " + editTextSundayStart.getText().toString()         + " to " + editTextSundayEnd.getText().toString()   +"\n";
        if (checkBoxMonday.isChecked())
            userAvailability += "\tMonday: " + editTextMondayStart.getText().toString()         + " to " + editTextMondayEnd.getText().toString()   +"\n";
        if (checkBoxTuesday.isChecked())
            userAvailability += "\tTuesday: " + editTextTuesdayStart.getText().toString()       + " to " + editTextTuesdayEnd.getText().toString()  +"\n";
        if (checkBoxWednesday.isChecked())
            userAvailability += "\tWednesday: " + editTextWednesdayStart.getText().toString()   + " to " + editTextWednesdayEnd.getText().toString() +"\n";
        if (checkBoxThursday.isChecked())
            userAvailability += "\tThursday: " + editTextThursdayStart.getText().toString()     + " to " + editTextThursdayEnd.getText().toString() +"\n";
        if (checkBoxFriday.isChecked())
            userAvailability += "\tFriday: " + editTextFridayStart.getText().toString()         + " to " + editTextFridayEnd.getText().toString()   +"\n";
        if (checkBoxSaturday.isChecked())
            userAvailability += "\tSaturday: " + editTextSaturdayStart.getText().toString()     + " to " + editTextSaturdayEnd.getText().toString() +"\n";
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
    //helper for TimePickerDialog. This is really clever.
    //https://stackoverflow.com/questions/17901946/timepicker-dialog-from-clicking-edittext
    class SetTime implements View.OnFocusChangeListener, TimePickerDialog.OnTimeSetListener {
        private EditText editText;
        private Calendar myCalendar;

        public SetTime(EditText editText, Context ctx){
            this.editText = editText;
            this.editText.setOnFocusChangeListener(this);
            this.myCalendar = Calendar.getInstance();
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                new TimePickerDialog(SettingsActivity.this, this, hour, minute, true).show();
            }
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            this.editText.setText( hourOfDay + ":" + String.format("%02d", minute));
        }
    }
}