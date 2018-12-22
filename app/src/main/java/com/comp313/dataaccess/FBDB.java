    package com.comp313.dataaccess;

    import android.app.Activity;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.util.Log;
    import android.util.Pair;
    import android.widget.Toast;

    import com.comp313.activities.Bookings_AllActivity;
    import com.comp313.activities.DashboardActivity;
    import com.comp313.activities.LoginActivity;
    import com.comp313.adapters.ICallBackFromDbAdapter;
    import com.comp313.adapters.ISingleBookingCallback;
    import com.comp313.helpers.DataParser;
    import com.comp313.helpers.DownloadUrl;
    import com.comp313.helpers.VariablesGlobal;
    import com.comp313.models.Booking;
    import com.comp313.models.User;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Query;
    import com.google.firebase.database.ValueEventListener;
    import com.google.gson.Gson;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.Iterator;
    import java.util.List;
    import java.util.Map;

    public class FBDB {



        public enum FB_REQUESTS
        {
            DrProfile,
            Clinics,
            Users
        }

        private DownloadUrl downloadUrl;
        private DataParser parser;
        private String jsonStr, NameOfUser;
        private boolean isTimeAvailable = false;
        private Gson gson;
        ICallBackFromDbAdapter callBk;
        FirebaseDatabase fbdb;
        DatabaseReference dbRef;


        private String baseUrl = "https://drappdb.firebaseio.com/";

        private Context ctx;

        public FBDB(Context _ctx)
        {
            ctx = _ctx;
            downloadUrl = new DownloadUrl();
            parser = new DataParser();
        }

        public FBDB(Context _ctx, ICallBackFromDbAdapter _callBk)
        {
            ctx = _ctx;
            callBk = _callBk;
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
                DatabaseReference myRef = database.getReference("Users");

                String userId = myRef.push().getKey();
                newUser.setId_User(userId);
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

        public boolean getUserById(String userIdStr)
        {
            boolean success = false;
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            Query query = myRef.child("Users").orderByKey().equalTo(userIdStr);
            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {
                        if(dataSnapshot.hasChildren())
                        {
                            try
                            {
                                DataSnapshot snap = dataSnapshot.getChildren().iterator().next();
                                User user = snap.getValue(User.class);
                                //store name of user -> needed for method getAllAppoints_Dr
                                //NameOfUser = user.getNameOfUser();
                                //
                                Gson gson = new Gson();
                                String userJsonStr = gson.toJson(user);
                                //
                                callBk.onResponseFromServer(userJsonStr, ctx);
                            }
                            catch (Exception e)
                            {
                                Log.e("getUserById()", e.getMessage());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
            success = true;
            return success;
        }

        public boolean updateUserProfile(User user, String userIdStr)
        {
            boolean success = false;
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference myRef = db.getReference("Users").child(userIdStr);
            myRef.setValue(user);
            success = true;
            return success;
        }

        public void createBooking(final Booking newBooking)
        {
            //boolean success = false;

            try {
                // Write a message to the database
                fbdb = FirebaseDatabase.getInstance();
                dbRef = fbdb.getReference("Appointments");
                //check if appointment time is available
                Query query = dbRef.orderByChild("doctor").equalTo(newBooking.getDoctor());
                                                          //.equalTo(newBooking.getAppointmentTime() , "appointmentTime")
                                                          //.equalTo(newBooking.getClinic() , "clinic");
                query.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChildren())
                            {
                                String key;
                                Pair p;

                                //https://stackoverflow.com/questions/50840053/iterator-next-is-not-working
                                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                                try
                                {
                                    while (it.hasNext())
                                    {
                                        key = it.next().getKey();
                                        Booking existingBooking = dataSnapshot.child(key).getValue(Booking.class);
                                        String existingBookTime = existingBooking.getAppointmentTime();
                                        String existingBookClinic = existingBooking.getClinic();
                                        String newgBookTime = newBooking.getAppointmentTime();
                                        String newBookClinic = newBooking.getClinic();


                                        if(existingBookTime.equals(newgBookTime) && existingBookClinic.equals(newBookClinic))
                                        {
                                            isTimeAvailable = false;
                                            break;
                                        }
                                        else
                                        {
                                            isTimeAvailable = true;
                                        }
                                    }//while-loop ends

                                    //outside while-loop
                                    if (isTimeAvailable)
                                    {
                                        //if time is available then create a booking
                                        String bookingId = dbRef.push().getKey();
                                        dbRef.child(bookingId).setValue(newBooking);
                                        callBk.onResponseFromServer("timeIsAvailable" , ctx);
                                    }
                                    else
                                     {
                                        callBk.onResponseFromServer("timeUnAvailable" , ctx);
                                     }
                                }
                                catch(Exception e)
                                {
                                    Log.e("LoginError", e.getMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
           /*     //if time is available then create a booking
                String bookingId = tblAppointment.push().getKey();
                tblAppointment.child(bookingId).setValue(newBooking);*/

                //success = true;
            }
            catch(Exception ex)
            {
                Log.e("> > Firebase Err: ", ex.getMessage());
            }

            //return true;
        }

        public void updateBooking(final Booking newBooking, final String appIdStr)
        {
            //boolean success = false;

            try {
                //Update an existing appointment to the database
                fbdb = FirebaseDatabase.getInstance();
                dbRef = fbdb.getReference("Appointments");
                //
                Query query = dbRef.orderByChild("doctor").equalTo(newBooking.getDoctor());
                query.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChildren())
                            {
                                String key;
                                Pair p;

                                //https://stackoverflow.com/questions/50840053/iterator-next-is-not-working
                                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                                try
                                {
                                    while (it.hasNext())
                                    {
                                        key = it.next().getKey();
                                        Booking existingBooking = dataSnapshot.child(key).getValue(Booking.class);
                                        String existingBookTime = existingBooking.getAppointmentTime();
                                        String existingBookClinic = existingBooking.getClinic();
                                        String newgBookTime = newBooking.getAppointmentTime();
                                        String newBookClinic = newBooking.getClinic();


                                        if(existingBookTime.equals(newgBookTime) && existingBookClinic.equals(newBookClinic))
                                        {
                                            isTimeAvailable = false;
                                            break;
                                        }
                                        else
                                        {
                                            isTimeAvailable = true;
                                        }
                                    }//while-loop ends

                                    //outside while-loop
                                    if (isTimeAvailable)
                                    {
                                        //if time is available then create a booking
                                        dbRef = fbdb.getReference("Appointments").child(appIdStr);
                                        dbRef.setValue(newBooking);
                                        callBk.onResponseFromServer("timeIsAvailable" , ctx);
                                    }
                                    else
                                    {
                                        callBk.onResponseFromServer("timeUnAvailable" , ctx);
                                    }
                                }
                                catch(Exception e)
                                {
                                    Log.e("LoginError", e.getMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
                //
               /* dbRef = fbdb.getReference("Appointments").child(appIdStr);
                dbRef.setValue(newBooking);*/

                //success = true;
            }
            catch(Exception ex)
            {
                Log.e("> > Firebase Err: ", ex.getMessage());
            }

            //return success;
        }

        public boolean getAllAppoints_Pateint(String userIdStr)
        {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            Query query = myRef.child("Appointments").orderByChild("id_User").equalTo(userIdStr);
            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChildren())
                        {
                            gson = new Gson();
                            VariablesGlobal.allAppoints.clear();// = new ArrayList<>();
                            VariablesGlobal.mapAppoints.clear();// = new HashMap<>();
                            String key;
                            Pair p;

                            //https://stackoverflow.com/questions/50840053/iterator-next-is-not-working
                            Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                            try
                            {
                                while (it.hasNext())
                                {
                                    key = it.next().getKey();
                                    p = new Pair(key , dataSnapshot.child(key).getValue(Booking.class));

                                    VariablesGlobal.mapAppoints.add(p);
                                    //allAppoints.add(it.next().getValue(Booking.class));
                                }
                            }
                            catch(Exception e)
                            {
                                Log.e("LoginError", e.getMessage());
                            }

                            for(Pair<String, Booking> pair : VariablesGlobal.mapAppoints)
                            {
                                VariablesGlobal.allAppoints.add(pair.second);
                            }

                            callBk.onResponseFromServer(VariablesGlobal.allAppoints, ctx);

                            Log.e("LoginError", ". . . . . . ");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError)
                {
                    Log.e("The read failed: " ,firebaseError.getMessage());
                }
            });

            return true;
        }

        public boolean getAllAppoints_Dr(String userIdStr)
        {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            //region >>> get doctor's name
            NameOfUser = ctx.getSharedPreferences("prefs", 0).getString("Name_of_User", "");
            //endregion

            //region >>> get all appointments for doctor
            Query queryAppointments = myRef.child("Appointments").orderByChild("doctor").equalTo(NameOfUser);
            queryAppointments.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChildren())
                        {
                            gson = new Gson();
                            VariablesGlobal.allAppoints.clear();// = new ArrayList<>();
                            VariablesGlobal.mapAppoints.clear();// = new HashMap<>();
                            String key;
                            Pair p;

                            //https://stackoverflow.com/questions/50840053/iterator-next-is-not-working
                            Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                            try
                            {
                                while (it.hasNext())
                                {
                                    key = it.next().getKey();
                                    p = new Pair(key , dataSnapshot.child(key).getValue(Booking.class));

                                    VariablesGlobal.mapAppoints.add(p);
                                    //allAppoints.add(it.next().getValue(Booking.class));
                                }
                            }
                            catch(Exception e)
                            {
                                Log.e("Error", e.getMessage());
                            }

                            for(Pair<String, Booking> pair : VariablesGlobal.mapAppoints)
                            {
                                VariablesGlobal.allAppoints.add(pair.second);
                            }

                            callBk.onResponseFromServer(VariablesGlobal.allAppoints, ctx);

                            Log.e("Error", ". . . . . . ");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError)
                {
                    Log.e("The read failed: " ,firebaseError.getMessage());
                }
            });
            //endregion

            return true;
        }

        public boolean cancelBooking(String appIdStr)
        {
            boolean success = true;
            try {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Appointments").child(appIdStr);

                myRef.setValue(null);
            }
            catch (Exception ex)
            {
                success = false;
            }

            return success;
        }

        public void searchUserByName(String stUserName)
        {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            //https://github.com/firebase/FirebaseUI-Android/issues/48
            Query queryUsers = myRef.child("Users").orderByChild("loginName").startAt(stUserName).endAt(stUserName+"\uf8ff");
            queryUsers.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.exists())
                    {
                        if (dataSnapshot.hasChildren())
                        {
                            gson = new Gson();
                            VariablesGlobal.allUsersAdminSearched.clear();// = new ArrayList<>();
                            VariablesGlobal.mapUsers.clear();// = new HashMap<>();
                            String key;
                            Pair p;
                            Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                            try
                            {
                                while (it.hasNext())
                                {
                                    key = it.next().getKey();
                                    p = new Pair(key , dataSnapshot.child(key).getValue(User.class));
                                    VariablesGlobal.mapUsers.add(p);

                                }
                            }
                            catch(Exception e)
                            {
                                Log.e("Error", e.getMessage());
                            }

                            for(Pair<String, User> pair : VariablesGlobal.mapUsers)
                            {
                                VariablesGlobal.allUsersAdminSearched.add(pair.second);
                            }

                            callBk.onResponseFromServer(VariablesGlobal.allUsersAdminSearched, ctx);


                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        public void deleteUser(String id_userEditing)
        {
            try {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Users").child(id_userEditing);
                myRef.setValue(null);
            }
            catch (Exception ex)
            {
                //success = false;
            }
        }

        boolean loginSuccess;


        //region FOR TESTING

        public void testGetAllAppoints_Patient(String userIdStr, final ICallBackFromDbAdapter cb)
        {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            Query query = myRef.child("Appointments").orderByChild("id_User").equalTo(userIdStr);
            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChildren())
                        {
                            gson = new Gson();
                            VariablesGlobal.allAppoints.clear();// = new ArrayList<>();
                            VariablesGlobal.mapAppoints.clear();// = new HashMap<>();
                            String key;
                            Pair p;

                            //https://stackoverflow.com/questions/50840053/iterator-next-is-not-working
                            Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                            try
                            {
                                while (it.hasNext())
                                {
                                    key = it.next().getKey();
                                    p = new Pair(key , dataSnapshot.child(key).getValue(Booking.class));

                                    VariablesGlobal.mapAppoints.add(p);
                                    //allAppoints.add(it.next().getValue(Booking.class));
                                }
                            }
                            catch(Exception e)
                            {
                                Log.e("LoginError", e.getMessage());
                            }

                            for(Pair<String, Booking> pair : VariablesGlobal.mapAppoints)
                            {
                                VariablesGlobal.allAppoints.add(pair.second);
                            }

                            cb.onResponseFromServer(VariablesGlobal.allAppoints, ctx);

                            Log.e("LoginError", ". . . . . . ");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError)
                {
                    Log.e("The read failed: " ,firebaseError.getMessage());
                }
            });

        }

        public void testGetAppointment(String userID, final ISingleBookingCallback cb)
        {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            Query query = myRef.child("Appointments").orderByChild("id_User").equalTo(userID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.hasChildren()) {

                            DataSnapshot snap = dataSnapshot.getChildren().iterator().next();

                            Booking booking = snap.getValue(Booking.class);

                            booking.setAppointmentKey(snap.getKey());

                            cb.onBookingRetrieved(booking);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public String testCreateBooking(Booking newBooking)
        {
            String bookingId = "";

            try {
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Appointments");

                bookingId = myRef.push().getKey();
                myRef.child(bookingId).setValue(newBooking);

            }
            catch(Exception ex)
            {
                Log.e("> > Firebase Err: ", ex.getMessage());
            }

            return bookingId;
        }

        //endregion






    }
