package com.shahbazanwar.feedingbangladesh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnVolunteer,btnDonor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDonor = findViewById(R.id.btnDonor);
        btnVolunteer = findViewById(R.id.btnVolunteer);
        btnDonor.setOnClickListener(this);
        btnVolunteer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnDonor){
            Intent intent = new Intent(MainActivity.this,DonorLoginActivity.class);
            startActivity(intent);
        }
        if(view.getId() == R.id.btnVolunteer){
            Intent intent = new Intent(MainActivity.this,VolunteerLoginActivity.class);
            startActivity(intent);
        }

    }
}