package com.comp313.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.comp313.helpers.ServiceIntro;

import com.comp313.R;

public class SplashScreenActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //IntentService
        startService(new Intent(this, ServiceIntro.class));


        Thread timer = new Thread(){
            public void run(){
                try{
//                  sleep(100);
                    ImageView icon = findViewById(R.id.icon);
                    Animation rotateAni = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                    icon.startAnimation(rotateAni);

                    sleep(2000);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                } finally {
                    Intent i = new Intent(SplashScreenActivity.this, MapsActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
