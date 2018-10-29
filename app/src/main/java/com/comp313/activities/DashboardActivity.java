package com.comp313.activities;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Main portal for user to see existing bookings or to create a new booking
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.comp313.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;




import static com.google.android.gms.location.places.Place.TYPE_DOCTOR;
import static com.google.android.gms.location.places.Place.TYPE_HOSPITAL;

public class DashboardActivity extends BaseActivity {

    private static final String TAG = "Catch Block says: ";
    //region >>> Class Variables
    int PLACE_PICKER_REQUEST = 1;
    double longitude,latitude;
    Intent i;
    public static DashboardActivity instance;
    String userIdStr, roleStr;
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        instance = this;
    }


    public void clk_newAppMap(View v)
    {
        //go to MapActivity
        i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void clk_newAppPlacePicker(View view)
    {
        getCurrentLoc();

        //top-left & bottom-right corners of what PlacePicker will display
        //use this site to get coords : https://www.latlong.net
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(43.790272, -79.234127))
                .include(new LatLng(43.759768, -79.225224))
                .build();

        //filter places e.g. clinics etc
        PlaceFilter placeFilter = new PlaceFilter();
        placeFilter.equals(TYPE_DOCTOR | TYPE_HOSPITAL);//int Place_IDs permitted : https://developers.google.com/android/reference/com/google/android/gms/location/places/Place

        PlacePicker.IntentBuilder iBuilder = new PlacePicker.IntentBuilder(); //compile 'com.google.android.gms:play-services-places:9.2.0'

        try
        {
            i = iBuilder
                    .setLatLngBounds(bounds)
                    .build(DashboardActivity.this);


            startActivityForResult(i, PLACE_PICKER_REQUEST);
        }
        catch (GooglePlayServicesRepairableException e)
        {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e)
        {
            e.printStackTrace();
        }
        //endregion
    }

    public void clk_allAppointments(View view)
    {
        i = new Intent(this, Bookings_AllActivity.class);
        startActivity(i);
    }


    //
    private void getCurrentLoc() //grant permissions for "Location" from phone/emulator
    {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ////if no permission then assign some default location like downtown or Centennial college
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        //location obj is NULL in newly installed app bcoz no "Last Known Loc" there. So turn Google Maps inbuilt app & click target icon to get at least one loc in memory
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        Toast.makeText(this, longitude +"---"+ latitude, Toast.LENGTH_LONG).show();
    }

    //get PlacePicker returned data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == PLACE_PICKER_REQUEST )
        {
            if(resultCode==RESULT_OK)
            {
                Place place = PlacePicker.getPlace(this, data);
                String clinicAddress = String.format("Place: %s", place.getAddress());
                String clinicName = String.format("Place: %s", place.getName());

                //display selected address
                Toast.makeText(this, "Name: " + clinicName
                + "\nAddress: " + clinicAddress, Toast.LENGTH_LONG).show();

//                getSupportActionBar().setTitle(clinicAddress);


            }


        }
    }

    public void clk_Settings(View view)
    {
        //get ID & role-code of the logged-in user
        userIdStr = getSharedPreferences("prefs", 0).getString("Id_User", "1");
        roleStr = getSharedPreferences("prefs", 0).getString("role", "");

        i = new Intent(this, SettingsActivity.class);
        i.putExtra("Id_User", userIdStr);
        startActivity(i);
    }

    public void clk_Testing(View view)
    {
        i = new Intent(this, FindClinicActivity.class);
        startActivity(i);
    }
}
