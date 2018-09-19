package com.comp313.helpers;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: From Google Places response (JSON), extract details of each place
 */
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser
{
    //fn to get one place - is called by fn below it
    private HashMap<String,String>  getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String,String> googlePlaceMap = new HashMap<>();
        String placeName = "-NA-", vicinity = "-NA-", latitude= "",longitude= "",reference= "";

        //extract data from JSON
        try
        {
            if(!googlePlaceJson.isNull("name"))
            {
                    placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity"))
            {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJson.getString("reference");

            //put data into HashMap
            googlePlaceMap.put("placeName", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("latitude", latitude);
            googlePlaceMap.put("longitude", longitude);
            googlePlaceMap.put("reference", reference);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return googlePlaceMap;//HashMap
    }

    //fn to get all places from JSON array - it gets called by fn below it
    private List<HashMap<String,String>> getPlaces (JSONArray jsonArray)//pleural 'places'
    {
        int count = jsonArray.length();
        List<HashMap<String,String>> placesList = new ArrayList<>();
        HashMap<String,String> placeMap = null;

        //fetch all places one by one from JSONArray & put in List<> placesList
        for(int i = 0; i < count; i++)
        {
            try {
                placeMap = getPlace( (JSONObject)jsonArray.get(i) );//cast ea item in json-array into a json-obj
                placesList.add(placeMap);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    //fn called from outside hence 'public' - receives string of json format
    public List<HashMap<String,String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try
        {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }
}
