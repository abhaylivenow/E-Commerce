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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectecommerce.Model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login_activity extends AppCompatActivity {

    private EditText enterPhone,enterPassword;
    private Button login_btn;
    private TextView txt_admin, txt_notAdmin;
    private ProgressDialog loadingBar;
    private String parentDbName = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        initViews();

        // handle login button
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        // login as an admin, root of the database changed to admin
        txt_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_btn.setText("Login as Admin");
                txt_admin.setVisibility(View.GONE);
                txt_notAdmin.setVisibility(View.VISIBLE);
                parentDbName = "Admin";
            }
        });
        // login as a user, root of the database is changes to Users
        txt_notAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_btn.setText("Login");
                txt_admin.setVisibility(View.VISIBLE);
                txt_notAdmin.setVisibility(View.GONE);
                parentDbName = "Users";
            }
        });
    }

    // login method
    public void loginUser(){
        // get the information entered by the user
        String phone = enterPhone.getText().toString();
        String password = enterPassword.getText().toString();

        // handle basic stuffs
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please enter your Phone number", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            // every thing is fine now allow access to the account
            allowAccessToAccount(phone,password);
        }
    }

    public void allowAccessToAccount(final String phone, final String password){
        // create a reference to the firebase database
        final DatabaseReference rootRef;
        // initialize database reference
        rootRef = FirebaseDatabase.getInstance().getReference();

        // add listener
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if the root database name and the phone number exist in the database
                if(snapshot.child(parentDbName).child(phone).exists()){
                    // attach the user model to the database
                    Users userData = snapshot.child(parentDbName).child(phone).getValue(Users.class);
                    // handle null pointer error
                    assert userData != null;

                    // if phone number entered by the user matches with the one in the database
                    if(userData.getPhone().equals(phone)){
                        // if password entered by the use matches with the one in the database
                        if(userData.getPassword().equals(password)){
                            // if root database is admin then login as admin
                            if(parentDbName.equals("Admin")){
                                loadingBar.dismiss();
                                Toast.makeText(login_activity.this, "Logged in successfully as admin", Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(login_activity.this,admin_activity.class);
                                startActivity(i);
                            }
                            // else login as the user
                            else{
                                loadingBar.dismiss();
                                Toast.makeText(login_activity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(login_activity.this, home_activity.class);
                                startActivity(i);
                            }
                        }
                        // password is wrong
                        else {
                            loadingBar.dismiss();
                            Toast.makeText(login_activity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                    // account with this number does not exist
                    else {
                        loadingBar.dismiss();
                        Toast.makeText(login_activity.this, "Account with this number does not exist", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    loadingBar.dismiss();
                    Toast.makeText(login_activity.this, "Account with this number does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void initViews(){
        enterPhone = findViewById(R.id.enter_phone_login);
        enterPassword = findViewById(R.id.enter_password_login);
        login_btn = findViewById(R.id.btn_login);
        txt_admin = findViewById(R.id.admin_text);
        txt_notAdmin = findViewById(R.id.not_admin_text);
        loadingBar = new ProgressDialog(this);
    }
}