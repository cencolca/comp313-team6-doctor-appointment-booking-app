package com.comp313.views;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

public class CustomTimePickerDialog extends TimePickerDialog
{
//http://chandelashwini.blogspot.com/2013/01/timepickerdialog-15-minutes-interval.html
    public CustomTimePickerDialog(Context arg0, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView)
    {
        //Newer TimePicket w round dial doesn't work well with 30min interval thing esp w my BlackBerry (works on Emulator thou!!!). So set theme to old SPINNER style picker : TimePickerDialog.THEME_HOLO_LIGHT
        super(arg0, TimePickerDialog.THEME_HOLO_LIGHT, callBack, hourOfDay, minute, is24HourView);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
    {
        if (mIgnoreEvent)
            return;

        if (minute%TIME_PICKER_INTERVAL!=0)
        {
            int minuteFloor=minute-(minute%TIME_PICKER_INTERVAL);
            minute=minuteFloor + (minute==minuteFloor+1 ? TIME_PICKER_INTERVAL : 0);

            if (minute==60)
                minute=0;

            //ignore any change made while TimePicker is being set
            mIgnoreEvent=true;
            view.setCurrentMinute(minute);
            mIgnoreEvent=false;
        }
    }

    private final int TIME_PICKER_INTERVAL=30;
    private boolean mIgnoreEvent=false;
}


