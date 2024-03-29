package com.example.ray.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ray.R;
import com.example.ray.models.vehiclesModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Vehicles extends Fragment implements OnMapReadyCallback {

    //arraylist
    ArrayList<vehiclesModel> AssignedvehiclesList;

    //Variables
    TextView modelTV, plateTV, tv1, tv2, tv3, tv4, tv5;
    ImageView image;

    //Firebase
    DatabaseReference ref;

    //FirebaseDatabase
    FirebaseDatabase db;
    DatabaseReference reference,imageRef,checkRef;

    //Location
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    private static final int REQUEST_CODE = 101;

    //Button
    Button updateLocationButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_vehicles, container, false);

        modelTV = view.findViewById(R.id.modelDescriptionIDFrag);
        plateTV = view.findViewById(R.id.plateNumberIDFrag);
        image = view.findViewById(R.id.ImageID);
        updateLocationButton = view.findViewById(R.id.updateLocation);

        //DB
        db = FirebaseDatabase.getInstance();
        //check if assigned vehicle exists
        checkRef = db.getReference().child("Users").child((FirebaseAuth.getInstance().getCurrentUser()).getUid());
        checkRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Assigned Vehicle").exists()) {

                    view.findViewById(R.id.regular_layout).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.empty_layout).setVisibility(View.GONE);


                } else {

                    view.findViewById(R.id.regular_layout).setVisibility(View.GONE);
                    view.findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        ref = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Assigned Vehicle");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    String modelDescription = (String) dataSnapshot.child("model").getValue();
                    String NumberPlate = (String) dataSnapshot.child("plate").getValue();
                    String vehicleId = (String) dataSnapshot.child("vehicleId").getValue();

                    modelTV.setText(modelDescription);
                    plateTV.setText(NumberPlate);

                    //imageview
                    db = FirebaseDatabase.getInstance();
                    imageRef = db.getReference().child("Vehicles Images").child(vehicleId);
                    imageRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String vehicleImage = (String) snapshot.child("imageUri").getValue();

                            Picasso.get().load(vehicleImage).into(image);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        //Location, initializing fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        fetchLastLocation();

        //initialize textviews
        tv1 = view.findViewById(R.id.tv1);
        tv2 = view.findViewById(R.id.tv2);
        tv3 = view.findViewById(R.id.tv3);
        tv4 = view.findViewById(R.id.tv4);
        tv5 = view.findViewById(R.id.tv5);

        //check permission location
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission is granted

            getLocation();

        } else {
            //when permission is not granted
            ActivityCompat.requestPermissions((Activity) getContext(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }


        return view;
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    currentLocation = location;
//                    Toast.makeText(getContext(), currentLocation.getLatitude()
//                            +""+currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment)getChildFragmentManager()
                            .findFragmentById(R.id.google_map);
                    supportMapFragment.getMapAsync(Vehicles.this);

                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {


        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //initialize location
                Location location = task.getResult();
                if(location != null) {
                    //Initialize geoCoder
                    Geocoder geocoder = new Geocoder(getContext(),
                            Locale.getDefault());
                    //Initialize address list
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        //set latitude on TextView
                        tv1.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Latitude:</b><br></font>"
                                        + addresses.get(0).getLatitude()
                        ));
                        //set longitude on TextView
                        tv2.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Longitude:</b><br></font>"
                                        + addresses.get(0).getLongitude()
                        ));
                        //set country name
                        tv3.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Country Name:</b><br></font>"
                                        + addresses.get(0).getCountryName()
                        ));
                        //set locality
                        tv4.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Locality:</b><br></font>"
                                        + addresses.get(0).getLocality()
                        ));
                        //set address
                        tv5.setText(Html.fromHtml(
                                "<font color = '#6200EE'><b>Address:</b><br></font>"
                                        + addresses.get(0).getAddressLine(0)
                        ));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                .title("I am Here");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        googleMap.addMarker(markerOptions);


        //Add location to database

        db = FirebaseDatabase.getInstance();
        reference = db.getReference().child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Current Location");
        Double lat = currentLocation.getLatitude();
        Double longi = currentLocation.getLongitude();

        Map<String, Object> item = new HashMap<>();
        item.put("latitude", lat);
        item.put("longitude", longi);
        reference.updateChildren(item);

        //use button to update
        updateLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                db = FirebaseDatabase.getInstance();
                reference = db.getReference().child("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .child("Current Location");
                Double lat = currentLocation.getLatitude();
                Double longi = currentLocation.getLongitude();

                Map<String, Object> item = new HashMap<>();
                item.put("latitude", lat);
                item.put("longitude", longi);
                reference.setValue(item);

            }
        });



//        reference.setValue(latLng).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
////                    Toast.makeText(getContext(), "Location Saved", Toast.LENGTH_SHORT).show();
//                }else{
////                    Toast.makeText(getContext(), "Location Not Saved", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }
                break;
        }
    }


}