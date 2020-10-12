package com.shahbazanwar.feedingbangladesh;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class VolunteerSettingsActivity extends AppCompatActivity {

    private EditText NameField, PhoneField;

    private Button Back, Confirm;



    private FirebaseAuth Auth;
    private DatabaseReference VolunteerDatabase;

    private String userID;
    private String Name;
    private String Phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_settings);


        NameField = (EditText) findViewById(R.id.name);
        PhoneField = (EditText) findViewById(R.id.phone);

        Back = (Button) findViewById(R.id.back);
        Confirm = (Button) findViewById(R.id.confirm);

        Auth = FirebaseAuth.getInstance();
        userID = Auth.getCurrentUser().getUid();
        VolunteerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(userID);

        getUserInfo();



        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
    private void getUserInfo(){
        VolunteerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        Name = map.get("name").toString();
                        NameField.setText(Name);
                    }
                    if(map.get("phone")!=null){
                        Phone = map.get("phone").toString();
                        PhoneField.setText(Phone);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void saveUserInformation() {
        Name = NameField.getText().toString();
        Phone = PhoneField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", Name);
        userInfo.put("phone", Phone);

        VolunteerDatabase.updateChildren(userInfo);


    }

}
