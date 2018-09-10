package comp231.drbooking;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Display a list of all bookings for a logged-in user. User can click any one booking to see details of it or to edit/cancel it
 */
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedList;
import java.util.List;

public class Bookings_All extends BaseActivity implements ICallBackFromDbAdapter
{

    //region Vars
    DbAdapter dbAdapter;
    Gson gson;
    Object[] paramsApiUri;
    ListView listAllAppV;
    String userIdStr, roleStr;
    public static Bookings_All instance;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings_all);
        instance = this;
        listAllAppV = (ListView)findViewById(R.id.listAllAppoints);
        gson = new Gson();
        paramsApiUri = new Object[3];
        userIdStr = getSharedPreferences("prefs", 0).getString("Id_User", "1");
        roleStr = getSharedPreferences("prefs", 0).getString("role", "");

switch (roleStr)
{
    case "1":
        LoadAllAppoints();
        break;
    case "2":
        LoadAllAppointsForDr();
        break;
    case "":
        break;
}
if(BookingDetails.instance != null)
{
    BookingDetails.instance.finish();
}

        if (MapsActivity.instance != null)
        {
            MapsActivity.instance.finish();
        }
    }

    @Override
    public void onResponseFromServer(String result, Context ctx)
    {
        if(!Bookings_All.this.isFinishing())
        {
            Toast.makeText(ctx, "Call Back successful" , Toast.LENGTH_SHORT).show();
        }
        Log.e("Call Back Success", "==== >>>>> Call Back Success");

        //extract Array of Appoints from json-str
        try
        {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObj = jsonArray.getJSONObject(0);
            String appointsJsonArrayStr = jsonObj.getString("Appointments");
            JSONArray appointsJsonArray = new JSONArray(appointsJsonArrayStr);

            List<Model_Booking> allAppList = new LinkedList<Model_Booking>();

            Model_Booking b;

            for (int i = 0; i <appointsJsonArray.length(); i++)
            {
                JSONObject j = appointsJsonArray.getJSONObject(i);

                b = new Model_Booking();
                b.Id_Appointment = Integer.parseInt( j.getString("Id_Appointment") );
                b.Id_User = Integer.parseInt( j.getString("Id_User") );
                b.Id_Doc = Integer.parseInt( j.getString("Id_Doc") );
                b.Clinic =  j.getString("Clinic");
                b.Doctor =  j.getString("Doctor");
                b.AppointmentTime =  j.getString("AppointmentTime");
                b.CreationTime =  j.getString("CreationTime");

                //list for Docs has 1 extra prop "PatientName". List for patients doesn't. See sample JSON fo Docs below:
                if(j.has("PatientName"))
                {
                    b.User = j.getString("PatientName");//name of patient
                }
                allAppList.add(b);
            }

            listAllAppV = (ListView)((Activity)ctx).findViewById(R.id.listAllAppoints);
            Booking_Adapter adapter = new Booking_Adapter((Activity) ctx, R.layout.eachbooking, allAppList);

            listAllAppV.setAdapter(adapter);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void LoadAllAppoints()
    {
        dbAdapter = new DbAdapter(Bookings_All.this, new Bookings_All());//new Bookings_All() just to give access to DbAdapter to onResponseFromServer()

        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/Appointments/" + userIdStr;
        paramsApiUri[1] = "";//formData not needed for this GET req since user_id is appended to URL
        paramsApiUri[2] = "GET";

        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);
    }

    private void LoadAllAppointsForDr()
    {
        dbAdapter = new DbAdapter(Bookings_All.this, new Bookings_All());//new Bookings_All() just to give access to DbAdapter to onResponseFromServer()

        paramsApiUri[0] = VariablesGlobal.API_URI + "/api/values/AppointmentsForDr/" + userIdStr;
        paramsApiUri[1] = "";//formData not needed for this GET req since user_id is appended to URL
        paramsApiUri[2] = "GET";
        //pass args to AsyncTask to read db
        dbAdapter.execute(paramsApiUri);
    }



}
