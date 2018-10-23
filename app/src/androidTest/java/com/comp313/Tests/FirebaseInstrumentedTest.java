

package com.comp313.Tests;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.comp313.adapters.ICallBackFromDbAdapter;
import com.comp313.dataaccess.FBDB;
import com.comp313.models.Booking;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseInstrumentedTest {

    private FirebaseTestCaller testCaller;

    @Mock
    private FBDB mockdb;

    @Captor
    private ArgumentCaptor<ICallBackFromDbAdapter> cbCaptor;



    Context ctx;


    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        ctx = InstrumentationRegistry.getContext();
//        mockdb = new FBDB(ctx);
        testCaller = new FirebaseTestCaller(mockdb);
    }



    @Test
    public void getAllAppoints_Pateint() {

        int expectedBookingCount = 10;

        List<Booking> userBookings = new ArrayList<Booking>();

        for (int x = 1; x <= expectedBookingCount; x ++) {
            Booking tempBooking = new Booking();
            tempBooking.setAppointmentTime(x + "");
            userBookings.add(tempBooking);
        }


        String userID = "-LNuxoOVTPSOW932ZJc8";

        testCaller.getAppointmentsByID(userID);

        System.out.println("Verifying...");

        verify(mockdb, times(1)).testGetAllAppoints_Patient(eq(userID), cbCaptor.capture());

        // initial assert before callback
        assertThat(testCaller.getBookings().isEmpty(), is(true));

        // trigger reply from callback
        cbCaptor.getValue().onResponseFromServer(userBookings, ctx);

        // assert if same
        assertThat(testCaller.getBookings().size(), is(equalTo(userBookings.size())));
    }

//    @Override
//    public void onResponseFromServer(String result, Context ctx) {
//    }
//
//    @Override
//    public void onResponseFromServer(List<Booking> allBookings, Context ctx) {
//        final int expectedBookingsCount = 5;
//        assertEquals(expectedBookingsCount, allBookings.size());
//    }
}
