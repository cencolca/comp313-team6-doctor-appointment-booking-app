package comp231.drbooking;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Customized InfoWindow for Maps. Displays name & address of a clinic.
 * Allows clicking on InfoWindow to fire BookingDetails activity & passes on the data about that Place as well.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class InfoWinAdapter implements InfoWindowAdapter
{
    LayoutInflater inflater=null;

    InfoWinAdapter(LayoutInflater inflater)
    {
        this.inflater=inflater;
    }

    //GoogleMap.InfoWindowAdapter interface needs 2 fn below
    @Override
    public View getInfoWindow(Marker marker)
    {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker)
    {
        View myMarkLay = inflater.inflate(R.layout.infowindowcustom, null);
        Button btn = myMarkLay.findViewById(R.id.markerIcon);

        TextView tv = (TextView)myMarkLay.findViewById(R.id.markerTitle);
        tv.setText(marker.getTitle());
        tv = (TextView)myMarkLay.findViewById(R.id.markerSnippet);
        tv.setText(marker.getSnippet());
        return(myMarkLay);
    }



    //custom listener for InfoWindow
    class MyClkListener implements View.OnClickListener
    {
        String userIdStr;
        //ViewHolder vh;
        Context ctx;
        int userId;

        //constructor to pass user ID to listener for each btn
        public MyClkListener(int userId)
        {
            this.userIdStr = String.valueOf(userId);
            this.userId = userId;
        }

        @Override
        public void onClick(View view)
        {
            ctx = view.getContext();

            Intent i = new Intent(ctx, BookingDetails.class);
            SharedPreferences prefs = ctx.getSharedPreferences("login",0);
            prefs.edit().putInt("userId", userId).commit();
            ctx.startActivity(i);
            ((Activity)ctx).finish();
        }
    }




}
