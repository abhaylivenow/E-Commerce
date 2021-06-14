package com.example.projectecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.EventListener;
import java.util.HashMap;

public class register_activity extends AppCompatActivity {

    private EditText enterName, enterPassword, enterPhoneNumber;
    private Button btn_signUp;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_activity);

        initViews();

        // create new account
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    public void createNewAccount() {
        // get all the information entered by the user
        String name = enterName.getText().toString();
        String phone = enterPhoneNumber.getText().toString();
        String password = enterPassword.getText().toString();

        // handle basic input errors
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Password length should be atleast 6", Toast.LENGTH_SHORT).show();
        }
        // now handle user information
        else {
            loadingBar.setTitle("Creating Account");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(false);

            validateUserDetails(name, phone, password);
        }
    }

    // validate the information entered by user, handle if the user already exist then throw error
    public void validateUserDetails(final String name, final String phone, final String password) {
        // create reference to the firebase database
        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        // add listener
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // create hashMap to map user data with the value
                if (!snapshot.child("Users").child(phone).exists()) {
                    HashMap<String, Object> userDataMap = new HashMap<>();
                    userDataMap.put("name", name);
                    userDataMap.put("phone", phone);
                    userDataMap.put("password", password);

                    // attach user map to the database and add on complete listener
                    rootRef.child("Users").child(phone).updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // handle if the task is successful
                            if (task.isSuccessful()) {
                                Toast.makeText(register_activity.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(register_activity.this, login_activity.class);
                                startActivity(i);
                            }
                            // handle if the task is not successful
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(register_activity.this, "Network error occurred. Try again later", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                // handle user already exist
                else {
                    Toast.makeText(register_activity.this, "User already exist", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(register_activity.this, "Please try another phone number", Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(register_activity.this, register_activity.class);
                    startActivity(i);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void initViews() {
        enterName = findViewById(R.id.enter_name_signup);
        enterPassword = findViewById(R.id.enter_password_signup);
        enterPhoneNumber = findViewById(R.id.enter_phone_signup);
        btn_signUp = findViewById(R.id.signUp_register);
        loadingBar = new ProgressDialog(this);
    }
}