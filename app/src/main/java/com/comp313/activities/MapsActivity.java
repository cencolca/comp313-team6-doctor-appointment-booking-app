package com.comp313.activities;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Displays the map. If GPS is on then Map centers itself.
 * User can search nearby clinics on this activity.
 */


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.comp313.adapters.InfoWinAdapter;
import com.comp313.dataaccess.GetNearbyPlacesData;
import com.comp313.helpers.VariablesGlobal;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


import com.comp313.R;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, LocationListener {

    //region >>> Vars
    int PROXIMITY_RADIUS = 2000, GPS_ENABLE_REQUEST = 1, ACCESS_FINE_LOCATION_REQUEST = 2;
    double longitude, latitude;
    private GoogleMap mMap;
    private static final String TAG = "CurrentLocation";
    protected LocationManager locationManager;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    //Bitmap bmp;
    InfoWinAdapter infoWinAdapter;
    public static MapsActivity instance = null;
    AlertDialog mGPSDialog;
    Object[] dataTransfer;
    GetNearbyPlacesData getNearbyPlacesData;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        //chk if service available
        if (!isGooglePlayServicesAvailable()) {
            return;
        }

        isLocPermissionGiven();
        isGpsOn();

        //inflate layout
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //-----------custom Info Window----------
        infoWinAdapter = new InfoWinAdapter(getLayoutInflater()/*, bmp*/);//custom info window adapter

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        displayCurrentLocation(mMap);
    }

    private void displayCurrentLocation(GoogleMap mMap)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null) {
            onLocationChanged(location);

        }

        locationManager.requestLocationUpdates(bestProvider, 1800000, 0, this);

    }

    private boolean isGooglePlayServicesAvailable()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG);
                finish();
            }
            return false;
        }
        return true;
    }

    @Override    public void onLocationChanged(Location location)
    {
         latitude = location.getLatitude();
         longitude = location.getLongitude();

        LatLng currentLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //
        mMap.setInfoWindowAdapter(infoWinAdapter);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker)
            {
                Intent i = new Intent(getApplicationContext(), BookingDetailsActivity.class);
                i.putExtra("infoWinTitle", marker.getTitle());//address included here!!
                startActivity(i);
            }
        });
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    private void isLocPermissionGiven()
    {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST);
    }

    private void isGpsOn()
    {
        try
        {
            int off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if(off == 0)
            {
                onProviderDisabled(LocationManager.GPS_PROVIDER);
            }
        }
        catch (Settings.SettingNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    //GPS activation - watch for GPS being disabled
    @Override
    public void onProviderDisabled(String provider)
    {

        if(provider.equals(LocationManager.GPS_PROVIDER))
        {
           showGPSDiabledDialog();
        }
    }

    private void showGPSDiabledDialog()
    {
        Log.d(TAG, "howGPSDiabledDialog() ===>>> entered ");
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle("GPS disabled !").setMessage("GPS is disabled, in order to use the application properly you need to enable GPS of your device")
                .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) , GPS_ENABLE_REQUEST);
                    }
                })
        .setNegativeButton("No, just exit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                Toast.makeText(MapsActivity.this, "You won't be able to see nearby clinics", Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, "howGPSDiabledDialog() ===>>> btns done ");

        mGPSDialog  = dlgBuilder.create();

        Log.d(TAG, "howGPSDiabledDialog() ===>>> going to dosplay dlg ");
        mGPSDialog.show();
        Log.d(TAG, "howGPSDiabledDialog() ===>>> dlg showing");


    }

    public void onClick(View v)
    {
        getNearbyPlacesData = new GetNearbyPlacesData(this);
        dataTransfer = new Object[2];//will hold 2 objs

        //
        switch (v.getId())
        {
            case R.id.B_search:
                EditText tf_location =  findViewById(R.id.TF_location);
                String location = tf_location.getText().toString();
                List<Address> addressList;

                if(!location.equals(""))
                {
                    Geocoder geocoder = new Geocoder(this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);

                        if(addressList != null)
                        {
                            for(int i = 0;i<addressList.size();i++)
                            {
                                latitude = addressList.get(i).getLatitude();
                                longitude = addressList.get(i).getLongitude();

                                LatLng latLng = new LatLng(addressList.get(i).getLatitude() , addressList.get(i).getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(location);
                                markerOptions.snippet(addressList.get(i).getFeatureName() + " " + addressList.get(i).getThoroughfare() +" "+ addressList.get(i).getLocality() +" "+ addressList.get(i).getAdminArea()+" "+addressList.get(i).getCountryName());
                                mMap.addMarker(markerOptions);

                                mMap.setInfoWindowAdapter(infoWinAdapter);
                                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker)
                                    {
                                        Intent i = new Intent(getApplicationContext(), BookingDetailsActivity.class);
                                        i.putExtra("infoWinTitle", marker.getTitle());//address included here!!
                                        startActivity(i);
                                    }
                                });
                                //----------------------------

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                showNearbyClinics();
                break;
 /*            case R.id.B_hopistals:
                mMap.clear();
                String hospital = "hospital";
                String url = getUrl(latitude, longitude, hospital);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url = "https://drappdb.firebaseio.com/MockClinics.json?auth=" + VariablesGlobal.KeyToAccessFirebaseDB;//Instead of Google, get hardCoded addresses from Firebase

                getNearbyPlacesData.execute(dataTransfer);//AsyncTask.execute();

                Toast.makeText(this, "Showing nearby hospitals", Toast.LENGTH_LONG).show();
                break;

           case R.id.B_restaurants:
                mMap.clear();
                String school = "school";
                url = getUrl(latitude, longitude, school);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);

                Toast.makeText(this, "Showing nearby schools", Toast.LENGTH_LONG).show();

                break;
            case R.id.B_schools:
                mMap.clear();
                String restaurants = "restaurant";
                url = getUrl(latitude, longitude, restaurants);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);

                Toast.makeText(this, "Showing nearby restaurants", Toast.LENGTH_LONG).show();

                break;*/
        }
    }

    private void showNearbyClinics()
    {
        mMap.clear();
        String hospital = "hospital";
        String url = getUrl(latitude, longitude, hospital);
        dataTransfer[0] = mMap;
        dataTransfer[1] = url = "https://drappdb.firebaseio.com/MockClinics.json?auth=" + VariablesGlobal.KeyToAccessFirebaseDB;//Instead of Google, get hardCoded addresses from Firebase

        getNearbyPlacesData.execute(dataTransfer);//AsyncTask.execute();

        Toast.makeText(this, "Showing nearby hospitals", Toast.LENGTH_LONG).show();
    }


    private String getUrl(double latitude, double longitude, String nearbyPlace)//
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyA3WABbO18GPtvg3VTl-TosotiD6Zba5kE");

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == GPS_ENABLE_REQUEST)
        {
            if (locationManager != null)
            {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                showGPSDiabledDialog();
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == ACCESS_FINE_LOCATION_REQUEST)
        {
            for (int i = 0; i < permissions.length; i++)
            {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if(permission.equals(Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    if(grantResult == PackageManager.PERMISSION_GRANTED)
                    {
                    }
                    else
                    {
                        Toast.makeText(MapsActivity.this, "You won't be able to see nearby clinics", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        }
    }
}

