package comp231.drbooking;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;

public class ServiceIntro extends IntentService
{

    //MUST add a constructor that takes NO arg. or else Manifest file gives errthat default constructor missing
    public ServiceIntro()
    {
        super("MyService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //set up MediaPlayer
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.chime);


        try {
            mp.start();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
