package com.shahbazanwar.feedingbangladesh;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VMapsActivity extends AppCompatActivity implements  OnMapReadyCallback{
    private Button Logout,Settings;


    private GoogleMap mMap;

    Location LastLocation;

    LocationRequest LocationRequest;

    private Switch WorkingSwitch;

    private int status = 0;

    private String donorId = "";

    private LatLng pickupLatLng;

    private SupportMapFragment mapFragment;

    private LinearLayout DonorInfo;

    private TextView DonorName, DonorPhone,FoodQuantity,FoodName;


    private FusedLocationProviderClient FusedLocationClient;

    private Boolean isLoggingOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        FusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DonorInfo = findViewById(R.id.DonorInfo);



        DonorName = findViewById(R.id.DonorName);
        DonorPhone = findViewById(R.id.DonorPhone);
        FoodName = findViewById(R.id.FoodName);
        FoodQuantity = findViewById(R.id.FoodQuantity);

        WorkingSwitch =  findViewById(R.id.WorkingSwitch);

        // working switch button for volunteer

        WorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    connectVolunteer();
                }else{
                    disconnectVolunteer();
                }
            }
        });


        Settings = findViewById(R.id.settings);
        Logout = findViewById(R.id.logout);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut = true;

                disconnectVolunteer();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(VMapsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VMapsActivity.this, VolunteerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });


    }

    private void getAssignedDonor(){
        String volunteerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedDonorRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(volunteerId).child("donorRequest").child("donorRequestId");
        assignedDonorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status = 1;
                    donorId = dataSnapshot.getValue().toString();
                    getAssignedFoodPickupLocation();
                    getAssignedDonorInfo();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private DatabaseReference assignedFoodPickupLocationRef;
    private ValueEventListener assignedFoodPickupLocationRefListener;
    private void getAssignedFoodPickupLocation(){
        assignedFoodPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("donorRequest").child(donorId).child("l");
        assignedFoodPickupLocationRefListener = assignedFoodPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !donorId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Donor & Food information
    private void getAssignedDonorInfo(){
        DonorInfo.setVisibility(View.VISIBLE);
        DatabaseReference DonorInfo = FirebaseDatabase.getInstance().getReference().child("Users").child("Donors").child(donorId);
        DonorInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        DonorName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!=null){
                        DonorPhone.setText(map.get("phone").toString());
                    }if(map.get("foodname")!=null){
                        FoodName.setText(map.get("foodname").toString());
                    }
                    if(map.get("foodquantity")!=null){
                        FoodQuantity.setText(map.get("foodquantity").toString());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationRequest = new LocationRequest();
        LocationRequest.setInterval(1000);
        LocationRequest.setFastestInterval(1000);
        LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }


    }


    LocationCallback LocationCallback = new LocationCallback(){

        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){


                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("volunteersAvailable");
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("volunteersWorking");
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    switch (donorId){
                        case "":
                            geoFireWorking.removeLocation(userId);
                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;

                        default:
                            geoFireAvailable.removeLocation(userId);
                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                    }
                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(VMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(VMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        FusedLocationClient.requestLocationUpdates(LocationRequest, LocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    private void connectVolunteer(){
        checkLocationPermission();
        FusedLocationClient.requestLocationUpdates(LocationRequest, LocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void disconnectVolunteer(){
        if(FusedLocationClient != null){
            FusedLocationClient.removeLocationUpdates(LocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("volunteersAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }


}