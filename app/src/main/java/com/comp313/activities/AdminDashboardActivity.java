package com.comp313.activities;
/*
 * By: Shafiq
 * Purpose: Dashboard for admin
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.comp313.R;
import com.comp313.adapters.ICallBackFromDbAdapter;
import com.comp313.adapters.User_Adapter;
import com.comp313.dataaccess.DbAdapter;
import com.comp313.dataaccess.FBDB;
import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.Booking;
import com.comp313.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;




public class AdminDashboardActivity extends BaseActivity implements ICallBackFromDbAdapter {
    //region >>> Vars
    Object[] paramsApiUri;
    EditText etUserName;
    String stUserName;
    DbAdapter dbAdapter;
    ListView lvUserList;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        paramsApiUri = new Object[3];
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        onSearchClick(null);
    }

    public void onSearchClick(View v)
    {
        etUserName = (EditText) findViewById(R.id.etUserName);
        stUserName = etUserName.getText().toString();
        if(stUserName.trim().isEmpty())
        {
            return;
        }

        new FBDB(this,this).searchUserByName(stUserName);

        /*dbAdapter = new DbAdapter(this, new AdminDashboardActivity());

        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/searchUserByName/" + stUserName;
        paramsApiUri[1] = "";
        paramsApiUri[2] = "GET";
        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);*/
    }

    @Override
    public void onResponseFromServer(ArrayList<User> allUserList, Context ctx)//new Firebase implementation
    {
        lvUserList = (ListView) ((Activity) ctx).findViewById(R.id.lvUserList);
        User_Adapter adapter = new User_Adapter((Activity) ctx, R.layout.eachuser, allUserList);
        lvUserList.setAdapter(adapter);//listAllAppV ref fetched in onCreate becomes NULL in this callBk!!! So get a fresh ref!
    }

    @Override
    public void onResponseFromServer(String result, Context ctx)//OLD Azure implementation
    {

        if (!this.isFinishing()) {
            Toast.makeText(ctx, "Call Back successful", Toast.LENGTH_SHORT).show();
        }
        Log.e("Call Back Success", "==== >>>>> Call Back Success");


        //extract Array of Appoints from json-str
        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObj = jsonArray.getJSONObject(0);
            String userJsonArrayStr = jsonObj.getString("UsersFound");
            JSONArray userJsonArray = new JSONArray(userJsonArrayStr);
            String allUserJsonObj = userJsonArray.getString(0);

            List<User> allUserList = new LinkedList<>();

            User usr;

            for (int i = 0; i < userJsonArray.length(); i++) {
                JSONObject j = userJsonArray.getJSONObject(i);

                usr = new User();
                usr.setLoginName(j.getString("loginName"));
                usr.setNameOfUser(j.getString("nameOfUser"));
                //usr.setId_User(j.getInt("Id_User"));//changed int to String in Model "User" bcoz FB Keys r strings

                allUserList.add(usr);
            }

            lvUserList = (ListView) ((Activity) ctx).findViewById(R.id.lvUserList);
            User_Adapter adapter = new User_Adapter((Activity) ctx, R.layout.eachuser, allUserList);

            lvUserList.setAdapter(adapter);//listAllAppV ref fetched in onCreate becomes NULL in this callBk!!! So get a fresh ref!
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponseFromServer(List<Booking> allBookings, Context ctx) {

    }



    public void onNewUserClick(View view)
    {
        i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }
}
