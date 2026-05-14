package com.marketplace.bookingservice.messaging;

import com.marketplace.bookingservice.config.RabbitMQConfig;
import com.marketplace.bookingservice.dto.BookingMessage;
import com.marketplace.bookingservice.dto.ServiceOfferDTO;
import com.marketplace.bookingservice.entity.Booking;
import com.marketplace.bookingservice.entity.Notification;
import com.marketplace.bookingservice.repository.BookingRepository;
import com.marketplace.bookingservice.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class BookingProcessor {

    @Autowired BookingRepository bookingRepo;
    @Autowired NotificationRepository notifRepo;
    @Autowired RabbitTemplate rabbitTemplate;

    RestTemplate restTemplate = new RestTemplate();

    private final String USER_SERVICE    = "http://localhost:8084/UserService-1.0-SNAPSHOT/api";
    private final String CATALOG_SERVICE = "http://localhost:8081";

    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)
    public void processBooking(BookingMessage msg) {

        Booking booking = bookingRepo.findById(msg.getBookingId()).orElseThrow();

        try {
            // Get offer from Catalog Service
            ServiceOfferDTO offer = restTemplate.getForObject(
                    CATALOG_SERVICE + "/offers/" + msg.getOfferId(),
                    ServiceOfferDTO.class
            );

            // Try to deduct from wallet via User Service
            Map<String, Object> deductBody = Map.of(
                    "customerId", msg.getCustomerId(),
                    "amount", offer.getPrice()
            );
            Map response = restTemplate.postForObject(
                    USER_SERVICE + "/users/internal/deduct",
                    deductBody, Map.class
            );

            boolean success = (Boolean) response.get("success");

            if (success) {
                // Confirm booking
                booking.setStatus("CONFIRMED");
                booking.setAmount(offer.getPrice());
                booking.setProviderId(offer.getProviderId());
                booking.setServiceCategory(offer.getCategory());
                bookingRepo.save(booking);

                // Notify both parties
                notifRepo.save(new Notification(msg.getCustomerId(), "BOOKING_CONFIRMED", booking.getId()));
                notifRepo.save(new Notification(offer.getProviderId(), "NEW_BOOKING", booking.getId()));

                rabbitTemplate.convertAndSend(RabbitMQConfig.CUSTOMER_NOTIFY_QUEUE, msg.getCustomerId());
                rabbitTemplate.convertAndSend(RabbitMQConfig.PROVIDER_NOTIFY_QUEUE, offer.getProviderId());

            } else {
                // Reject booking
                booking.setStatus("REJECTED");
                bookingRepo.save(booking);

                notifRepo.save(new Notification(msg.getCustomerId(), "BOOKING_REJECTED", booking.getId()));

                // Notify admin via direct exchange
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.PAYMENT_EXCHANGE,
                        RabbitMQConfig.PAYMENT_FAILED_KEY,
                        "Payment failed for booking " + booking.getId()
                );
            }

        } catch (Exception e) {
            booking.setStatus("REJECTED");
            bookingRepo.save(booking);
            e.printStackTrace();
        }
    }
}