package com.comp313.activities;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Activity to either display a new booking details or existing booking details once user clicks on an item in list of all bookings
 */

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.comp313.adapters.DrList_Adapter;
import com.comp313.adapters.ICallBackFromDbAdapter;
import com.comp313.dataaccess.DbAdapter;
import com.comp313.helpers.VariablesGlobal;
import com.comp313.models.Booking;
import com.comp313.models.DrProfile;
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

import com.comp313.R;


public class BookingDetailsActivity extends BaseActivity implements ICallBackFromDbAdapter
{

    //region Variables
    boolean isYES = false;
    TextView dateTxtV, timeTxtV, txtV, txt_clinicName, txt_timings;
    Button btnCancelApp;
    Spinner spinDrList, spinSpecialtyList;
    Calendar cal;
    String formData, dateStr, timeStr,AppointmentTime;
    Long dateTimeUnix;
    DbAdapter dbAdapter;
    Booking bModel, app;
    Gson gson;
    Object[] paramsApiUri;
    public static BookingDetailsActivity instance;
    AdapterView.OnItemSelectedListener spinListener;
    ListView lv_DrList;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        instance = this;
        super.setupToolbar("Choose a Doctor");
        //
        paramsApiUri = new Object[3];
        gson = new Gson();
        //
        VariablesGlobal.DrNamesListFiltered.clear();
        //ref to views
        txtV = findViewById(R.id.txtBookingActivity);
        txt_clinicName = findViewById(R.id.txtClinicName);
        txt_timings = findViewById(R.id.txtTime);

        spinDrList = findViewById(R.id.spinDrList);
        spinSpecialtyList = findViewById(R.id.spinSpecialtyList);
        VariablesGlobal.spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, VariablesGlobal.DrNamesListFiltered);
        spinDrList.setAdapter(VariablesGlobal.spinAdapter);

        //btnCancelApp = (Button)findViewById(R.id.btnCancelApp);
        //btnCancelApp.setVisibility(View.INVISIBLE);

        //Listener for spinner - will be attached & removed
        spinListener = new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                VariablesGlobal.filterDrNamesBy = parentView.getSelectedItem().toString();//default filter is "All"
                filteredDrList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        };

        //get list of all Drs from API
        GetSpecialtyList(spinSpecialtyList);
        GetDrArray();

        //get Extras passed from InfoWindow of marker
        txtV.setText(getIntent().getStringExtra("address"));
        txt_clinicName.setText(getIntent().getStringExtra("infoWinTitle"));
        txt_timings.setText("  " + getIntent().getStringExtra("timing"));


        //region >>>DatePicker Set Up
  /*      dateTxtV = (TextView)findViewById(R.id.txtDate);
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
                                BookingDetailsActivity.this,
                                dateChangeListener,
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                        ).show();
            }
        });
        updateDateTxtV();*/
        //endregion

        //region >>>TimePicker Set Up
/*        timeTxtV = (TextView)findViewById(R.id.txtTime);
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
                                BookingDetailsActivity.this,
                                timeChangeListener,
                                cal.get(Calendar.HOUR),
                                cal.get(Calendar.MINUTE),
                                false
                        ).show();
            }
        });*/
        //endregion

        //
        //region if editing existing booking sent from Booking_All
/*        String jsonAppointment = getIntent().getStringExtra("appointment");
        app = gson.fromJson(jsonAppointment, Booking.class);
        if(app != null)
        {
            btnCancelApp.setVisibility(View.VISIBLE);

            getSharedPreferences("prefs", 0).edit().putInt("Id_Appointment", app.getId_Appointment()).commit();
            txtV.setText(app.getClinic());

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa");
            Date dt = null;
            try
            {
                dt = sdf.parse(app.getAppointmentTime());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            sdf = new SimpleDateFormat("EEE, d MMM yyyy");
            dateTxtV.setText(sdf.format(dt));
            sdf = new SimpleDateFormat("hh:mm aaa");
            timeTxtV.setText(sdf.format(dt));

            spinDrList.setSelection(VariablesGlobal.DrNamesList.indexOf(app.getDoctor()));//GetDrArray() is slower so this line of code fixed selected index to '0' unless return bk to list of appoints & come bk
            cal.setTimeInMillis(dt.getTime());
        }*/
        //endregion

        //set min to 00 so that TimePicker never launches to show any other min
/*        cal.set(Calendar.MINUTE, 0);
        updateDateTxtV();
        updateTimeTxtV();*/

    }

    private void GetSpecialtyList(Spinner spinSpecialtyList)
    {
        ArrayAdapter<CharSequence> fieldsListAdapter = ArrayAdapter.createFromResource(this, R.array.DrSpecialty, android.R.layout.simple_spinner_item);
        fieldsListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSpecialtyList.setAdapter(fieldsListAdapter);

        //set listener
        spinSpecialtyList.setOnItemSelectedListener(spinListener);
    }

    private void GetDrArray()
    {
        dbAdapter = new DbAdapter(this, "GetDrNamesArray", this);
        paramsApiUri[0] = "https://drappdb.firebaseio.com/DrProfiles.json?auth=" + VariablesGlobal.KeyToAccessFirebaseDB; //Before Firebase >>> //= VariablesGlobal.API_URI + "/api/values/doctors";
        paramsApiUri[1] = formData = "";
        paramsApiUri[2] = "GET";
        dbAdapter.execute(paramsApiUri);
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

    public void clk_SaveAppoint(View btn_v)
    {
        alert("","save", btn_v);
    }

    private void SaveAppoint(View view)
    {
        //chk if user is logged in:
        String userIdStr = getSharedPreferences("prefs",0).getString("Id_User", "");
        if(userIdStr.equals(""))
        {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }

        dbAdapter = new DbAdapter(this);

        //covert unix-datetime (in seconds) to string
        Date appointTime = new Date(dateTimeUnix * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa");
        AppointmentTime = sdf.format(appointTime);

        //bind model
        bModel = new Booking();
        userIdStr = getSharedPreferences("prefs", 0).getString("Id_User", "1");
        int userIdInt = Integer.parseInt(userIdStr);
        bModel.setId_User(userIdStr);
        bModel.setAppointmentTime(this.AppointmentTime);
        bModel.setClinic(txtV.getText().toString());
        bModel.setCreationTime(sdf.format( Calendar.getInstance().getTime() ));
        if(spinDrList.getSelectedItemPosition() == 0)
        {
            Snackbar.make(view, "Select a doctor", Snackbar.LENGTH_LONG).show();
            return;
        }
        bModel.setDoctor(VariablesGlobal.DrProfiles.get(spinDrList.getSelectedItemPosition() -1).name);
        bModel.setDRAVAILABLE("1");
        bModel.setId_Doc((VariablesGlobal.DrProfiles.get(spinDrList.getSelectedItemPosition() - 1)).id_doc);

        //make json from model
        formData = gson.toJson(bModel);

        //chk if u r coming from List of appoints or from Map
        if (app == null) //i.e. Coming from Map, hence a non-existing booking, so create a new one
        {

            paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/newAppointment";
            paramsApiUri[1] = formData;
            paramsApiUri[2] = "POST";
        }
        else
        {
            int appointId = getSharedPreferences("prefs", 0).getInt("Id_Appointment", 0);
            paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/UpdateAppoint/" + appointId;
            paramsApiUri[1] = formData;
            paramsApiUri[2] = "POST";
        }

        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);
        if(Bookings_AllActivity.instance != null)
        {
            Bookings_AllActivity.instance.finish();
        }
    }

    public void clk_CancelAppoint(View btn_v)
    {
        alert("", "cancel", btn_v);
    }

    private void CancelAppoint(View btn_v)
    {
        dbAdapter = new DbAdapter(this);
        int appointId = getSharedPreferences("prefs", 0).getInt("Id_Appointment", 0);
        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/DeleteAppointment/" + appointId;
        paramsApiUri[1] = formData = "";
        paramsApiUri[2] = "POST";
        dbAdapter.execute(paramsApiUri);
        Bookings_AllActivity.instance.finish();
    }

    @Override
    public void onResponseFromServer(String result, Context ctx)
    {

        lv_DrList = (ListView)((Activity)ctx).findViewById(R.id.lv_DoctorsList);
        DrList_Adapter drList_adapter = new DrList_Adapter((Activity)ctx, R.layout.eachuser, VariablesGlobal.DrProfiles);
        lv_DrList.setAdapter(drList_adapter);



        //========old code starts==========
     /*   if(app != null)
        {
            for (DrProfile dr : VariablesGlobal.DrProfiles)
            {
                if(dr.id_doc == app.getId_Doc())
                {
                    spinDrList.setSelection(VariablesGlobal.DrProfiles.indexOf(dr) + 1);
                    break;
                }
            }
        }

        //filter DrNames by Specialty
        filteredDrList();*/
        //========old code ends==========

    }

    @Override
    public void onResponseFromServer(List<Booking> allBookings, Context ctx) {

    }

    @Override
    public void onResponseFromServer(ArrayList<User> allUsersAdminSearched, Context ctx) {

    }

    private void filteredDrList()
    {
        spinSpecialtyList.setEnabled(false);
        VariablesGlobal.DrNamesListFiltered.clear();
        VariablesGlobal.DrNamesListFiltered.add("~Select Doctor~");
        for (int j = 0; j < VariablesGlobal.DrProfiles.size(); j++)
        {
            DrProfile Dr = VariablesGlobal.DrProfiles.get(j);
            if(!VariablesGlobal.filterDrNamesBy.equals("All"))
            {
                if(Dr.specialty.equals(VariablesGlobal.filterDrNamesBy))
                {
                    VariablesGlobal.DrNamesListFiltered.add(Dr.name/* + " (" + Dr.specialty + ")"*/);
                }
            }
            else
            {
                VariablesGlobal.DrNamesListFiltered.add(Dr.name);
            }
        }

        VariablesGlobal.spinAdapter.notifyDataSetChanged();
        spinSpecialtyList.setEnabled(true);
    }

    public void clk_drProfile(View view)
    {
        int DrSelectedIndex = spinDrList.getSelectedItemPosition();
        String DrSelectedName = spinDrList.getSelectedItem().toString();
        i = new Intent(this, DrProfileActivity.class);
        i.putExtra("DrSelectedIndex", DrSelectedIndex);
        i.putExtra("DrSelectedName", DrSelectedName);
        startActivity(i);
    }

    public void alert(String txtMsg, final String action, final View btn_view)
    {
        //region (1) set custom view for dialog
       LayoutInflater inflator = LayoutInflater.from(BookingDetailsActivity.this);
       final View yourCustomView = inflator.inflate(R.layout.custom_dialog, null);
        //endregion

        //region (2) init dialogue
        final AlertDialog dialog = new AlertDialog.Builder(BookingDetailsActivity.this)
               .setTitle("Do you want to proceed ?")//replace w "txtMsg"
               .setView(yourCustomView)
               .create();
        //endregion

        //region (3) set onClicks for custom dialog btns
        yourCustomView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switch (action)
                {
                    case "save":
                        SaveAppoint(btn_view);
                        break;
                    case "cancel":
                        CancelAppoint(btn_view);
                        break;
                }
            }
        });
        yourCustomView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
            }
        });
        //endregion

        //region (4) Display dialogue
        dialog.show();
        //endregion
    }
}
