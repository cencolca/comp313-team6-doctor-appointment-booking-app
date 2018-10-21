package com.comp313.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.comp313.R;


/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Base class for all activities that will show a menuoptions (three dots) on top right corner. e.g. to logout etc
 */
public class BaseActivity extends AppCompatActivity
{
    Intent i;
    String userIdStr, roleStr;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return displayThreeDotsOrSearchBar(menu);
    }

    public boolean displayThreeDotsOrSearchBar(Menu menu)
    {
        userIdStr = getSharedPreferences("prefs", 0).getString("Id_User", "1");
        roleStr = getSharedPreferences("prefs", 0).getString("role", "");

        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.menuoptions, menu);
        //alter bw "Login / Register" & "Logout" on menu item; based on whether user is loggedin or not
        MenuItem logInOutItem = menu.findItem(R.id.menuLogout);
        logInOutItem.setTitle(getSharedPreferences("prefs",0).getString("Id_User", "").equals("")?"Login / Register":"Logout");

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuLogout:
                getSharedPreferences("prefs",0).edit().putString("Id_User", "").putString("role", "").commit();



                //taken back to Login screen
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.menuDashboard:
                //only if logged-in then show Dashboard
                if(roleStr.equals("3"))//admin is logged in
                {
                    i = new Intent(this, AdminDashboardActivity.class);

                }
                else
                {
                    i = new Intent(this, DashboardActivity.class);
                }
                startActivity(i);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
