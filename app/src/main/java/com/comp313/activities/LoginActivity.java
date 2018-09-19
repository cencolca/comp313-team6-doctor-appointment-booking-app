package com.comp313.activities;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: User can login or register
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.comp313.dataaccess.DbAdapter;
import com.comp313.helpers.AESCrypt;
import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.User;
import com.google.gson.Gson;

import comp231.drbooking.R;

public class LoginActivity extends BaseActivity
{

    //region >>> Variables
    EditText uNameView, uPassView;
    SharedPreferences prefs;
    Intent intent;
    String formData;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login or Create New Account");
        getSharedPreferences("prefs",0).edit().putString("Id_User", "").putString("role", "").commit();
        //get references
        uNameView = (EditText) findViewById(R.id.txtLoginName);
        uPassView = (EditText) findViewById(R.id.txtLoginPass);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getSharedPreferences("prefs",0).edit().putString("Id_User", "").putString("role", "").commit();

    }

    //Login btn clk
    public void clk_Login(View view)
    {
        String testing = uPassView.getText().toString();

        //get form data into class
        User uModel = new User();
        uModel.setLoginName(uNameView.getText().toString());
        //encrypt pw
        try {
            uModel.setPw(AESCrypt.encrypt(uPassView.getText().toString()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        formData = gson.toJson(uModel);


        //region (Step-1)Send Object[ApiUri , params] to AsyncTask to read DB to verify login+pw
            //init AsyncTack class
            DbAdapter dbAdapter = new DbAdapter(this);

            //create API's URI
            Object paramsApiUri[] = new Object[3];//[uri , form-data , Http-Method e.g POST ]

        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/login";
        paramsApiUri[1] = formData;
            paramsApiUri[2] = "POST";


        //pass args to AsyncTask to read db
            dbAdapter.execute(paramsApiUri);
    }

    //get PlacePicker returned data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

    }


    //Create a new user account
    public void clk_NewUserRegister(View view)
    {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }
}
