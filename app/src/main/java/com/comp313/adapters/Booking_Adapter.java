package com.comp313.adapters;
/*
* By: SHAFIQ-UR-REHMAN
* Purpose: ListView will use this adapter to display customized view for each item in the list of bookings
*/
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.comp313.activities.BookingDetailsActivity;
import com.comp313.activities.SelectTime;
import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.Booking;
import com.google.gson.Gson;

import java.util.List;

import com.comp313.R;

//custom ArrayAdapter to display list of Model_Booking class
public class Booking_Adapter extends ArrayAdapter<Booking>
{

    private List<Booking> list;
    private Activity context;
    Gson gson;

    //constructor
    public Booking_Adapter(@NonNull Activity ctx, int layoutId, @NonNull List<Booking> list
    )
    {
        super(ctx, layoutId, list);
        this.context = ctx;
        this.list = list;
        gson = new Gson();
    }

    static class ViewHolder
    {
        protected TextView rowTime;
        protected TextView rowDrName, rowPtName;
        protected TextView rowClinic;
        protected TextView rowAppId;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable final View convertView, @NonNull ViewGroup parent)
    {
        View view = null;

        LayoutInflater inflater = context.getLayoutInflater();
        view = inflater.inflate(R.layout.eachbooking, null);

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.rowTime = (TextView) view.findViewById(R.id.rowTime);
        viewHolder.rowDrName = (TextView) view.findViewById(R.id.rowDrName);
        viewHolder.rowClinic = (TextView) view.findViewById(R.id.rowClinic);
        viewHolder.rowPtName = view.findViewById(R.id.rowPtName);
        viewHolder.rowAppId = view.findViewById(R.id.rowAppId_Hidden);


        //viewHolder.rowTime.setText(list.get(position).);
        view.setTag(viewHolder);

        Booking app = list.get(position);
        String appIdStr = VariablesGlobal.mapAppoints.get(position).first;

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.rowPtName.setText(app.getUser());
        holder.rowTime.setText(app.getAppointmentTime());
        holder.rowDrName.setText(app.getDoctor());
        holder.rowClinic.setText(app.getClinic());
        holder.rowAppId.setText(appIdStr);
        if(app.getUser().equals(app.getDoctor()))//Dr created appoint for oneself = unavailable slot
        {
            view.setBackgroundColor(ContextCompat.getColor(context,  R.color.dashboard_segment2));
            holder.rowPtName.setText(R.string.strDrUnAvailable);//"*** Un-Available***"
            holder.rowDrName.setText(R.string.strDrUnAvailable);
            holder.rowClinic.setText(R.string.strDrUnAvailable);
            holder.rowAppId.setText(appIdStr);
            holder.rowPtName.setBackgroundColor(ContextCompat.getColor(context, R.color.dashboard_segment4));
        }

       //click Listener
        myClkListener listener = new myClkListener(context, app);
        view.setOnClickListener(listener);
        return view;
    }

    class myClkListener implements View.OnClickListener
    {
        Context ctx;
        Booking app;

        public myClkListener(Context ctx, Booking app)
        {
            this.ctx = ctx;
            this.app = app;
        }

        @Override
        public void onClick(View view)
        {
            String appIdStr = ((TextView)view.findViewById(R.id.rowAppId_Hidden)).getText().toString();

            Intent i = new Intent(context, SelectTime.class/*BookingDetailsActivity.class*/);
            i.putExtra("appointment", gson.toJson(app));
            i.putExtra("appId_clicked",appIdStr);
            context.startActivity(i);
        }
    }
}

