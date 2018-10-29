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
import android.location.Criteria;
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

import com.comp313.R;
import com.comp313.adapters.InfoWinAdapter;
import com.comp313.dataaccess.GetNearbyPlacesData;
import com.comp313.helpers.VariablesGlobal;
import com.comp313.views.MenuDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

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
    Object[] dataTransfer = new Object[2];
    GetNearbyPlacesData getNearbyPlacesData;
    //endregion

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 3;
    EditText positionText;

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
        positionText = findViewById(R.id.positionText);

        // click listeners
        positionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSearchIntent(PLACE_AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        findViewById(R.id.btnMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuDialog dialog = MenuDialog.getInstance(MapsActivity.this);
                dialog.show(MapsActivity.this.getSupportFragmentManager(), "Show menu");
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        displayCurrentLocation(mMap);

        mMap.setInfoWindowAdapter(infoWinAdapter);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent i = new Intent(getApplicationContext(), BookingDetailsActivity.class);
                i.putExtra("infoWinTitle", marker.getTitle());//address included here!!
                i.putExtra("infoWinTitle", marker.getTitle());//Clinic name included here!!
                i.putExtra("address", marker.getSnippet());//address included here!!
                i.putExtra("timing", "09:00 AM - 05:00 PM");

                startActivity(i);
            }
        });
    }

    private void displayCurrentLocation(GoogleMap mMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            //alternate to using : LatLng myLoc = new LatLng(43.784030, -79.233090);
            Location loc = new Location("dummyprovider");
            loc.setLatitude(43.784030);
            loc.setLongitude(-79.233090);
            onLocationChanged(loc);
            return;
        }

        mMap.setMyLocationEnabled(true);//https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap#setMyLocationEnabled(boolean)
        mMap.getUiSettings().setCompassEnabled(true);//https://developers.google.com/android/reference/com/google/android/gms/maps/UiSettings.html#isCompassEnabled()
        mMap.getUiSettings().setZoomControlsEnabled(true);//https://developers.google.com/android/reference/com/google/android/gms/maps/UiSettings.html#setZoomControlsEnabled(boolean)
        mMap.getUiSettings().setMyLocationButtonEnabled(false);//https://developers.google.com/android/reference/com/google/android/gms/maps/UiSettings.html#setMyLocationButtonEnabled(boolean)
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();

        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null) {
            onLocationChanged(location);
        } else {
            //alternate to using : LatLng myLoc = new LatLng(43.784030, -79.233090);
            Location loc = new Location("dummyprovider");
            loc.setLatitude(43.784030);
            loc.setLongitude(-79.233090);
            onLocationChanged(loc);
        }


        locationManager.requestLocationUpdates(bestProvider, 1800000, 0, this);//onLocationChanged(Location) method will be called for each location update

    }

    private boolean isGooglePlayServicesAvailable() {
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

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        showNearbyClinics();

        /*LatLng currentLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //
        mMap.setInfoWindowAdapter(infoWinAdapter);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent i = new Intent(getApplicationContext(), BookingDetailsActivity.class);
                i.putExtra("infoWinTitle", marker.getTitle());//address included here!!
                i.putExtra("infoWinTitle", marker.getTitle());//Clinic name included here!!
                i.putExtra("address", marker.getSnippet());//address included here!!
                i.putExtra("timing", "09:00 AM - 05:00 PM");

                startActivity(i);
            }
        });*/

    }

    private void launchSearchIntent(int code) {
        try {

            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setBoundsBias(new LatLngBounds(
                                    new LatLng(43.176327, -80.010487),
                                    new LatLng(44.043365, -78.773152)))
                            .build(this);
            startActivityForResult(intent, code);
        } catch (GooglePlayServicesRepairableException e) {

        } catch (GooglePlayServicesNotAvailableException e) {

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    private void isLocPermissionGiven() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_REQUEST);
    }

    private void isGpsOn() {
        try {
            int off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (off == 0) {
                onProviderDisabled(LocationManager.GPS_PROVIDER);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

    }

    //GPS activation - watch for GPS being disabled
    @Override
    public void onProviderDisabled(String provider) {

        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            showGPSDiabledDialog();
        }
    }

    private void showGPSDiabledDialog() {
        Log.d(TAG, "howGPSDiabledDialog() ===>>> entered ");
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle("GPS disabled !").setMessage("GPS is disabled, in order to use the application properly you need to enable GPS of your device")
                .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_ENABLE_REQUEST);
                    }
                })
                .setNegativeButton("No, just exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MapsActivity.this, "You won't be able to see nearby clinics", Toast.LENGTH_SHORT).show();
                    }
                });
        Log.d(TAG, "howGPSDiabledDialog() ===>>> btns done ");

        mGPSDialog = dlgBuilder.create();

        Log.d(TAG, "howGPSDiabledDialog() ===>>> going to dosplay dlg ");
        mGPSDialog.show();
        Log.d(TAG, "howGPSDiabledDialog() ===>>> dlg showing");
    }

    /*private void showHospital() {
        getNearbyPlacesData = new GetNearbyPlacesData(this);//this sends URL request for addresses on .execute() later
//        dataTransfer = new Object[2];//will hold 2 objs

        //
//        switch (v.getId())
//        {
//            case R.id.B_search:
//                EditText tf_location =  findViewById(R.id.TF_location);
//                String location = tf_location.getText().toString();
//                List<Address> addressList;

//                if(!location.equals(""))
//                {
                    *//*Geocoder geocoder = new Geocoder(this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);

                        if(addressList != null)
                        {
                            for(int i = 0;i<addressList.size();i++)
                            {
                                //Placing the initial marker
                                latitude = addressList.get(i).getLatitude();
                                longitude = addressList.get(i).getLongitude();

                                LatLng latLng = new LatLng(addressList.get(i).getLatitude() , addressList.get(i).getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(location);
                                markerOptions.snippet(addressList.get(i).getFeatureName() + " " + addressList.get(i).getThoroughfare() +" "+ addressList.get(i).getLocality() +" "+ addressList.get(i).getAdminArea()+" "+addressList.get(i).getCountryName());
                                mMap.addMarker(markerOptions);
                                title = addressList.get(i).getFeatureName();
                                markerOptions.title(location);

                                address = addressList.get(i).getThoroughfare() + " " + addressList.get(i).getLocality() + " " + addressList.get(i).getAdminArea() + " " + addressList.get(i).getCountryName();

                                mMap.setInfoWindowAdapter(infoWinAdapter);
                                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker)
                                    {
                                        Intent i = new Intent(getApplicationContext(), BookingDetailsActivity.class);
                                        i.putExtra("infoWinTitle", marker.getTitle());//address included here!!
                                        i.putExtra("address",address);//address included here!!
                                        i.putExtra("timing", "09:00 AM - 05:00 PM");//address included here!!

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
                    }*//*
//                }
//                showNearbyClinics();
//                break;
//             case R.id.B_hopistals:
        mMap.clear();
        String hospital = "hospital";
        String url = getUrl(latitude, longitude, hospital);
        dataTransfer[0] = mMap;
        dataTransfer[1] = url = "https://drappdb.firebaseio.com/MockClinics.json?auth=" + VariablesGlobal.KeyToAccessFirebaseDB;//Instead of Google, get hardCoded addresses from Firebase

        getNearbyPlacesData.execute(dataTransfer);//AsyncTask.execute();

        Toast.makeText(this, "Showing nearby hospitals", Toast.LENGTH_LONG).show();
//                break;
*//*
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

                break;*//*
//        }
    }*/

    private void showNearbyClinics() {
        mMap.clear();
        String hospital = "hospital";
        String url = getUrl(latitude, longitude, hospital);
        dataTransfer = new Object[2];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url = "https://drappdb.firebaseio.com/MockClinics.json?auth=" + VariablesGlobal.KeyToAccessFirebaseDB;//Instead of Google, get hardCoded addresses from Firebase

        //this sends URL request for addresses
        getNearbyPlacesData = new GetNearbyPlacesData(this);
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

        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GPS_ENABLE_REQUEST) {
            if (locationManager != null) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDiabledDialog();
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                positionText.setText(place.getName() + " - " + place.getAddress());
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;

                Location loc = new Location(place.getName().toString());
                loc.setLatitude(latitude);
                loc.setLongitude(longitude);
                onLocationChanged(loc);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("TAG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACCESS_FINE_LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    } else {
                        Toast.makeText(MapsActivity.this, "You won't be able to see nearby clinics", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        }
    }

    /*String title = "";
    String address="";
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.menuoptions, menu);
        inflator.inflate(R.menu.right_menu, menu);
        getNearbyPlacesData = new GetNearbyPlacesData(this);
        dataTransfer = new Object[2];
        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);//it's magnifying glass icon, which is an <item> of menuoptions.xml
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print
                //region >>>This region could be commented out as long as we are using hard-coded addresses from Firebase.
                Toast.makeText(getApplicationContext(), "Here" + query, Toast.LENGTH_LONG).show();
                String location = query;
                final List<Address> addressList;

                if (!location.equals("")) {
                    Geocoder geocoder = new Geocoder(getApplicationContext());

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);

                        if (addressList != null) {
                            for (int i = 0; i < addressList.size(); i++) {
                                latitude = addressList.get(i).getLatitude();
                                longitude = addressList.get(i).getLongitude();

                                LatLng latLng = new LatLng(addressList.get(i).getLatitude(), addressList.get(i).getLongitude());
                                final MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(location);
                                markerOptions.snippet(addressList.get(i).getFeatureName() + " " + addressList.get(i).getThoroughfare() + " " + addressList.get(i).getLocality() + " " + addressList.get(i).getAdminArea() + " " + addressList.get(i).getCountryName());
                                mMap.addMarker(markerOptions);
                                title = addressList.get(i).getFeatureName();
                                address = addressList.get(i).getThoroughfare() + " " + addressList.get(i).getLocality() + " " + addressList.get(i).getAdminArea() + " " + addressList.get(i).getCountryName();
                                mMap.setInfoWindowAdapter(infoWinAdapter);
                                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker) {
                                        Intent i = new Intent(getApplicationContext(), BookingDetailsActivity.class);
                                        i.putExtra("infoWinTitle",markerOptions.getTitle());//address included here!!
                                        i.putExtra("address",address);//address included here!!
                                        i.putExtra("timing", "09:00 AM - 05:00 PM");//address included here!!
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
                //endregion
                showNearbyClinics();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;    }*/
}