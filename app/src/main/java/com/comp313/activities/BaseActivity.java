package com.comp313.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.comp313.R;
import com.comp313.views.MenuDialog;

/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Base class for all activities that will show a menuoptions (three dots) on top right corner. e.g. to logout etc
 */
public class BaseActivity extends AppCompatActivity {
    Intent i;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator = getMenuInflater();
        inflator.inflate(R.menu.right_menu, menu);

        return true;
    }

    protected void setupToolbar(String title) {
        this.setupToolbar(title, true);
    }

    protected void setupToolbar(String title, boolean displayBack) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(displayBack);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu:
                MenuDialog dialog = MenuDialog.getInstance(this);
                dialog.show(this.getSupportFragmentManager(), "Show menu");
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
