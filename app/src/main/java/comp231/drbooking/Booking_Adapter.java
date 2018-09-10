package comp231.drbooking;
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

import com.google.gson.Gson;

import java.util.List;

//custom ArrayAdapter to display list of Model_Booking class
public class Booking_Adapter extends ArrayAdapter<Model_Booking>
{

    private List<Model_Booking> list;
    private Activity context;
    Gson gson;

    //constructor
    public Booking_Adapter(@NonNull Activity ctx, int layoutId, @NonNull List<Model_Booking> list)
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

        //viewHolder.rowTime.setText(list.get(position).);
        view.setTag(viewHolder);

        Model_Booking app = list.get(position);

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.rowPtName.setText(app.User);
        holder.rowTime.setText(app.AppointmentTime);
        holder.rowDrName.setText(app.Doctor);
        holder.rowClinic.setText(app.Clinic);
        if(app.User.equals(app.Doctor))//Dr created appoint for oneself = unavailable slot
        {
            view.setBackgroundColor(ContextCompat.getColor(context,  R.color.dashboard_segment2));
            holder.rowPtName.setText(R.string.strDrUnAvailable);//"*** Un-Available***"
            holder.rowDrName.setText(R.string.strDrUnAvailable);
            holder.rowClinic.setText(R.string.strDrUnAvailable);
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
        Model_Booking app;

        public myClkListener(Context ctx, Model_Booking app)
        {
            this.ctx = ctx;
            this.app = app;
        }

        @Override
        public void onClick(View view)
        {
            Intent i = new Intent(context, BookingDetails.class);
            i.putExtra("appointment", gson.toJson(app));
            context.startActivity(i);
        }
    }
}

