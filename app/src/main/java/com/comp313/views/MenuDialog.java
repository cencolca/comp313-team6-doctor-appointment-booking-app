package com.comp313.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.comp313.R;
import com.comp313.activities.AdminDashboardActivity;
import com.comp313.activities.DashboardActivity;
import com.comp313.activities.LoginActivity;
import com.comp313.adapters.MenuAdapter;

public class MenuDialog extends BottomSheetDialogFragment {
    private static MenuDialog instance;

    private Activity context;

    public static MenuDialog getInstance(Activity context) {
        if (instance == null) instance = new MenuDialog();
        instance.context = context;
        return instance;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        final View contentView = View.inflate(getContext(), R.layout.menu_dialog, null);

        String userIdStr = context.getSharedPreferences("prefs",0).getString("Id_User", "");
        final String userRoleStr = context.getSharedPreferences("prefs",0).getString("role", "");

        final boolean isLoggedIn = userIdStr != "";

        String[] titles = new String[]{"Login"};
        int[] imgList = new int[]{R.drawable.ic_outline_account_circle_24px};

        if (isLoggedIn) {
            titles = new String[]{"Logout", "Dashboard"};
            imgList = new int[]{R.drawable.ic_outline_account_circle_24px, R.drawable.ic_outline_dashboard_24px};
        }
        MenuAdapter adapter = new MenuAdapter(context, titles, imgList);

        ListView listView = contentView.findViewById(R.id.menuList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = null;

                if (!isLoggedIn) {
                    intent = new Intent(context, LoginActivity.class);
                } else {
                    switch (i) {
                        case 0:
                            context.getSharedPreferences("prefs",0).edit().putString("Id_User", "").putString("role", "").commit();
                            intent = new Intent(context, LoginActivity.class);
                            break;

                        case 1:
                            if(userRoleStr.equals("3"))
                                intent = new Intent(context, AdminDashboardActivity.class);
                            else
                                intent = new Intent(context, DashboardActivity.class);
                            break;

                        default:
                    }


                }

                if (intent != null) {
                    startActivity(intent);
                }
                dismiss();
                context.finish();
            }
        });

        dialog.setContentView(contentView);
    }
}
