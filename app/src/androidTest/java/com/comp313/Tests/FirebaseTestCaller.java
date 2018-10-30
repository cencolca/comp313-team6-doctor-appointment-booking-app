package com.comp313.Tests;

import android.content.Context;

import com.comp313.adapters.ICallBackFromDbAdapter;
import com.comp313.dataaccess.FBDB;
import com.comp313.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class FirebaseTestCaller implements ICallBackFromDbAdapter {

    FBDB db;

    List<Booking> bookings = new ArrayList<Booking>();

    public FirebaseTestCaller(FBDB mockdb)
    {
        db = mockdb;
    }

    public void getAppointmentsByID(String userId)
    {
        db.testGetAllAppoints_Patient(userId,this);
    }

    public List<Booking> getBookings()
    {
        return this.bookings;
    }

    @Override
    public void onResponseFromServer(String result, Context ctx) {

    }

    @Override
    public void onResponseFromServer(List<Booking> allBookings, Context ctx) {
        this.bookings = allBookings;

        System.out.println("testCaller bookings size: " + allBookings.size());
    }
}
