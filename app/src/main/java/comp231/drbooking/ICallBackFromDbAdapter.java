package comp231.drbooking;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Interface to give ability to DbAdapter class to be able to call a method in calling class.
 */

import android.content.Context;

//Callback interface
public interface ICallBackFromDbAdapter
{
    void onResponseFromServer(String result, Context ctx);
}
