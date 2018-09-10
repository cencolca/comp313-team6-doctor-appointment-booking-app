package comp231.drbooking;

import android.app.Service;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        //IntentService
        startService(new Intent(this, ServiceIntro.class));


        Thread timer = new Thread(){
            public void run(){
                try{
//                  sleep(100);
                    ImageView icon = (ImageView) findViewById(R.id.icon);
                    Animation rotateAni = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                    icon.startAnimation(rotateAni);

                    sleep(2000);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                } finally {
                    Intent i = new Intent(SplashScreen.this, MapsActivity.class);
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
