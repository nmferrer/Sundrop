package com.nmferrer.sundrop;

//Helper class for common routines

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Configuration {

    public static String trimEmail(String email) {
        int endIndex = email.indexOf('@');
        if (endIndex != -1)
            return email.substring(0, endIndex);
        return email;
    }
    public static void connectToFirebase(FirebaseAuth mAuth, FirebaseUser currentUser, FirebaseDatabase database, DatabaseReference databaseRef) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference();

        String TAG = "CONFIG_HELPER";
        Log.d(TAG, "Connection established: {User: " + currentUser.getUid() + "DatabaseReference: " + databaseRef.toString() + "}");
    }
}
