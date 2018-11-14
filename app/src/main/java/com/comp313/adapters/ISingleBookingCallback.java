package com.comp313.adapters;

import com.comp313.models.Booking;

public interface ISingleBookingCallback {
    void onBookingRetrieved(Booking booking);
}
