package comp231.drbooking;
/*
 * By: David Tyler
 * Purpose: Dashboard for admin
 */
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class AdminDashboard extends BaseActivity implements ICallBackFromDbAdapter {
    Object[] paramsApiUri;
    EditText etUserName;
    String stUserName;
    DbAdapter dbAdapter;
    ListView lvUserList;

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
        dbAdapter = new DbAdapter(AdminDashboard.this, new AdminDashboard());

        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/searchUserByName/" + stUserName;
        paramsApiUri[1] = "";
        paramsApiUri[2] = "GET";
        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);
    }

    @Override
    public void onResponseFromServer(String result, Context ctx)
    {

        if (!AdminDashboard.this.isFinishing()) {
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

            List<Model_User> allUserList = new LinkedList<Model_User>();

            Model_User usr;

            for (int i = 0; i < userJsonArray.length(); i++) {
                JSONObject j = userJsonArray.getJSONObject(i);

                usr = new Model_User();
                usr.loginName = j.getString("loginName");
                usr.nameOfUser = j.getString("nameOfUser");
                usr.Id_User = j.getInt("Id_User");

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

    public void onNewUserClick(View view)
    {
        i = new Intent(this, NewUserRegister.class);
        startActivity(i);
    }
}
