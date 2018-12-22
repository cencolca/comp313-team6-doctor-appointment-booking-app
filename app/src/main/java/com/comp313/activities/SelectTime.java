package com.comp313.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.comp313.R;
import com.comp313.adapters.ICallBackFromDbAdapter;
import com.comp313.dataaccess.FBDB;
import com.comp313.models.Booking;
import com.comp313.models.User;
import com.comp313.views.CustomTimePickerDialog;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SelectTime extends BaseActivity implements ICallBackFromDbAdapter {

    //region Vars
    Button btnCancelApp;
    TextView dateTxtV, timeTxtV, txtClinicV, txtDrV;
    Calendar cal;
    String formData, DrSelectedName, ClinicName, dateStr, timeStr,AppointmentTime,userIdStr, nameOfUser, appIdStr;
    int  DrSelectedId;
    Long dateTimeUnix;
    SharedPreferences prefs;
    Intent i;
    Booking app;
    Gson gson;


    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_time);
        super.setupToolbar("Appointment Details");
        //
        gson = new Gson();
        prefs = getSharedPreferences("prefs", 0);
        userIdStr = prefs.getString("Id_User", "");
        nameOfUser = prefs.getString("UserWhoCreateApp","");
        //
        txtDrV = (TextView)findViewById(R.id.txtDr_in_SelectTime);
        txtClinicV = (TextView)findViewById(R.id.txtClinic_in_SelectTime);

        //userIdInt = Integer.parseInt(userIdStr);
        //
        Intent intent = getIntent();
        DrSelectedName = intent.getStringExtra("DrSelectedName");
        ClinicName = intent.getStringExtra("ClinicName");
        DrSelectedName = intent.getStringExtra("DrSelectedName");
        DrSelectedId = intent.getIntExtra("DrSelectedId", 0);
        appIdStr = intent.getStringExtra("appId_clicked");

        //
        btnCancelApp = (Button)findViewById(R.id.btnCancelApp);
        btnCancelApp.setVisibility(View.INVISIBLE);

        //region >>>DatePicker Set Up
        dateTxtV = (TextView)findViewById(R.id.txtDate);
        cal = Calendar.getInstance();
        //Create Listener for DatePicker
        final DatePickerDialog.OnDateSetListener dateChangeListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int yr, int mo, int day)
            {
                cal.set(Calendar.YEAR, yr);
                cal.set(Calendar.MONTH, mo);
                cal.set(Calendar.DAY_OF_MONTH, day);
                updateDateTxtV();//Update txtDateV
            }
        };
        //Assign Listener to dateTxtV
        dateTxtV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog
                        (
                                SelectTime.this,
                                dateChangeListener,
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                        ).show();
            }
        });
        updateDateTxtV();
        //endregion

        //region >>>TimePicker Set Up
        timeTxtV = (TextView)findViewById(R.id.txtTime);
        //Create Listener for DatePicker
        final TimePickerDialog.OnTimeSetListener timeChangeListener = new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int _24hr, int min)
            {
                cal.set(Calendar.HOUR_OF_DAY, _24hr);
                cal.set(Calendar.MINUTE, min);
                updateTimeTxtV();
            }
        };
        //Assign Listener to dateTxtV
        timeTxtV.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                //new TimePickerDialog
                new CustomTimePickerDialog
                        (
                                SelectTime.this,
                                timeChangeListener,
                                cal.get(Calendar.HOUR),
                                cal.get(Calendar.MINUTE),
                                false
                        ).show();
            }
        });
        //endregion

        //region if editing existing booking sent from Booking_All
        String jsonAppointment = getIntent().getStringExtra("appointment");
        app = gson.fromJson(jsonAppointment, Booking.class);
        if(app != null)
        {
            DrSelectedName = app.getDoctor();
            ClinicName = app.getClinic();
            DrSelectedId = app.getId_Doc();
            //
            btnCancelApp.setVisibility(View.VISIBLE);

            getSharedPreferences("prefs", 0).edit().putInt("Id_Appointment", app.getId_Appointment()).commit();
            txtClinicV.setText(app.getClinic());
            txtDrV.setText(app.getDoctor());
            //
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa");
            Date dt = null;
            try
            {
                dt = sdf.parse(app.getAppointmentTime());
            }
            catch (ParseException e)//import java.text.ParseException;
            {
                e.printStackTrace();
            }
            sdf = new SimpleDateFormat("EEE, d MMM yyyy");
            dateTxtV.setText(sdf.format(dt));
            sdf = new SimpleDateFormat("hh:mm aaa");
            timeTxtV.setText(sdf.format(dt));

            //spinDrList.setSelection(VariablesGlobal.DrNamesList.indexOf(app.getDoctor()));//GetDrArray() is slower so this line of code fixed selected index to '0' unless return bk to list of appoints & come bk
            cal.setTimeInMillis(dt.getTime());
        }
        else
        {
            txtClinicV.setText(ClinicName);
            txtDrV.setText(DrSelectedName);
        }
        //endregion

        //set min to 00 so that TimePicker never launches to show any other min
        cal.set(Calendar.MINUTE, 0);
        updateDateTxtV();
        updateTimeTxtV();
    }

    //Update Date in text field
    private void updateDateTxtV()
    {
        String dateFormat ="EEE, d MMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.CANADA);
        dateStr = sdf.format(cal.getTime());
        dateTxtV.setText(dateStr);

        //unix datetime for saving in db
        dateTimeUnix = cal.getTimeInMillis() / 1000L;
    }
    //Update Time in text field
    private void updateTimeTxtV()
    {
        String timeFormat ="hh:mm aaa";//12:08 PM
        SimpleDateFormat stf = new SimpleDateFormat(timeFormat, Locale.CANADA);
        timeStr = stf.format(cal.getTime());
        timeTxtV.setText(timeStr);

        //unix datetime for saving in db
        dateTimeUnix = cal.getTimeInMillis() / 1000L;
    }

    public void clk_SaveAppoint(View view)
    {
        this.AppointmentTime = getDateTime(dateTimeUnix);

        Booking newBooking = new Booking();

        newBooking.setId_User(userIdStr);
        newBooking.setAppointmentTime(this.AppointmentTime);
        newBooking.setClinic(ClinicName);
        newBooking.setCreationTime(getDateTime(Calendar.getInstance().getTimeInMillis() /1000L));
        newBooking.setDoctor(DrSelectedName);
        newBooking.setDRAVAILABLE("1");
        newBooking.setId_Doc(DrSelectedId);
        newBooking.setUser(nameOfUser);

        boolean success = false;

        if(app != null)//editing existing booking (not creating a new one)
        {
            /*success =*/ new FBDB(SelectTime.this , this).updateBooking(newBooking, appIdStr);
        }
        else //creating a new appoint
        {
            /*success = */new FBDB(SelectTime.this , this).createBooking(newBooking);
        }

   /*     if(success)
        {
            Toast.makeText(getApplicationContext(), "Appointment saved!", Toast.LENGTH_LONG).show();
            i = new Intent(this, Bookings_AllActivity.class);
            startActivity(i);
            finish();
        }

        else
            Toast.makeText(getApplicationContext(), "Error occurred!", Toast.LENGTH_SHORT).show();*/
    }

    private String getDateTime(Long dateTimeUnix)
    {
        //covert unix-datetime (in seconds) to string
        Date appointTime = new Date(dateTimeUnix * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa");
        AppointmentTime = sdf.format(appointTime);
        return AppointmentTime;
    }

    public void clk_CancelAppoint(View view)
    {
        boolean success = false;
        success = new FBDB(SelectTime.this).cancelBooking(appIdStr);
        if(success)
        {
            Toast.makeText(getApplicationContext(), "Appointment deleted!", Toast.LENGTH_LONG).show();
            i = new Intent(this, Bookings_AllActivity.class);
            startActivity(i);
            finish();
        }

        else
            Toast.makeText(getApplicationContext(), "Error occurred!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponseFromServer(String result, Context ctx)
    {
        if(result.equals("timeIsAvailable"))
        {
            Toast.makeText(getApplicationContext(), "Appointment saved!", Toast.LENGTH_LONG).show();
            i = new Intent(this, Bookings_AllActivity.class);
            startActivity(i);
            finish();
        }

        else
            Toast.makeText(getApplicationContext(), "Time not available!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponseFromServer(List<Booking> allBookings, Context ctx)
    {

    }

    @Override
    public void onResponseFromServer(ArrayList<User> allUsersAdminSearched, Context ctx) {

    }
}
