package com.shahbazanwar.feedingbangladesh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class FoodDonateActivity extends AppCompatActivity {


    private FirebaseAuth Auth;
    private DatabaseReference VolunteerDatabase;
    private String userID;
    private ImageButton ib_cancel;
    private EditText et_fooditems, et_quantity;
    private Button btn_submit;

    private String userId;
    private String foodname;
    private String foodquantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_donate);

        ib_cancel = findViewById(R.id.ib_cancel);
        et_fooditems = findViewById(R.id.et_fooditems);
        et_quantity = findViewById(R.id.et_quantity);
        btn_submit = findViewById(R.id.btn_submit);

        Auth = FirebaseAuth.getInstance();
        userId = Auth.getCurrentUser().getUid();

        foodname = et_fooditems.getText().toString();
        foodquantity = et_quantity.getText().toString();
        VolunteerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(userId).child("foodinfo");

        getFoodInfo();

        ib_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_fooditems != null && et_quantity != null) {
                    Intent intent = new Intent(FoodDonateActivity.this, DMapsActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void getFoodInfo() {
        VolunteerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("foodname") != null) {
                        foodname = map.get("foodname").toString();
                    }
                    if (map.get("foodquantity") != null) {
                        foodquantity = map.get("foodquantity").toString();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}