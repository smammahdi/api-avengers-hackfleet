package com.careforall.notification.config;

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
 * Configures exchanges, queues, and bindings for consuming donation, campaign, and payment events.
 */
@Configuration
public class RabbitMQConfig {

    // Donation event configuration
    public static final String DONATION_EXCHANGE = "donation.exchange";
    public static final String DONATION_QUEUE = "donation.notification.queue";
    public static final String DONATION_ROUTING_KEY = "donation.completed";

    // Campaign event configuration
    public static final String CAMPAIGN_EXCHANGE = "campaign.exchange";
    public static final String CAMPAIGN_CREATED_QUEUE = "campaign.created.notification.queue";
    public static final String CAMPAIGN_COMPLETED_QUEUE = "campaign.completed.notification.queue";
    public static final String CAMPAIGN_CREATED_ROUTING_KEY = "campaign.created";
    public static final String CAMPAIGN_COMPLETED_ROUTING_KEY = "campaign.completed";

    // Payment event configuration
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.notification.queue";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    // Donation Exchange and Queue
    @Bean
    public TopicExchange donationExchange() {
        return new TopicExchange(DONATION_EXCHANGE);
    }

    @Bean
    public Queue donationQueue() {
        return new Queue(DONATION_QUEUE, true);
    }

    @Bean
    public Binding donationBinding() {
        return BindingBuilder
            .bind(donationQueue())
            .to(donationExchange())
            .with(DONATION_ROUTING_KEY);
    }

    // Campaign Exchange and Queues
    @Bean
    public TopicExchange campaignExchange() {
        return new TopicExchange(CAMPAIGN_EXCHANGE);
    }

    @Bean
    public Queue campaignCreatedQueue() {
        return new Queue(CAMPAIGN_CREATED_QUEUE, true);
    }

    @Bean
    public Queue campaignCompletedQueue() {
        return new Queue(CAMPAIGN_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding campaignCreatedBinding() {
        return BindingBuilder
            .bind(campaignCreatedQueue())
            .to(campaignExchange())
            .with(CAMPAIGN_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding campaignCompletedBinding() {
        return BindingBuilder
            .bind(campaignCompletedQueue())
            .to(campaignExchange())
            .with(CAMPAIGN_COMPLETED_ROUTING_KEY);
    }

    // Payment Exchange and Queue
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
            .bind(paymentFailedQueue())
            .to(paymentExchange())
            .with(PAYMENT_FAILED_ROUTING_KEY);
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
