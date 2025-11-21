package com.careforall.donation.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 *
 * Configures exchanges, queues, and bindings for donation events.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String DONATION_EXCHANGE = "donation.exchange";

    // Queues
    public static final String DONATION_CREATED_QUEUE = "donation.created.queue";
    public static final String DONATION_COMPLETED_QUEUE = "donation.completed.queue";
    public static final String DONATION_FAILED_QUEUE = "donation.failed.queue";
    public static final String DONATION_REFUNDED_QUEUE = "donation.refunded.queue";

    // Routing Keys
    public static final String DONATION_CREATED_ROUTING_KEY = "donation.created";
    public static final String DONATION_COMPLETED_ROUTING_KEY = "donation.completed";
    public static final String DONATION_FAILED_ROUTING_KEY = "donation.failed";
    public static final String DONATION_REFUNDED_ROUTING_KEY = "donation.refunded";

    /**
     * Declare donation exchange
     */
    @Bean
    public TopicExchange donationExchange() {
        return new TopicExchange(DONATION_EXCHANGE);
    }

    /**
     * Declare donation created queue
     */
    @Bean
    public Queue donationCreatedQueue() {
        return new Queue(DONATION_CREATED_QUEUE, true);
    }

    /**
     * Declare donation completed queue
     */
    @Bean
    public Queue donationCompletedQueue() {
        return new Queue(DONATION_COMPLETED_QUEUE, true);
    }

    /**
     * Declare donation failed queue
     */
    @Bean
    public Queue donationFailedQueue() {
        return new Queue(DONATION_FAILED_QUEUE, true);
    }

    /**
     * Declare donation refunded queue
     */
    @Bean
    public Queue donationRefundedQueue() {
        return new Queue(DONATION_REFUNDED_QUEUE, true);
    }

    /**
     * Bind donation created queue to exchange
     */
    @Bean
    public Binding donationCreatedBinding() {
        return BindingBuilder
            .bind(donationCreatedQueue())
            .to(donationExchange())
            .with(DONATION_CREATED_ROUTING_KEY);
    }

    /**
     * Bind donation completed queue to exchange
     */
    @Bean
    public Binding donationCompletedBinding() {
        return BindingBuilder
            .bind(donationCompletedQueue())
            .to(donationExchange())
            .with(DONATION_COMPLETED_ROUTING_KEY);
    }

    /**
     * Bind donation failed queue to exchange
     */
    @Bean
    public Binding donationFailedBinding() {
        return BindingBuilder
            .bind(donationFailedQueue())
            .to(donationExchange())
            .with(DONATION_FAILED_ROUTING_KEY);
    }

    /**
     * Bind donation refunded queue to exchange
     */
    @Bean
    public Binding donationRefundedBinding() {
        return BindingBuilder
            .bind(donationRefundedQueue())
            .to(donationExchange())
            .with(DONATION_REFUNDED_ROUTING_KEY);
    }

    /**
     * JSON message converter
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
