package com.marketplace.bookingservice.controller;

import com.marketplace.bookingservice.config.RabbitMQConfig;
import com.marketplace.bookingservice.dto.BookingMessage;
import com.marketplace.bookingservice.entity.Booking;
import com.marketplace.bookingservice.entity.Notification;
import com.marketplace.bookingservice.repository.BookingRepository;
import com.marketplace.bookingservice.repository.NotificationRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired BookingRepository bookingRepo;
    @Autowired NotificationRepository notifRepo;
    @Autowired RabbitTemplate rabbitTemplate;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Long> body) {
        Booking booking = new Booking();
        booking.setCustomerId(body.get("customerId"));
        booking.setOfferId(body.get("offerId"));
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        bookingRepo.save(booking);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_QUEUE,
                new BookingMessage(booking.getId(), body.get("customerId"), body.get("offerId"))
        );

        return ResponseEntity.accepted().body(Map.of(
                "message", "Booking received, processing...",
                "bookingId", booking.getId()
        ));
    }

    // GET /bookings/admin/transactions
    @GetMapping("/admin/transactions")
    public ResponseEntity<?> getAllTransactions() {
        List<Booking> all = bookingRepo.findAll();
        return ResponseEntity.ok(Map.of(
                "totalBookings", all.size(),
                "bookings", all
        ));
    }

    @GetMapping("/customer/{customerId}")
    public List<Booking> getCustomerBookings(@PathVariable Long customerId) {
        return bookingRepo.findByCustomerId(customerId);
    }

    @GetMapping("/provider/{providerId}")
    public List<Booking> getProviderBookings(@PathVariable Long providerId) {
        return bookingRepo.findByProviderId(providerId);
    }

    @GetMapping("/all")
    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    @GetMapping("/notifications/{userId}")
    public List<Notification> getNotifications(@PathVariable Long userId) {
        return notifRepo.findByUserId(userId);
    }
}