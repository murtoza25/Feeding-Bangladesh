package com.shahbazanwar.feedingbangladesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class DonorLoginActivity extends AppCompatActivity {
    private EditText etDonorEmail,etDonorPassword;
    private Button btnDonorLogin,btnDonorRegistration;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_login);

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(DonorLoginActivity.this,FoodDonateActivity.class);
                    startActivity(intent);
                }
            }
        };

        etDonorEmail = findViewById(R.id.etDonorEmail);
        etDonorPassword = findViewById(R.id.etDonorPassword);

        btnDonorLogin = findViewById(R.id.btnDonorLogin);
        btnDonorRegistration = findViewById(R.id.btnDonorRegistration);

        btnDonorRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = etDonorEmail.getText().toString().trim();
                final String password = etDonorPassword.getText().toString().trim();
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(DonorLoginActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    Toast.makeText(DonorLoginActivity.this,"Sign up error",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    String user_id = auth.getCurrentUser().getUid();
                                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Donors").child(user_id);
                                    current_user_db.setValue(email);
                                }
                            }
                        });
            }
        });

        btnDonorLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = etDonorEmail.getText().toString().trim();
                final String password = etDonorPassword.getText().toString().trim();
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(DonorLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(DonorLoginActivity.this,"Sign in error",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
    }
}