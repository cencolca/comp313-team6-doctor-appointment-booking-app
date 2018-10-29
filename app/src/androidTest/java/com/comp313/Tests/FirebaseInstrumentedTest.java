

package com.comp313.Tests;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.comp313.adapters.ICallBackFromDbAdapter;
import com.comp313.adapters.ISingleBookingCallback;
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
import static org.junit.Assert.assertTrue;
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

    // User ID from firebase DB used for testing
    String userID = "-LNuxoOVTPSOW932ZJc8";

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
        testUserBookings = new ArrayList<>();
        for (int x = 1; x <= expectedBookingCount; x ++) {
            Booking tempBooking = new Booking();
            tempBooking.setAppointmentTime(x + "");
            testUserBookings.add(tempBooking);
        }
    }


    // dummy booking list to compare to
    List<Booking> testUserBookings;
    // test if an appointment fetch returns the expected count
    @Test
    public void testIsAppointmentCountCorrect() throws InterruptedException {
        // use a sync object to use wait/notify
        final Object syncObject = new Object();


        // call the method under test
        db.testGetAllAppoints_Patient(userID, new ICallBackFromDbAdapter() {
            @Override
            public void onResponseFromServer(String result, Context ctx) {

            }

            @Override
            public void onResponseFromServer(List<Booking> allBookings, Context ctx) {

                // Test to see if they actually return correctly
                System.out.println("==TEST== userBookings.size: " + testUserBookings.size());
                System.out.println("==TEST== allBookings.size: " + allBookings.size());

                assertThat(testUserBookings.size(), is(allBookings.size()));

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

    Booking testBook;
    // get first appointment returned from firebase DB, by userID
    @Test
    public void testViewAppointment() throws InterruptedException
    {
        // use a sync object to use wait/notify
        final Object syncObject = new Object();

        // create test booking to compare with what we get from firebase DB
        testBook = new Booking();
        testBook.setClinic("Address : Markham Rd Walk-in Clinic : 1825 Markham Rd, Toronto");
        testBook.setAppointmentTime("Sun, 14 Oct 2018 11:00 PM");
        testBook.setCreationTime("Sun, 14 Oct 2018 11:07 PM");

        db.testGetAppointment(userID, new ISingleBookingCallback() {
            @Override
            public void onBookingRetrieved(Booking booking) {

                // assert some data
                assertThat(booking.getAppointmentTime(), is(testBook.getAppointmentTime()));

                assertThat(booking.getCreationTime(), is(testBook.getCreationTime()));

                assertThat(booking.getClinic(), is(testBook.getClinic()));

                synchronized (syncObject){
                    syncObject.notify();
                }
            }
        });


        synchronized (syncObject){
            syncObject.wait();
        }
    }

    @Test
    public void testUpdateAppointment() throws InterruptedException
    {
        // use a sync object to use wait/notify
        final Object syncObject = new Object();

        // first, get one appointment
        db.testGetAppointment(userID, new ISingleBookingCallback() {
            @Override
            public void onBookingRetrieved(Booking booking) {

                Booking currBooking = booking;

                // we need to the key update
                String appointmentKey = currBooking.getAppointmentKey();

                // update some value
                currBooking.setDoctor("Doctor TEST");

                // call the update method
                boolean success = db.updateBooking(currBooking, appointmentKey);

                assertThat(success, is(true));

                synchronized (syncObject)
                {
                    syncObject.notify();
                }
            }
        });

        synchronized (syncObject)
        {
            syncObject.wait();
        }
    }

    @Test
    public void testCancelAppointment() throws InterruptedException
    {
        // use a sync object to use wait/notify
        final Object syncObject = new Object();

        // first, get one appointment
        db.testGetAppointment(userID, new ISingleBookingCallback() {
            @Override
            public void onBookingRetrieved(Booking booking) {

                Booking currBooking = booking;

                // we need to the key update
                String appointmentKey = currBooking.getAppointmentKey();

                // call the update method
                boolean success = db.cancelBooking(appointmentKey);

                assertThat(success, is(true));

                synchronized (syncObject)
                {
                    syncObject.notify();
                }
            }
        });

        synchronized (syncObject)
        {
            syncObject.wait();
        }
    }

    // test if appointment booking returns appointment ID
    @Test
    public void testBookAppointment() throws InterruptedException
    {
        // create test booking to compare with what we get from firebase DB
        testBook = new Booking();
        testBook.setDoctor("Doctor TEST");
        testBook.setClinic("Address : Test Address Clinic : Test Clinic");
        testBook.setAppointmentTime("Sun, 14 Oct 2018 11:00 PM");
        testBook.setCreationTime("Sun, 14 Oct 2018 11:07 PM");

        String testBookingID = "";

        // initial assert to see if testBookingID is empty
        assertTrue(testBookingID.equals(""));

        testBookingID = db.testCreateBooking(testBook);

        // assert to see if testBookingID is no longer empty
        assertTrue(!testBookingID.equals(""));

        Log.v("TESTBookAppointment", "BookingID: " + testBookingID);
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
