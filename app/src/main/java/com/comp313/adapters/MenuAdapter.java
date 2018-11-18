package com.comp313.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.comp313.R;

public class MenuAdapter extends BaseAdapter {
    private Activity mContext;
    private String[] titles;
    private int[] imge;

    public MenuAdapter(Activity context, String[] titles, int[] imageIds) {
        mContext = context;
        this.titles = titles;
        this.imge = imageIds;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return titles.length;
    }

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = mContext.getLayoutInflater();
        View row;
        row = inflater.inflate(R.layout.menu_list, parent, false);
        ((ImageView) row.findViewById(R.id.menuIcon)).setImageResource(imge[position]);
        ((TextView) row.findViewById(R.id.menuText)).setText(titles[position]);

        return row;
    }
}
