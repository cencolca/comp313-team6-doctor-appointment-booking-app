package com.comp313.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.DrProfile;

import com.comp313.R;
import com.comp313.databinding.ActivityDrProfileBinding;

public class DrProfileActivity extends AppCompatActivity {

    ActivityDrProfileBinding binding;

    //region >>> Vars
    int DrSelectedPos;
    String DrSelectedName;
    DrProfile dr;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dr_profile);
        TextView txtDrNameV = binding.txtDrName;
        txtDrNameV.setText("a a a a a a ");

        dr = new DrProfile();
        DrSelectedPos = getIntent().getIntExtra("DrSelectedIndex", 0);
        DrSelectedName = getIntent().getStringExtra("DrSelectedName");

        if(DrSelectedPos != 0)
        {
            for (DrProfile Dr : VariablesGlobal.DrProfiles)
            {
                if(Dr.name.equals(DrSelectedName))
                {
                    dr = Dr;
                }
            }
        }
        else
        {
            dr.name = "No Doctor Was Selected";
            dr.specialty = "No Doctor Was Selected";
            dr.email = "No Doctor Was Selected";
            dr.phone = "No Doctor Was Selected";
            dr.id_doc = 0;
            dr.Id_User = 0;
        }

        binding.setDr(dr);
    }
}
