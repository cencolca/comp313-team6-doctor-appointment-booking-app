package comp231.drbooking;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Activity to either display a new booking details or existing booking details once user clicks on an item in list of all bookings
 */
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookingDetails extends BaseActivity implements ICallBackFromDbAdapter
{

    //region Variables
    boolean isYES = false;
    TextView dateTxtV, timeTxtV, txtV;
    Button btnCancelApp;
    Spinner spinDrList, spinSpecialtyList;
    Calendar cal;
    String formData, dateStr, timeStr,AppointmentTime;
    Long dateTimeUnix;
    DbAdapter dbAdapter;
    Model_Booking bModel, app;
    Gson gson;
    Object[] paramsApiUri;
    public static BookingDetails instance;
    AdapterView.OnItemSelectedListener spinListener;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        instance = this;
        //
        paramsApiUri = new Object[3];
        gson = new Gson();
        //
        VariablesGlobal.DrNamesListFiltered.clear();
        //ref to views
        txtV = findViewById(R.id.txtBookingActivity);
        spinDrList = findViewById(R.id.spinDrList);
        spinSpecialtyList = findViewById(R.id.spinSpecialtyList);
        VariablesGlobal.spinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, VariablesGlobal.DrNamesListFiltered);
        spinDrList.setAdapter(VariablesGlobal.spinAdapter);

        btnCancelApp = (Button)findViewById(R.id.btnCancelApp);
        btnCancelApp.setVisibility(View.INVISIBLE);

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
        txtV.setText("Address : " + getIntent().getStringExtra("infoWinTitle"));

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
                                BookingDetails.this,
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
                                BookingDetails.this,
                                timeChangeListener,
                                cal.get(Calendar.HOUR),
                                cal.get(Calendar.MINUTE),
                                false
                        ).show();
            }
        });
        //endregion

        //
        //region if editing existing booking sent from Booking_All
        String jsonAppointment = getIntent().getStringExtra("appointment");
        app = gson.fromJson(jsonAppointment, Model_Booking.class);
        if(app != null)
        {
            btnCancelApp.setVisibility(View.VISIBLE);

            getSharedPreferences("prefs", 0).edit().putInt("Id_Appointment", app.Id_Appointment).commit();
            txtV.setText(app.Clinic);

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa");
            Date dt = null;
            try
            {
                dt = sdf.parse(app.AppointmentTime);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            sdf = new SimpleDateFormat("EEE, d MMM yyyy");
            dateTxtV.setText(sdf.format(dt));
            sdf = new SimpleDateFormat("hh:mm aaa");
            timeTxtV.setText(sdf.format(dt));

            spinDrList.setSelection(VariablesGlobal.DrNamesList.indexOf(app.Doctor));//GetDrArray() is slower so this line of code fixed selected index to '0' unless return bk to list of appoints & come bk
            cal.setTimeInMillis(dt.getTime());
        }
        //endregion

        //set min to 00 so that TimePicker never launches to show any other min
        cal.set(Calendar.MINUTE, 0);
        updateDateTxtV();
        updateTimeTxtV();

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
        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/doctors";
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
            Intent i = new Intent(this, Login.class);
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
        bModel = new Model_Booking();
        userIdStr = getSharedPreferences("prefs", 0).getString("Id_User", "1");
        int userIdInt = Integer.parseInt(userIdStr);
        bModel.Id_User = userIdInt;
        bModel.AppointmentTime = this.AppointmentTime;
        bModel.Clinic = txtV.getText().toString();
        bModel.CreationTime = sdf.format( Calendar.getInstance().getTime() );
        if(spinDrList.getSelectedItemPosition() == 0)
        {
            Snackbar.make(view, "Select a doctor", Snackbar.LENGTH_LONG).show();
            return;
        }
        bModel.Doctor = VariablesGlobal.DrProfiles.get(spinDrList.getSelectedItemPosition() -1).name;
        bModel.DRAVAILABLE = "1";
        bModel.Id_Doc = (VariablesGlobal.DrProfiles.get(spinDrList.getSelectedItemPosition() - 1)).id_doc;

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
        if(Bookings_All.instance != null)
        {
            Bookings_All.instance.finish();
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
        Bookings_All.instance.finish();
    }

    @Override
    public void onResponseFromServer(String result, Context ctx)
    {
        if(app != null)
        {
            for (Model_DrProfile dr : VariablesGlobal.DrProfiles)
            {
                if(dr.id_doc == app.Id_Doc)
                {
                    spinDrList.setSelection(VariablesGlobal.DrProfiles.indexOf(dr) + 1);
                    break;
                }
            }
        }

        //filter DrNames by Specialty
        filteredDrList();
    }

    private void filteredDrList()
    {
        spinSpecialtyList.setEnabled(false);
        VariablesGlobal.DrNamesListFiltered.clear();
        VariablesGlobal.DrNamesListFiltered.add("~Select Doctor~");
        for (int j = 0; j < VariablesGlobal.DrProfiles.size(); j++)
        {
            Model_DrProfile Dr = VariablesGlobal.DrProfiles.get(j);
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
        i = new Intent(this, DrProfile.class);
        i.putExtra("DrSelectedIndex", DrSelectedIndex);
        i.putExtra("DrSelectedName", DrSelectedName);
        startActivity(i);
    }

    public void alert(String txtMsg, final String action, final View btn_view)
    {
        //region (1) set custom view for dialog
       LayoutInflater inflator = LayoutInflater.from(BookingDetails.this);
       final View yourCustomView = inflator.inflate(R.layout.custom_dialog, null);
        //endregion

        //region (2) init dialogue
        final AlertDialog dialog = new AlertDialog.Builder(BookingDetails.this)
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
