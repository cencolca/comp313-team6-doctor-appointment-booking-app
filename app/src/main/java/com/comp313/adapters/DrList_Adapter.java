
package com.comp313.adapters;

        /*
         * By: Shafiq
         * Purpose: custom adapter to display result-set when admin searches for users by a part of username
         */
        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.text.Layout;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;

        import com.comp313.activities.DrProfileActivity;
        import com.comp313.activities.SettingsActivity;
        import com.comp313.helpers.VariablesGlobal;
        import com.comp313.models.DrProfile;
        import com.comp313.models.DrProfile;
        import com.google.gson.Gson;

        import java.util.List;

        import com.comp313.R;

//custom ArrayAdapter to display list of Model_Booking class
public class DrList_Adapter extends ArrayAdapter<DrProfile>
{

    private List<DrProfile> list;
    private Activity context;
    Gson gson;

    //constructor
    public DrList_Adapter(@NonNull Activity ctx, int layoutId, @NonNull List<DrProfile> list)
    {
        super(ctx, layoutId, list);
        this.context = ctx;
        this.list = list;
        gson = new Gson();
    }

    static class ViewHolder
    {
        protected TextView rowLogInName;
        protected TextView rowNameOfUser;
        protected TextView userId_From;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable final View convertView, @NonNull ViewGroup parent)
    {
        View view = null;

        LayoutInflater inflater = context.getLayoutInflater();
        view = inflater.inflate(R.layout.eachuser, null);

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.rowLogInName = (TextView) view.findViewById(R.id.rowUserName);
        viewHolder.rowNameOfUser = (TextView) view.findViewById(R.id.rowFullName);

        view.setTag(viewHolder);

        DrProfile app = list.get(position);

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.rowLogInName.setText(app.name);
        holder.rowNameOfUser.setText(app.specialty);

        //click Listener
        myClkListener listener = new myClkListener(context, app, position);
        view.setOnClickListener(listener);
        return view;
    }

    class myClkListener implements View.OnClickListener
    {
        Context ctx;
        DrProfile app;
        int DrSelectedIndex;
        String DrSelectedName, ClinicName;

        public myClkListener(Context ctx, DrProfile app, int DrSelectedIndex)
        {
            this.ctx = ctx;
            this.app = app;
            this.DrSelectedIndex = DrSelectedIndex + 1;
        }

        @Override
        public void onClick(View view)
        {


            DrSelectedName = ((TextView)view.findViewById(R.id.rowUserName)).getText().toString();
            String ClinicName = ((TextView) ((Activity)ctx).findViewById(R.id.txtClinicName)).getText().toString();

            //String item_2 = ((TextView)view.findViewById(R.id.rowFullName)).getText().toString();

            Intent i = new Intent(context, DrProfileActivity.class);
            i.putExtra("DrSelectedIndex", DrSelectedIndex);
            i.putExtra("ClinicName", ClinicName);
            i.putExtra("DrSelectedName", DrSelectedName);

            i.putExtra("DrSelectedId", VariablesGlobal.DrProfiles.get(DrSelectedIndex - 1).id_doc);

            context.startActivity(i);


            //i.putExtra("Id_User", String.valueOf(app.getId_User()));
        }
    }
}