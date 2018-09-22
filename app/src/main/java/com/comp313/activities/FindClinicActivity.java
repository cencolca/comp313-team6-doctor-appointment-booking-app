package com.comp313.activities;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Not in use yet... may discard later.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.comp313.R;

public class FindClinicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_clinic);
        getSupportActionBar().setTitle("Clinics Near You");

    }
}
