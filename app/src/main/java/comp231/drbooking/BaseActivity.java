package comp231.drbooking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
                i = new Intent(this, Login.class);
                startActivity(i);
                finish();
                break;
            case R.id.menuDashboard:
                //only if logged-in then show Dashboard
                if(roleStr.equals("3"))//admin is logged in
                {
                    i = new Intent(this, AdminDashboard.class);

                }
                else
                {
                    i = new Intent(this, Dashboard.class);
                }
                startActivity(i);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
