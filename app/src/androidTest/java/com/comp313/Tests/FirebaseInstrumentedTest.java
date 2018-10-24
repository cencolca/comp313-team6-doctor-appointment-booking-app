

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

    //region mock objects initialization
    @Mock
    private FBDB mockdb;

    @Captor
    private ArgumentCaptor<ICallBackFromDbAdapter> cbCaptor;
    //endregion

    // android context
    Context ctx;

    FBDB db;

    // setup objects - the @Before decorator will make sure that this method will be called first
    @Before
    public void setUp()
    {
        db = new FBDB(ctx);
        MockitoAnnotations.initMocks(this);
        ctx = InstrumentationRegistry.getContext();
        testCaller = new FirebaseTestCaller(mockdb);


        // ==================================
        // ==== Appointments ================
        // ==================================

        // since writing this test (2018-10-24), we expect 10 appointments from the given userID
        int expectedBookingCount = 10;
        // setup dummy booking list
        userBookings = new ArrayList<>();
        for (int x = 1; x <= expectedBookingCount; x ++) {
            Booking tempBooking = new Booking();
            tempBooking.setAppointmentTime(x + "");
            userBookings.add(tempBooking);
        }
    }



    // dummy booking list to compare to
    List<Booking> userBookings;

    // test if an appointment fetch returns the expected count
    @Test
    public void testIsAppointmentCountCorrect() throws InterruptedException {
        // use a sync object to use wait/notify
        final Object syncObject = new Object();

        String userID = "-LNuxoOVTPSOW932ZJc8";

        // call the method under test
        db.testGetAllAppoints_Patient(userID, new ICallBackFromDbAdapter() {
            @Override
            public void onResponseFromServer(String result, Context ctx) {

            }

            @Override
            public void onResponseFromServer(List<Booking> allBookings, Context ctx) {

                // Test to see if they actually return correctly
                System.out.println("==TEST== userBookings.size: " + userBookings.size());
                System.out.println("==TEST== allBookings.size: " + allBookings.size());

                assertThat(userBookings.size(), is(allBookings.size()));

                // notify our object that we have a result
                synchronized (syncObject){
                    syncObject.notify();
                }
            }
        });

        // wait for notification from callback
        synchronized (syncObject){
            syncObject.wait();
        }
    }

    //region Test that uses mock - currently not in use
    @Test
    public void getAllAppoints_Pateint_mock() {

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
//        assertThat(testCaller.getBookings().isEmpty(), is(true));

        // trigger reply from callback
        cbCaptor.getValue().onResponseFromServer(userBookings, ctx);

        System.out.println("dummyBookings size: " + userBookings.size());

        // assert if same
        assertThat(testCaller.getBookings().size(), is(equalTo(userBookings.size())));
    }
    //endregion

}
