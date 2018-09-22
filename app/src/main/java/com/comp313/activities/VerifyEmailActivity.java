package com.comp313.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.comp313.R;

public class VerifyEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);
        getActionBar().setTitle("Verify Security Code");
    }
}
