package comp231.drbooking;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Activity for user to register as new user
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.UnicodeSetSpanner;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;//implementation  'com.android.support:design:26.1.0'

import com.google.gson.Gson;

import java.sql.SQLNonTransientConnectionException;
import java.util.Map;
import java.util.Random;

public class NewUserRegister extends BaseActivity {

    //region Class Variables
    String ROLE_CODE = "0", roleStr, txtVeriCode,formData, uName, uPass,fName, lName, add, city, postC, key_uName, key_uPass;
    SharedPreferences pref;
    Map<String, ?> allPrefs;
    int numOfPrefs;
    long rowID;
    TextView uNameV;
    EditText txtVeriCodeV, txtVerifyEmailV;
    RadioButton radRoleDr, radRoleAdmin;
    RadioGroup radGrpRole;
    View lay;
    DbAdapter dbAdapter;
    Model_User uModel;
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
        getSupportActionBar().setTitle("Create New Account");
        //chk if admin is creating a new user
        roleStr = getSharedPreferences("prefs", 0).getString("role", "");
        isAdmin = roleStr.equals("3")?true:false;
        //
        txtVeriCodeV = (EditText)findViewById(R.id.txtVerifyEmail);
        lay = findViewById(R.id.layNewUser);
        btnVerifyEmail = (Button)findViewById(R.id.btnVerifyEmail);
        txtVerifyEmailV = (EditText)findViewById(R.id.txtVerifyEmail);
        radGrpRole = (RadioGroup)findViewById(R.id.radGrpRole);

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
        uModel = new Model_User();

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
            uModel.loginName = uName  = ((EditText)findViewById(R.id.txtUserName)).getText().toString();
            fName   = ((EditText)findViewById(R.id.txtFName)).getText().toString();
            lName   = ((EditText)findViewById(R.id.txtLName)).getText().toString();
            uModel.nameOfUser = fName;
            uModel.address = add     = ((EditText)findViewById(R.id.txtAdd)).getText().toString();
            uModel.email = ((EditText)findViewById(R.id.txtEmail)).getText().toString();
            uModel.phone = ((EditText)findViewById(R.id.txtPhone)).getText().toString();
            uModel.role = ROLE_CODE;
            uModel.pw = ((EditText)findViewById(R.id.txtPass)).getText().toString();

            //if uName & pw & email r empty, -> prompt
            if(uModel.email.equals("") || uModel.nameOfUser.equals("") || uModel.loginName.equals("") || uModel.pw.equals(""))
            {
                Toast.makeText(this,"Required fields are empty" , Toast.LENGTH_LONG).show();
            }


            else
                {
                //encrypt pw
                try
                {
                    uModel.pw = uPass   = AESCrypt.encrypt(uModel.pw);
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
