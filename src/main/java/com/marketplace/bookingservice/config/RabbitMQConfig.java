package com.marketplace.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_QUEUE = "booking.queue";
    public static final String CUSTOMER_NOTIFY_QUEUE = "customer.notify.queue";
    public static final String PROVIDER_NOTIFY_QUEUE = "provider.notify.queue";
    public static final String PAYMENT_EXCHANGE = "payments";
    public static final String PAYMENT_FAILED_KEY = "PaymentFailed";
    public static final String ADMIN_QUEUE = "admin.payment.failed";

    @Bean public Queue bookingQueue() { return new Queue(BOOKING_QUEUE, true); }
    @Bean public Queue customerNotifyQueue() { return new Queue(CUSTOMER_NOTIFY_QUEUE, true); }
    @Bean public Queue providerNotifyQueue() { return new Queue(PROVIDER_NOTIFY_QUEUE, true); }
    @Bean public Queue adminQueue() { return new Queue(ADMIN_QUEUE, true); }

    @Bean
    public DirectExchange paymentsExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding adminBinding() {
        return BindingBuilder.bind(adminQueue()).to(paymentsExchange()).with(PAYMENT_FAILED_KEY);
    }
}