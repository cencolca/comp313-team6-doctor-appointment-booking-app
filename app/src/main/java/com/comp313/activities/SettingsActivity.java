package com.comp313.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.comp313.adapters.ICallBackFromDbAdapter;
import com.comp313.dataaccess.DbAdapter;
import com.comp313.dataaccess.FBDB;
import com.comp313.helpers.AESCrypt;
import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.Booking;
import com.comp313.models.User;
import com.google.gson.Gson;

import com.comp313.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseActivity implements ICallBackFromDbAdapter {

    //region Class Variables
    String userIdStr, roleStr, formData, uName, uPass,fName, lName, add;
    DbAdapter dbAdapter;
    User uModel;
    SharedPreferences pref;
    Gson gson;
    Object[] paramsApiUri;
    EditText loginNameV, fNameV, lNameV, addressV, emailV, phoneV, pwV, Id_UserV;
    Button btnDeleteUserV;

    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        super.setupToolbar("Update User Profile");

        //get refs to EditText views
        loginNameV = (EditText)findViewById(R.id.txtEditUserName);
        fNameV = (EditText)findViewById(R.id.txtEditFName);
        addressV = (EditText)findViewById(R.id.txtEditAdd);
        emailV = (EditText)findViewById(R.id.txtEditEmail);
        phoneV = (EditText)findViewById(R.id.txtEditPhone);
        Id_UserV = (EditText)findViewById(R.id.txtEditId);
        //disable the update btn & make del btn invisi
        ((Button)findViewById(R.id.btnUpdateUser)).setEnabled(false);
        ((Button)findViewById(R.id.btnDeleteUser)).setVisibility(View.INVISIBLE);

        //get role of logedin person
        roleStr = getSharedPreferences("prefs", 0).getString("role", "");

        //init vars
        paramsApiUri = new Object[3];
        gson = new Gson();
        uModel = new User();

        //Either logged-in user's id/role passed here -or- ADMIN passes id/role of the user he's searching for
        userIdStr = getIntent().getStringExtra("Id_User");
    }

    //Get & display details of a user from DB via API
    public void btnClk_EditUserProfile(View view)
    {
        //chk if user is logged in:
        String logedinUserIdStr = getSharedPreferences("prefs",0).getString("Id_User", "");
        if(logedinUserIdStr.equals(""))
        {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }
        //
        boolean success = new FBDB(this, this).getUserById(userIdStr);

     /*   dbAdapter = new DbAdapter(this, new SettingsActivity());
        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/getuserdetail/" + userIdStr;
        paramsApiUri[1] = "";
        paramsApiUri[2] = "GET";
        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);*/


    }

    public void btnClk_UpdateUser(View view)
    {
        //dbAdapter = new DbAdapter(this);
        FBDB fbdb = new FBDB(this);

        //references to EditText & bind model
        uName  = (loginNameV).getText().toString();
        uModel.setLoginName(uName);
        fName   = (fNameV).getText().toString();
        uModel.setNameOfUser(fName);
        add     = (addressV).getText().toString();
        uModel.setAddress(add);
        uModel.setEmail((emailV).getText().toString());
        uModel.setPhone((phoneV).getText().toString());
        uPass = ((EditText)findViewById(R.id.txtEditPass)).getText().toString();
        uModel.setPw(uPass);

/*        //encrypt pw
        try
        {
            uPass   = AESCrypt.encrypt(((EditText)findViewById(R.id.txtEditPass)).getText().toString());
            uModel.setPw(uPass);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/

        //get shared preference
        pref = getSharedPreferences("prefs", 0);

        //make json from model
        formData = gson.toJson(uModel);
        //
        boolean success = fbdb.updateUserProfile(uModel, userIdStr);
        if(success)
        {
            Toast.makeText(this, "Profile Updated", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Profile could not be updated", Toast.LENGTH_LONG).show();
        }

  /*      //prep args
        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/UpdateUser/" + userIdStr;
        paramsApiUri[1] = formData;
        paramsApiUri[2] = "POST";
        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);*/
    }

    //populate detail of user in EditText boxes
    @Override
    public void onResponseFromServer(String result, Context ctx)
    {
        User u;
        Gson gson = new Gson();
        //need to get refs AGAIN via ctx
        loginNameV = ((Activity)ctx).findViewById(R.id.txtEditUserName);
        fNameV =((Activity)ctx).findViewById(R.id.txtEditFName);
        addressV = ((Activity)ctx).findViewById(R.id.txtEditAdd);
        emailV = ((Activity)ctx).findViewById(R.id.txtEditEmail);
        phoneV = ((Activity)ctx).findViewById(R.id.txtEditPhone);
        pwV = ((Activity)ctx).findViewById(R.id.txtEditPass);
        btnDeleteUserV = ((Activity)ctx).findViewById(R.id.btnDeleteUser);

        //map user-JSON to user-obj
        u = gson.fromJson(result, User.class);
        //
        uPass   = u.getPw();


        //de-crypt pw
    /*    try
        {
            uPass   = AESCrypt.decrypt(u.getPw());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/

        //populate EditTexts w details of retrieved user
        loginNameV.setText(u.getLoginName());
        fNameV.setText(u.getNameOfUser());
        addressV.setText(u.getAddress());
        emailV.setText(u.getEmail());
        phoneV.setText(u.getPhone());
        ((Activity)ctx).getSharedPreferences("prefs", 0).edit().putString("Id_UserEditing",String.valueOf(u.getId_User())).commit();
        pwV.setText(uPass);

        //activate the btn
        ((Activity)ctx).findViewById(R.id.btnUpdateUser).setEnabled(true);
        //if ADMIN enable del btn too
        //get role of logedin person
        String roleStr = ((Activity)ctx).getSharedPreferences("prefs", 0).getString("role", "");
        if(roleStr.equals("3"))
        {
            btnDeleteUserV.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResponseFromServer(List<Booking> allBookings, Context ctx) {

    }

    @Override
    public void onResponseFromServer(ArrayList<User> allUsersAdminSearched, Context ctx) {

    }

    //only ADMIN an delete a user
    public void btnClk_DeleteUser(View btn_v)
    {
        alert("", "Action_DeleteUser", btn_v);
    }

    private void DeleteUser(View btn_v)
    {
        //get Id_User from hidden ctrl
        String Id_UserEditing = getSharedPreferences("prefs", 0).getString("Id_UserEditing", "");
        new FBDB(this).deleteUser(Id_UserEditing);
/*        //send id to be deleted to API
        dbAdapter = new DbAdapter(this);
        //e.g. /api/values/DeleteUser/13
        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/DeleteUser/" + Id_UserEditing;
        paramsApiUri[1] = formData = "";
        paramsApiUri[2] = "POST";
        dbAdapter.execute(paramsApiUri);*/
        finish();
    }

    public void alert(String txtMsg, final String action, final View btn_view)
    {
        //region (1) set custom view for dialog
        LayoutInflater inflator = LayoutInflater.from(SettingsActivity.this);
        final View yourCustomView = inflator.inflate(R.layout.custom_dialog, null);
        //endregion

        //region (2) init dialogue
        final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this)
                .setTitle("Do you want to proceed ?")//replace w "txtMsg"
                .setView(yourCustomView)
                .create();
        //endregion

        //region (3) set onClicks for custom dialog btns
        yourCustomView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switch (action)
                {
                    case "Action_DeleteUser":
                        DeleteUser(btn_view);
                        dialog.dismiss();
                        break;
                }
            }
        });
        yourCustomView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
            }
        });
        //endregion

        //region (4) Display dialogue
        dialog.show();
        //endregion
    }
}
