package com.example.ray.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ray.R;
import com.example.ray.databinding.ActivityAdminMainBinding;
import com.example.ray.databinding.ActivityMainBinding;

public class AdminMainActivity extends DrawerbaseActivity2{

    //creating bundle variable

    ActivityAdminMainBinding activityAdminMainBinding;

    //creating button variables
    Button vehiclesBT, driversBT;
    private long backPressed;
    private static  final int TIME_INTERVAL = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        activityAdminMainBinding =   ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(activityAdminMainBinding.getRoot());

        setTitle("Admin Panel");

        //initialising buttons
        vehiclesBT = (Button) findViewById(R.id.vehicles_button);
        driversBT = (Button) findViewById(R.id.drivers_button);

        //setting onclick to buttons

        vehiclesBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //vehicles button takes you to vehicles screen

                Intent i = new Intent(AdminMainActivity.this, AdminVehiclesActivity.class);
                startActivity(i);

            }
        });

        driversBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //drivers button takes you to drivers screen

                Intent i = new Intent(AdminMainActivity.this, AdminDriversActivity.class);
                startActivity(i);

            }
        });

    }

    //action for when the back button on device is pressed
    public void onBackPressed(){

        if(backPressed + TIME_INTERVAL > System.currentTimeMillis()){
            super.onBackPressed();
            finishAffinity();
        }
        else {
            Toast.makeText(this, "Press Back Again To Exit", Toast.LENGTH_SHORT).show();
        }
        backPressed = System.currentTimeMillis();

    }
}