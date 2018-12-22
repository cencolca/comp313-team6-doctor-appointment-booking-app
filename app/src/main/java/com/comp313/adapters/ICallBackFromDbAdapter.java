package com.comp313.adapters;
/*
 * By: SHAFIQ-UR-REHMAN
 * Purpose: Interface to give ability to DbAdapter class to be able to call a method in calling class.
 */

import android.content.Context;

import com.comp313.models.Booking;
import com.comp313.models.User;

import java.util.ArrayList;
import java.util.List;

//Callback interface
public interface ICallBackFromDbAdapter
{
    void onResponseFromServer(String result, Context ctx);
    void onResponseFromServer(List<Booking> allBookings, Context ctx);
    void onResponseFromServer(ArrayList<User> allUsersAdminSearched, Context ctx);
}
