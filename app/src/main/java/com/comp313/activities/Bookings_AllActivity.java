package com.comp313.activities;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Display a list of all bookings for a logged-in user. User can click any one booking to see details of it or to edit/cancel it
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.comp313.adapters.Booking_Adapter;
import com.comp313.adapters.ICallBackFromDbAdapter;
import com.comp313.dataaccess.DbAdapter;
import com.comp313.dataaccess.FBDB;
import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.Booking;
import com.comp313.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.comp313.R;


public class Bookings_AllActivity extends BaseActivity implements ICallBackFromDbAdapter {

    //region Vars
    DbAdapter dbAdapter;
    Gson gson;
    Object[] paramsApiUri;
    ListView listAllAppV;
    String userIdStr, roleStr;
    public static Bookings_AllActivity instance;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings_all);
        instance = this;
        listAllAppV = (ListView) findViewById(R.id.listAllAppoints);
        gson = new Gson();
        paramsApiUri = new Object[3];
        userIdStr = getSharedPreferences("prefs", 0).getString("Id_User", "1");
        roleStr = getSharedPreferences("prefs", 0).getString("role", "");

        switch (roleStr) {
            case "1":
                LoadAllAppoints();
                break;
            case "2":
                LoadAllAppointsForDr();
                break;
            case "":
                break;
        }
        if (BookingDetailsActivity.instance != null) {
            BookingDetailsActivity.instance.finish();
        }

        if (MapsActivity.instance != null) {
            MapsActivity.instance.finish();
        }
    }

    @Override
    public void onResponseFromServer(List<Booking> allBookings, Context ctx) {
        listAllAppV = (ListView) ((Activity) ctx).findViewById(R.id.listAllAppoints);
        Booking_Adapter adapter = new Booking_Adapter((Activity) ctx, R.layout.eachbooking, allBookings);
        listAllAppV.setAdapter(adapter);
    }

    @Override
    public void onResponseFromServer(ArrayList<User> allUsersAdminSearched, Context ctx) {

    }


    @Override
    public void onResponseFromServer(String result, Context ctx) {
        if (!Bookings_AllActivity.this.isFinishing()) {
            Toast.makeText(ctx, "Call Back successful", Toast.LENGTH_SHORT).show();
        }
        Log.e("Call Back Success", "==== >>>>> Call Back Success");

        //extract Array of Appoints from json-str
        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObj = jsonArray.getJSONObject(0);
            String appointsJsonArrayStr = jsonObj.getString("Appointments");
            JSONArray appointsJsonArray = new JSONArray(appointsJsonArrayStr);

            List<Booking> allAppList = new LinkedList<>();

            Booking b;

            for (int i = 0; i < appointsJsonArray.length(); i++) {
                JSONObject j = appointsJsonArray.getJSONObject(i);

                b = new Booking();
                b.setId_Appointment(Integer.parseInt(j.getString("Id_Appointment")));
                b.setId_User("0"/*Integer.parseInt( j.getString("Id_User") )*/);
                b.setId_Doc(Integer.parseInt(j.getString("Id_Doc")));
                b.setClinic(j.getString("Clinic"));
                b.setDoctor(j.getString("Doctor"));
                b.setAppointmentTime(j.getString("AppointmentTime"));
                b.setCreationTime(j.getString("CreationTime"));

                //list for Docs has 1 extra prop "PatientName". List for patients doesn't. See sample JSON fo Docs below:
                if (j.has("PatientName")) {
                    b.setUser(j.getString("PatientName"));//name of patient
                }
                allAppList.add(b);
            }

            listAllAppV = (ListView) ((Activity) ctx).findViewById(R.id.listAllAppoints);
            Booking_Adapter adapter = new Booking_Adapter((Activity) ctx, R.layout.eachbooking, allAppList);

            listAllAppV.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void LoadAllAppoints()
    {
        boolean success = new FBDB(this, this).getAllAppoints_Pateint(userIdStr);
    }

    private void LoadAllAppointsForDr()
    {
        boolean success = new FBDB(this, this).getAllAppoints_Dr(userIdStr);

 /*       dbAdapter = new DbAdapter(Bookings_AllActivity.this, new Bookings_AllActivity());//new Bookings_All() just to give access to DbAdapter to onResponseFromServer()

        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/AppointmentsForDr/" + userIdStr;
        paramsApiUri[1] = "";//formData not needed for this GET req since user_id is appended to URL
        paramsApiUri[2] = "GET";
        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);*/
    }


}
