package com.shahbazanwar.feedingbangladesh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location LastLocation;
    LocationRequest LocationRequest;

    private FusedLocationProviderClient FusedLocationClient;

    private Button Logout, Request, Settings;

    private LatLng pickupLocation;

    private Marker pickupMarker;

    private SupportMapFragment mapFragment;

    private LinearLayout VolunteerInfo;


    private TextView VolunteerName, VolunteerPhone;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        FusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        VolunteerInfo = findViewById(R.id.VolunteerInfo);


        VolunteerName = findViewById(R.id.VolunteerName);
        VolunteerPhone = findViewById(R.id.VolunteerPhone);



        Logout = findViewById(R.id.logout);
        Request = findViewById(R.id.Request);

        Settings = findViewById(R.id.Settings);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DMapsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("donorRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(LastLocation.getLatitude(), LastLocation.getLongitude()));

                    pickupLocation = new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Food From Here"));

                    getClosestVolunteer();

            }
        });

        Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DMapsActivity.this, DonorSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });
    }

    private int radius = 1;
    private Boolean volunteerFound = false;
    private String volunteerFoundID;

    GeoQuery geoQuery;
    private void getClosestVolunteer(){
        DatabaseReference volunteerLocation = FirebaseDatabase.getInstance().getReference().child("volunteersAvailable");

        GeoFire geoFire = new GeoFire(volunteerLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!volunteerFound ){
                    DatabaseReference DonorDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(key);
                    DonorDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (!volunteerFound) {
                                    volunteerFound = true;
                                    volunteerFoundID = dataSnapshot.getKey();

                                    DatabaseReference volunteerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(volunteerFoundID).child("donorRequest");
                                    String donorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("donorRequestId", donorId);
                                    volunteerRef.updateChildren(map);

                                    getDriverLocation();
                                    getDriverInfo();

                                    Request.setText("Looking for Volunteer Location..");
                                    return;
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!volunteerFound)
                {
                    radius++;
                    getClosestVolunteer();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker VolunteerMarker;
    private DatabaseReference volunteerLocationRef;
    private ValueEventListener volunteerLocationRefListener;
    private void getDriverLocation(){
        volunteerLocationRef = FirebaseDatabase.getInstance().getReference().child("volunteersWorking").child(volunteerFoundID).child("l");
        volunteerLocationRefListener = volunteerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng volunteerLatLng = new LatLng(locationLat,locationLng);
                    if(VolunteerMarker != null){
                        VolunteerMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(volunteerLatLng.latitude);
                    loc2.setLongitude(volunteerLatLng.longitude);

                    VolunteerMarker = mMap.addMarker(new MarkerOptions().position(volunteerLatLng).title("your volunteer"));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getDriverInfo(){
        VolunteerInfo.setVisibility(View.VISIBLE);
        DatabaseReference DonorDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Volunteers").child(volunteerFoundID);
        DonorDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        VolunteerName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        VolunteerPhone.setText(dataSnapshot.child("phone").getValue().toString());
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationRequest = new LocationRequest();
        LocationRequest.setInterval(1000);
        LocationRequest.setFastestInterval(1000);
        LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        FusedLocationClient.requestLocationUpdates(LocationRequest, LocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    LocationCallback LocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    LastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                }
            }
        }
    };



    private void checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(DMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(DMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }


}

