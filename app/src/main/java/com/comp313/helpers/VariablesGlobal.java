package com.comp313.helpers;

import android.util.Pair;
import android.widget.ArrayAdapter;

import com.comp313.models.Booking;
import com.comp313.models.DrProfile;
import com.comp313.models.User;

import java.util.ArrayList;
import java.util.HashMap;
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
    public static List<DrProfile> DrProfiles =new ArrayList<>();
    public static String filterDrNamesBy = "All";
    public static   List<String> DrNamesListFiltered = new ArrayList<String>();
    public static ArrayAdapter spinAdapter;
    public static String KeyToAccessFirebaseDB = "xuMPG9VJbpjYXXPgPPAgwKYCA1kyx4Lvg4YNkz1a";
    public static List<Pair<String, Booking>> mapAppoints = new ArrayList<>();
    public static ArrayList<Booking> allAppoints = new ArrayList<>();
    public static List<Pair<String, User>> mapUsers = new ArrayList<>();
    public static ArrayList<User> allUsersAdminSearched = new ArrayList<>();
}
