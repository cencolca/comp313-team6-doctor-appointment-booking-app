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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.comp313.activities.SettingsActivity;
import com.comp313.models.User;
import com.google.gson.Gson;

import java.util.List;

import com.comp313.R;

//custom ArrayAdapter to display list of Model_Booking class
public class User_Adapter extends ArrayAdapter<User>
{

    private List<User> list;
    private Activity context;
    Gson gson;

    //constructor
    public User_Adapter(@NonNull Activity ctx, int layoutId, @NonNull List<User> list)
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

        User app = list.get(position);

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.rowLogInName.setText(app.getLoginName());
        holder.rowNameOfUser.setText(app.getNameOfUser());

        //click Listener
        myClkListener listener = new myClkListener(context, app);
        view.setOnClickListener(listener);
        return view;
    }

    class myClkListener implements View.OnClickListener
    {
        Context ctx;
        User app;

        public myClkListener(Context ctx, User app)
        {
            this.ctx = ctx;
            this.app = app;
        }

        @Override
        public void onClick(View view)
        {
            Intent i = new Intent(context, SettingsActivity.class);
            i.putExtra("Id_User", String.valueOf(app.getId_User()));
            context.startActivity(i);
        }
    }
}