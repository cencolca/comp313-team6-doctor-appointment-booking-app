package com.comp313.dataaccess;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Display nearby hospitals in Map.
 * It's AsyncTask & uses DownloadUri class to send HTTP request. And uses DataParser to parse JSON response from that call.
 */
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.comp313.helpers.BitmapDescriptorFromVector;
import com.comp313.helpers.DataParser;
import com.comp313.helpers.DownloadUrl;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import com.comp313.R;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String>//args,progress,result
{
    //Class Variables
    String googlePlacesData, url;
    GoogleMap mMap;
    Context ctx;

    public GetNearbyPlacesData(Context ctx)
    {
        this.ctx = ctx;
    }

    @Override
    protected String doInBackground(Object... objects)
    {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();

        try
        {
            googlePlacesData = downloadUrl.readUrl(url);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s)
    {
        //read hard-coded JSON bcoz API is severely limited by Google
     /*   try
        {
            InputStream is  = ctx.getResources().openRawResource(R.raw.nearbyclinics);
            byte[] buffer = new byte[is.available()];
            while(is.read(buffer) != -1);
            s = new String(buffer);
        }
        catch (IOException e)
        {
            Log.e("raw file reading:", ""+e.toString());
        }*/

        //
        List<HashMap<String,String>> nearbyPlacesList = null;
        DataParser parser = new DataParser();
        nearbyPlacesList = parser.parse(s);//JSON parsed here
        showNearbyPlaces(nearbyPlacesList);
    }

    //Add markers to map to show ALL places - move camera to marker
    private void showNearbyPlaces(List<HashMap<String,String>> nearbyPlacesList)
    {
        BitmapDescriptor marker = BitmapDescriptorFromVector.bitmapDescriptorFromVector(ctx, R.drawable.ic_location_on_black_24dp);
        for (int i = 0; i < nearbyPlacesList.size(); i++)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String,String> googlePlace = nearbyPlacesList.get(i);
            String placeName = googlePlace.get("placeName");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble(googlePlace.get("latitude"));
            double lng = Double.parseDouble(googlePlace.get("longitude"));

            LatLng latLng  = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            markerOptions.icon(marker);

            mMap.addMarker(markerOptions);

            //move Camera to:
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        }
    }


}
