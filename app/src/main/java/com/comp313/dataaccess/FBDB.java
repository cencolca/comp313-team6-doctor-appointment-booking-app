    package com.comp313.dataaccess;

    import android.content.Context;
    import android.content.SharedPreferences;
    import android.util.Log;

    import com.comp313.helpers.DataParser;
    import com.comp313.helpers.DownloadUrl;
    import com.comp313.models.User;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Query;
    import com.google.firebase.database.ValueEventListener;

    import java.io.IOException;
    import java.util.HashMap;
    import java.util.List;

    public class FBDB {

        public enum FB_REQUESTS
        {
            DrProfile,
            Clinics,
            Users
        }

        private DownloadUrl downloadUrl;
        private DataParser parser;
        private String jsonStr;

        private String baseUrl = "https://drappdb.firebaseio.com/";

        private Context ctx;

        public FBDB(Context _ctx)
        {
            ctx = _ctx;
            downloadUrl = new DownloadUrl();
            parser = new DataParser();
        }

        public Object request(FB_REQUESTS requestType, String authKey)
        {
            String url = baseUrl;

            switch(requestType)
            {
                case DrProfile:
                    url += "DrProfile.json";
                    url += "?auth=" + authKey;

                    jsonStr = getJsonStrFromUrl(url);

                    break;

                case Clinics:
                    url += "MockClinics.json";
                    url += "?auth=" + authKey;

                    jsonStr = getJsonStrFromUrl(url);

                    List<HashMap<String,String>> clinics = parseClinics(jsonStr);
                    return clinics;

                case Users:
                    url += "Users.json";// add auth key
                    url += "?auth=" + authKey;

                    jsonStr = getJsonStrFromUrl(url);

                    break;
            }

            return null;
        }

        private String getJsonStrFromUrl(String url)
        {
            String jsonStr = "";

            try
            {
                jsonStr = downloadUrl.readUrl(url);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return jsonStr;
        }

        private List<HashMap<String,String>> parseClinics(String json)
        {
            List<HashMap<String,String>> clinics = null;
            clinics = parser.parse(json);

            return clinics;
        }

        public boolean registerUser(User newUser)
        {
            boolean success = false;

            try {
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();

                String userId = myRef.child("Users").push().getKey();
                myRef.child(userId).setValue(newUser);

                //get shared preference
                SharedPreferences pref = ctx.getSharedPreferences("prefs", 0);

                // store userID to sharedPref
                pref.edit().putString("Id_User", userId).apply();

                // set role to 1 (patient)
                pref.edit().putString("role", "1").apply();

                success = true;

            }
            catch(Exception ex)
            {

            }

            return success;
        }

        boolean loginSuccess;






    }
