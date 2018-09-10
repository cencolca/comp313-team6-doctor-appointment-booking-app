package comp231.drbooking;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import comp231.drbooking.databinding.ActivityDrProfileBinding;

public class DrProfile extends AppCompatActivity {

    ActivityDrProfileBinding binding;

    //region >>> Vars
    int DrSelectedPos;
    String DrSelectedName;
    Model_DrProfile dr;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dr_profile);
        TextView txtDrNameV = binding.txtDrName;
        txtDrNameV.setText("a a a a a a ");

        dr = new Model_DrProfile();
        DrSelectedPos = getIntent().getIntExtra("DrSelectedIndex", 0);
        DrSelectedName = getIntent().getStringExtra("DrSelectedName");

        if(DrSelectedPos != 0)
        {
            for (Model_DrProfile Dr : VariablesGlobal.DrProfiles)
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
