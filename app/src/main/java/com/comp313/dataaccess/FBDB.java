    package com.comp313.dataaccess;

    import com.comp313.helpers.DataParser;
    import com.comp313.helpers.DownloadUrl;
    import com.comp313.models.User;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

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

        public FBDB()
        {
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
            boolean success = true;

            try {
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();

                myRef.child("Users").push().setValue(newUser);

            }
            catch(Exception ex)
            {
                success = false;
            }

            return success;
        }



    }
