package com.marketplace.bookingservice.dto;

import java.io.Serializable;

public class BookingMessage implements Serializable {
    private Long bookingId;
    private Long customerId;
    private Long offerId;

    public BookingMessage() {}

    public BookingMessage(Long bookingId, Long customerId, Long offerId) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.offerId = offerId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }
}