package com.marketplace.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_QUEUE          = "booking.queue";
    public static final String CUSTOMER_NOTIFY_QUEUE  = "customer.notify.queue";
    public static final String PROVIDER_NOTIFY_QUEUE  = "provider.notify.queue";
    public static final String ADMIN_QUEUE            = "admin.payment.failed";
    public static final String PAYMENT_EXCHANGE       = "payments";
    public static final String PAYMENT_FAILED_KEY     = "PaymentFailed";

    @Bean public Queue bookingQueue()        { return new Queue(BOOKING_QUEUE, true); }
    @Bean public Queue customerNotifyQueue() { return new Queue(CUSTOMER_NOTIFY_QUEUE, true); }
    @Bean public Queue providerNotifyQueue() { return new Queue(PROVIDER_NOTIFY_QUEUE, true); }
    @Bean public Queue adminQueue()          { return new Queue(ADMIN_QUEUE, true); }

    @Bean
    public DirectExchange paymentsExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding adminBinding() {
        return BindingBuilder
                .bind(adminQueue())
                .to(paymentsExchange())
                .with(PAYMENT_FAILED_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}