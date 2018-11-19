package com.comp313.activities;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: User can login or register
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.comp313.R;

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
        getSharedPreferences("prefs",0).edit().putString("Id_User", "").putString("UserWhoCreateApp","").putString("role", "").commit();
        //get references
        uNameView = findViewById(R.id.txtLoginName);
        uPassView = findViewById(R.id.txtLoginPass);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getSharedPreferences("prefs",0).edit().putString("Id_User", "").putString("UserWhoCreateApp","").putString("role", "").commit();

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
//            uModel.setPw(AESCrypt.encrypt(uPassView.getText().toString()));
            uModel.setPw(uPassView.getText().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        loginUser(uModel);


//
//        Gson gson = new Gson();
//        formData = gson.toJson(uModel);
//
//
//        //region (Step-1)Send Object[ApiUri , params] to AsyncTask to read DB to verify login+pw
//            //init AsyncTack class
//            DbAdapter dbAdapter = new DbAdapter(this);
//
//            //create API's URI
//            Object paramsApiUri[] = new Object[3];//[uri , form-data , Http-Method e.g POST ]
//
//        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/login";
//        paramsApiUri[1] = formData;
//            paramsApiUri[2] = "POST";
//
//
//        //pass args to AsyncTask to read db
//            dbAdapter.execute(paramsApiUri);
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


    public void loginUser(final User currUser)
    {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        Query query = myRef.child("Users").orderByChild("loginName").equalTo(currUser.getLoginName());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChildren())
                    {
                        try {
                            Context ctx = LoginActivity.this;

                            DataSnapshot snap = dataSnapshot.getChildren().iterator().next();

                            User dbUser = snap.getValue(User.class);
                            if(dbUser.getPw().equals(currUser.getPw()))
                            {
//                                get shared preference
                                SharedPreferences pref = ctx.getSharedPreferences("prefs", 0);


                                String userId = snap.getKey();

                                // store userID to sharedPref
                                pref.edit().putString("Id_User", userId).apply();
                                // store name of user who is creating appointment
                                pref.edit().putString("UserWhoCreateApp",dbUser.getNameOfUser()).apply();

                                // set role to 0 (patient)
                                pref.edit().putString("role", dbUser.getRole()).apply();

                                //store name of user -> needed for method getAllAppoints_Dr in FBDB
                                pref.edit().putString("Name_of_User", dbUser.getNameOfUser()).apply();


                                Toast.makeText(ctx, "Login successful", Toast.LENGTH_LONG).show();
                                if (dbUser.getRole().equals("3"))
                                {
                                    i = new Intent(ctx, AdminDashboardActivity.class);
                                    ctx.startActivity(i);
                                }
                                else
                                {
                                    i = new Intent(ctx, DashboardActivity.class);
                                    ctx.startActivity(i);
                                }
                            }
                            else
                            {
                                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch(Exception e)
                        {
                            Log.e("LoginError", e.getMessage());
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Username/password not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

