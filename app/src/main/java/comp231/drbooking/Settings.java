package comp231.drbooking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

public class Settings extends BaseActivity implements ICallBackFromDbAdapter {

    //region Class Variables
    String userIdStr, roleStr, formData, uName, uPass,fName, lName, add;
    DbAdapter dbAdapter;
    Model_User uModel;
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
        getSupportActionBar().setTitle("Update User Profile");

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
        uModel = new Model_User();

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
            Intent i = new Intent(this, Login.class);
            startActivity(i);
            finish();
            return;
        }
        //
        dbAdapter = new DbAdapter(Settings.this, new Settings());
        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/getuserdetail/" + userIdStr;
        paramsApiUri[1] = "";
        paramsApiUri[2] = "GET";
        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);


    }

    public void btnClk_UpdateUser(View view)
    {
        dbAdapter = new DbAdapter(this);

        //references to EditText & bind model
        uModel.loginName = uName  = (loginNameV).getText().toString();
        fName   = (fNameV).getText().toString();
        uModel.nameOfUser = fName;
        uModel.address = add     = (addressV).getText().toString();
        uModel.email = (emailV).getText().toString();
        uModel.phone = (phoneV).getText().toString();
        //encrypt pw
        try
        {
            uModel.pw = uPass   = AESCrypt.encrypt(((EditText)findViewById(R.id.txtEditPass)).getText().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //get shared preference
        pref = getSharedPreferences("prefs", 0);

        //make json from model
        formData = gson.toJson(uModel);
        //prep args
        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/UpdateUser/" + userIdStr;
        paramsApiUri[1] = formData;
        paramsApiUri[2] = "POST";
        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);
    }

    //populate detail of user in EditText boxes
    @Override
    public void onResponseFromServer(String result, Context ctx)
    {
        Model_User u;
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
        u = gson.fromJson(result, Model_User.class);

        //de-crypt pw
        try
        {
            uPass   = AESCrypt.decrypt(u.pw);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //populate EditTexts w details of retrieved user
        loginNameV.setText(u.loginName);
        fNameV.setText(u.nameOfUser);
        addressV.setText(u.address);
        emailV.setText(u.email);
        phoneV.setText(u.phone);
        ((Activity)ctx).getSharedPreferences("prefs", 0).edit().putString("Id_UserEditing",String.valueOf(u.Id_User)).commit();
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

    //only ADMIN an delete a user
    public void btnClk_DeleteUser(View btn_v)
    {
        alert("", "Action_DeleteUser", btn_v);
    }

    private void DeleteUser(View btn_v)
    {
        //get Id_User from hidden ctrl
        String Id_UserEditing = getSharedPreferences("prefs", 0).getString("Id_UserEditing", "");

        //send id to be deleted to API
        dbAdapter = new DbAdapter(this);
        //e.g. /api/values/DeleteUser/13
        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/DeleteUser/" + Id_UserEditing;
        paramsApiUri[1] = formData = "";
        paramsApiUri[2] = "POST";
        dbAdapter.execute(paramsApiUri);
        finish();
    }

    public void alert(String txtMsg, final String action, final View btn_view)
    {
        //region (1) set custom view for dialog
        LayoutInflater inflator = LayoutInflater.from(Settings.this);
        final View yourCustomView = inflator.inflate(R.layout.custom_dialog, null);
        //endregion

        //region (2) init dialogue
        final AlertDialog dialog = new AlertDialog.Builder(Settings.this)
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
