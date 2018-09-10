package comp231.drbooking;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Async db read/write operations, in background thread
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DbAdapter extends AsyncTask<Object, Integer, String>//<args,progress,result>
{
    //region Class Variables
    String jsonResponse, url, formData, httpMethod, roleStr;
    Context ctx;
    Intent i;
    SharedPreferences prefs;
    ICallBackFromDbAdapter callBk;
    Gson gson;
    //String[] DrNamesList;
    boolean isGettingDrList = false, isAdmin = false;;
    //endregion


    //constructor # 1
    public DbAdapter(Context ctx)
    {
        this.ctx = ctx;
        gson = new Gson();
        //chk if admin is creating a new user
        roleStr = ctx.getSharedPreferences("prefs", 0).getString("role", "");
        isAdmin = roleStr.equals("3")?true:false;
    }
    //constructor # 2 = pass a callBack fn
    public DbAdapter(Context ctx, ICallBackFromDbAdapter callBk)
    {
        this.ctx = ctx;
        this.callBk = callBk;
        gson = new Gson();
    }

    //constructor # 3 = To pass extra data like array-of-Dr names = Java doesn't support optiona params so we're stuch w overloading constructors
    public DbAdapter(Context ctx, String purpose, ICallBackFromDbAdapter callBk)
    {
        switch (purpose)
        {
            case "GetDrNamesArray":
                isGettingDrList = true;
                break;
        }
        this.ctx = ctx;
        gson = new Gson();
        this.callBk = callBk;
    }

    //read db via API in bg
    @Override
    protected String doInBackground(Object... objects)
    {
        //get URI for API from params
        url = (String)objects[0];
        formData = (String) objects[1];
        httpMethod = (String) objects[2];

        if(httpMethod.equals("POST"))
        {
            SendToUrl sendToUrl  = new SendToUrl();

            try
            {
                jsonResponse = sendToUrl.sendToUrl(url, formData);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return jsonResponse;
        }
        

        //obj to send http request to API
        DownloadUrl downloadUrl = new DownloadUrl();

        //API request
        try
        {
            jsonResponse = (String) downloadUrl.readUrl(url);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //return JSON string returned from API
        return jsonResponse;
    }

    //process the result(JSON) from API(db)
    @Override
    protected void onPostExecute(String s)//JSON string passed
    {
        //region >>> if fetching list of Drs names
        if(isGettingDrList)
        {
            try
            {
                JSONArray jsonArrDrNames = new JSONArray(jsonResponse);
                int len = jsonArrDrNames.length();
                VariablesGlobal.DrNamesList.clear();//clear dummy Dr names like "Un-Known Doctor"
                VariablesGlobal.DrNamesList.add("~~ Please Select a Doctor ~~");
                VariablesGlobal.DrNamesListFiltered.clear();
                VariablesGlobal.DrNamesListFiltered.add("~~ Please Select a Doctor ~~");
                VariablesGlobal.DrProfiles.clear();

                JSONObject jObj;
                Model_DrProfile dr;
                for (int j = 0; j < len; j++)
                {
                    dr = new Model_DrProfile();

                    jObj = jsonArrDrNames.getJSONObject(j);
                    dr.id_doc      =jObj.getInt("id_doc");
                    dr.Id_User     =jObj.getInt("Id_User");
                    dr.name        =jObj.getString("name");
                    dr.phone       =jObj.getString("phone");
                    dr.email       =jObj.getString("email");
                    dr.specialty   =jObj.getString("specialty");

                    VariablesGlobal.DrNamesList.add(dr.name);
                    VariablesGlobal.DrNamesListFiltered.add(dr.name);
                    VariablesGlobal.DrProfiles.add(dr);
                }

                VariablesGlobal.spinAdapter.notifyDataSetChanged();
                callBk.onResponseFromServer(null, ctx);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return;
        }
        //endregion

        //region >>> chk which activity called for API
        switch (ctx.getClass().getSimpleName())
        {
            //Go to Dashboard - on successful login
            case "Login":
            if(jsonResponse.equals("0"))
            {
                Toast.makeText(ctx, jsonResponse + " Login Failed", Toast.LENGTH_LONG).show();
            }
            else if(jsonResponse.equals(""))
            {
                Toast.makeText(ctx, jsonResponse + " No Response From Server!", Toast.LENGTH_LONG).show();

                //------------For testing without login server---
                //go to Dashboard
                i = new Intent(ctx, Dashboard.class);
                ctx.startActivity(i);
                //----------------------------------------------
            }
            else
            {
                Model_User u = gson.fromJson(jsonResponse, Model_User.class);

                Toast.makeText(ctx, "User ID: " + u.Id_User + " Login Successful", Toast.LENGTH_LONG).show();
                //save User_Id
                prefs = ctx.getSharedPreferences("prefs" , 0);
                String UserIdStr = jsonResponse.equals("")?"1":String.valueOf(u.Id_User);
                String roleStr = jsonResponse.equals("")?"1":String.valueOf(u.role);
                prefs.edit().putString("Id_User", UserIdStr).putString("role", roleStr).commit();
                //Go to dashboard
                if(roleStr.equals("3"))
                {
                    i = new Intent(ctx, AdminDashboard.class);
                    ctx.startActivity(i);
                }
                else
                {
                    i = new Intent(ctx, Dashboard.class);
                    ctx.startActivity(i);
                }
            }
            break;
            //
            case "NewUserRegister":
                if(jsonResponse.equals("0"))//user already exists
                {

                    Log.e("Server-NewUser ==>>", jsonResponse);
                    Toast.makeText(ctx.getApplicationContext(), jsonResponse + " Login-Name already exists!", Toast.LENGTH_LONG).show();
                }
                else if(jsonResponse.equals(""))
                {
                    Toast.makeText(ctx, jsonResponse + " No Response From Server!", Toast.LENGTH_LONG).show();

                    //------------For testing without login server---
                    //go to regular Dashboard or admin-dash
                    if(isAdmin)
                        i = new Intent(ctx, AdminDashboard.class);
                    else
                    i = new Intent(ctx, Dashboard.class);
                    ctx.startActivity(i);
                    //----------------------------------------------

                }
                else
                {
                    //go to Dashboard
                    Toast.makeText(ctx, jsonResponse + " User Created", Toast.LENGTH_LONG).show();
                    //go to regular Dashboard or admin-dash
                    if(isAdmin)
                        i = new Intent(ctx, AdminDashboard.class);
                    else
                    i = new Intent(ctx, Dashboard.class);
                    ctx.startActivity(i);
                }
            break;
            //
            case "BookingDetails":
                if(jsonResponse.equals("0"))
                {
                    Toast.makeText(ctx.getApplicationContext(), jsonResponse + " Appointment unavailable, Choose another time!", Toast.LENGTH_LONG).show();
                }
                else if(jsonResponse.equals(""))
                {
                    Toast.makeText(ctx, jsonResponse + " No Response From Server!", Toast.LENGTH_LONG).show();

                    //------------For testing go to ALL bookings if server is not up---
                    //go to Dashboard
                    i = new Intent(ctx, Bookings_All.class);
                    ctx.startActivity(i);
                    ((Activity)ctx).finish();
                    //----------------------------------------------

                }
                else
                {
                    //go to Dashboard
                    Toast.makeText(ctx, jsonResponse + " Appointment Created", Toast.LENGTH_LONG).show();//jsonResponse is Appoint_id
                    i = new Intent(ctx, Bookings_All.class);
                    ctx.startActivity(i);
                }
             break;
            //
            case "Bookings_All":
                callBk.onResponseFromServer(jsonResponse, ctx);
                break;
            //
            case "Settings":
                if(callBk != null)
                {
                    callBk.onResponseFromServer(jsonResponse, ctx);
                }
                else
                {
                    Toast.makeText(ctx, jsonResponse + ctx.getClass().getSimpleName() , Toast.LENGTH_LONG).show();
                }
            break;
            //
            case "AdminDashboard":
                callBk.onResponseFromServer(jsonResponse, ctx);
                break;
            //
            default:
                Toast.makeText(ctx, jsonResponse + ctx.getClass().getSimpleName() , Toast.LENGTH_LONG).show();
                break;
        }
        //endregion
    }
}
