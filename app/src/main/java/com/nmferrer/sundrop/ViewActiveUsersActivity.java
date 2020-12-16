package com.nmferrer.sundrop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ViewActiveUsersActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    private ListView lv;
    private Button optInButton;
    private Button signOutButton;

    private ChildEventListener childEventListener;
    private ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    private final String TAG = "SEEKING_USERS_DEBUG";
    private boolean isOnline = false;

    @Override
    public void onStart() {
        super.onStart();
        listItems.clear(); //listItems must update on activity creation
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_active_users);

        //Firebase Setup
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        //UI Setup
        lv = findViewById(R.id.userDynamicList);
        optInButton = findViewById(R.id.optIn);
        signOutButton = findViewById(R.id.signOut);

        lv.setClickable(true);

        //Listener Setup
        optInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ADD ENTRY TO DB
                if (currentUser != null) {
                    String UID = currentUser.getUid();
                    String name = Configuration.trimEmail(currentUser.getEmail());
                    databaseRef.child("Active Users")
                            .child(UID)
                            .setValue(name);

                    isOnline = true;
                } else {
                    Log.d(TAG, "Opt-in failure.");
                }
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Log.d(TAG, "mAuthSignOutSuccessful");

                if (isOnline) {
                    DatabaseReference toRemove = FirebaseDatabase.getInstance().getReference("Active Users/" + currentUser.getUid());
                    Log.d(TAG, "databaseAccessSuccessful");
                    toRemove.removeValue();
                    Log.d(TAG, "databaseSignOutSuccessful");
                    isOnline = false;
                }
                
                launchHome();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                        Toast.LENGTH_SHORT).show();

                //using displayName, query for UserInfo
                //display info as popup
            }
        });

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        lv.setAdapter(adapter);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //pull added child
                String UID = snapshot.getKey();
                String name = snapshot.getValue(String.class);
                Log.d(TAG, "ADD KEY: " + UID);
                Log.d(TAG, "ADD VAL: " + name);

                //update view
                listItems.add(name);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //pull removed child
                String UID = snapshot.getKey();
                String name = snapshot.getValue(String.class);
                Log.d(TAG, "REM KEY: " + UID);
                Log.d(TAG, "REM VAL: " + name);

                //update view
                listItems.remove(name);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        databaseRef.child("Active Users").addChildEventListener(childEventListener);
    }


    private void launchHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}