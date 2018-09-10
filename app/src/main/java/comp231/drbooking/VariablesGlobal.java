package comp231.drbooking;

import android.support.v4.content.ContextCompat;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Set any variables that can be used by multiple classes
 * e.g. hostname for API in Azure etc. For testing wecan replace that with localhost:<postNumber> etc
 */
public class VariablesGlobal
{
    public static String API_URI = "http://drappapi.azurewebsites.net";

    public static   List<String> DrNamesList = new ArrayList<String>(){{add("Please Wait"); add("Fetching List of Doctors");}};
    public static List<Model_DrProfile> DrProfiles =new ArrayList<Model_DrProfile>();
    public static String filterDrNamesBy = "All";
    public static   List<String> DrNamesListFiltered = new ArrayList<String>();


    public static ArrayAdapter spinAdapter;


}
