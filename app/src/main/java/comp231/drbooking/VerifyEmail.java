package comp231.drbooking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class VerifyEmail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);
        getActionBar().setTitle("Verify Security Code");
    }
}
