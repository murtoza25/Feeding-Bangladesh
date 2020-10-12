package com.shahbazanwar.feedingbangladesh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class VolunteerDisplayActivity extends AppCompatActivity {
    private TextView tv_hellovolunteer;
    private ImageButton ib_receive_food;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_display);


        tv_hellovolunteer = findViewById(R.id.tv_hellovolunteer);
        ib_receive_food = findViewById(R.id.ib_receive_food);
        ib_receive_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VolunteerDisplayActivity.this,VMapsActivity.class);
                startActivity(intent);
            }
        });
    }
}