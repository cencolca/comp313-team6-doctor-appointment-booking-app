package com.comp313.activities;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Activity for user to register as new user
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.comp313.dataaccess.DbAdapter;
import com.comp313.dataaccess.FBDB;
import com.comp313.helpers.AESCrypt;
import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.User;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Random;

import com.comp313.R;

public class RegisterActivity extends BaseActivity {

    //region Class Variables
    String ROLE_CODE = "0", roleStr, txtVeriCode,formData, uName, uPass,fName, lName, add, city, postC, key_uName, key_uPass;
    SharedPreferences pref;
    Map<String, ?> allPrefs;
    int numOfPrefs;
    long rowID;
    TextView uNameV;
    EditText txtVeriCodeV, txtVerifyEmailV;

    // Al - 2018-10-03 START
    EditText txtUserName, txtPass, txtFName, txtLName, txtAdd, txtEmail, txtPhone;
    // Al - 2018-10-03 END

    RadioButton radRoleDr, radRoleAdmin;
    RadioGroup radGrpRole;
    View lay;
    DbAdapter dbAdapter;
    User uModel;
    Object[] paramsApiUri;
    Button btnCreateNewUser, btnVerifyEmail;
    Gson gson;
    int emailCode = 999;
    Intent i;
    boolean isAdmin;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_register);
        super.setupToolbar("Create New Account");
        //chk if admin is creating a new user
        roleStr = getSharedPreferences("prefs", 0).getString("role", "");
        isAdmin = roleStr.equals("3")?true:false;
        //
        txtVeriCodeV = findViewById(R.id.txtVerifyEmail);
        lay = findViewById(R.id.layNewUser);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);
        txtVerifyEmailV = findViewById(R.id.txtVerifyEmail);
        radGrpRole = findViewById(R.id.radGrpRole);

        // Al - 2018-10-03 START
        final EditText txtUserName, txtPass, txtFName, txtLName, txtAdd, txtEmail, txtPhone;

        txtUserName = findViewById(R.id.txtUserName);
        txtPass = findViewById(R.id.txtPass);
        txtFName = findViewById(R.id.txtFName);
        txtLName = findViewById(R.id.txtLName);
        txtAdd = findViewById(R.id.txtAdd);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);

        btnCreateNewUser = findViewById(R.id.btnCreateNewUser);
        btnCreateNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get values from screen
                // and create User object

                User newUser = new User();
                newUser.setNameOfUser(txtFName.getText().toString().trim() + " " + txtLName.getText().toString().trim());
                newUser.setPw(txtPass.getText().toString().trim());
                newUser.setLoginName(txtUserName.getText().toString().trim());
                newUser.setAddress(txtAdd.getText().toString().trim());
                newUser.setEmail(txtEmail.getText().toString().trim());
                newUser.setPhone(txtPhone.getText().toString().trim());

                boolean success = new FBDB(RegisterActivity.this).registerUser(newUser);

                if(success)
                    Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Registration failed!", Toast.LENGTH_SHORT).show();
            }
        });


        // Al - 2018-10-03 END

        //listener for RadioGroup
        radGrpRole.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id_of_radBtn)
            {
                switch (id_of_radBtn)
                {
                    case R.id.radRolePt:
                        ROLE_CODE = "1";//Patient
                        break;
                    case R.id.radRoleDr:
                        ROLE_CODE = "2";//Care Provider
                        break;
                    case R.id.radRoleAdmin:
                    ROLE_CODE = "3";//Admin
                        break;
                        default:
                            ROLE_CODE = "0";//guest
                            break;
                }
            }
        });

        paramsApiUri = new Object[3];
        gson = new Gson();

        //get form data into class
        uModel = new User();

        //for admin hide verify email etc
        if(isAdmin)
        {
            btnVerifyEmail.setVisibility(View.INVISIBLE);
            txtVerifyEmailV.setVisibility(View.INVISIBLE);
        }
    }

    public void btnClk_CreateNewUser(View view)
    {
        //for admin by-pass email verification - assume admin enters correct email
        if(isAdmin)
        {
            disableTextBoxes(lay);
            clk_verifyEmail(null);
            return;
        }
        //for non-admin
        disableTextBoxes(lay);
        sendCodeByEmail(getRandomCode());
        btnVerifyEmail.setEnabled(true);
        txtVerifyEmailV.setEnabled(true);

    }

    private void sendCodeByEmail(int code)
    {
        i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"mani66@hotmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "DrApp Verification Code");
        i.putExtra(Intent.EXTRA_TEXT   , "your DrApp verification code is: " + emailCode);

        try
        {
            startActivity(Intent.createChooser(i, "Send mail..."));
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private int getRandomCode()
    {
        Random random = new Random();
        emailCode = random.nextInt(900) + 100;
        return emailCode;
    }

    private void disableTextBoxes(View layout)
    {
        if(layout instanceof ViewGroup)
        {
            ViewGroup vg = (ViewGroup)layout;
            int allViews = vg.getChildCount();
            for (int j = 0; j < allViews; j++)
            {
                View v = vg.getChildAt(j);
                disableTextBoxes(v);
            }
        }
        else if (layout instanceof EditText)
        {
            layout.setEnabled(false);
        }

    }

    //If need to call sth in this class from AsyncTask, put that here:
    static void AfterAsyncTask(String jsonResponse, Context ctx)
    {

    }

    public void clk_verifyEmail(View view)
    {
        //for non-admin, read verifi code
        if(!isAdmin)
        {
            txtVeriCode = txtVeriCodeV.getText().toString();
        }

        //if admin, bypass matching of verifi code
        if(isAdmin || String.valueOf(emailCode).equals(txtVeriCode))
        {

            dbAdapter = new DbAdapter(this);

            //references to EditText & bind model
            uName  = ((EditText)findViewById(R.id.txtUserName)).getText().toString();
            uModel.setLoginName(uName);
            fName   = ((EditText)findViewById(R.id.txtFName)).getText().toString();
            lName   = ((EditText)findViewById(R.id.txtLName)).getText().toString();
            uModel.setNameOfUser(fName);
            add     = ((EditText)findViewById(R.id.txtAdd)).getText().toString();
            uModel.setAddress(add);
            uModel.setEmail(((EditText)findViewById(R.id.txtEmail)).getText().toString());
            uModel.setPhone(((EditText)findViewById(R.id.txtPhone)).getText().toString());
            uModel.setRole(ROLE_CODE);
            uModel.setPw(((EditText)findViewById(R.id.txtPass)).getText().toString());

            //if uName & pw & email r empty, -> prompt
            if(uModel.getEmail().equals("") || uModel.getNameOfUser().equals("") || uModel.getLoginName().equals("") || uModel.getPw().equals(""))
            {
                Toast.makeText(this,"Required fields are empty" , Toast.LENGTH_LONG).show();
            }


            else
                {
                //encrypt pw
                try
                {
                    uPass   = AESCrypt.encrypt(uModel.getPw());
                    uModel.setPw(uPass);
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
                paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/newUser";
                paramsApiUri[1] = formData;
                paramsApiUri[2] = "POST";
                //pass args to AsyncTask to read db
                dbAdapter.execute(paramsApiUri);
            }
        }
        else
        {
            Snackbar.make(view, "code mis-match", Snackbar.LENGTH_LONG).show();
        }

    }
}
