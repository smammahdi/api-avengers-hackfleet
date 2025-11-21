package com.careforall.analytics.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DONATION_EXCHANGE = "donation.exchange";
    public static final String CAMPAIGN_EXCHANGE = "campaign.exchange";

    public static final String DONATION_COMPLETED_QUEUE = "analytics.donation.completed.queue";
    public static final String CAMPAIGN_CREATED_QUEUE = "analytics.campaign.created.queue";
    public static final String CAMPAIGN_COMPLETED_QUEUE = "analytics.campaign.completed.queue";

    public static final String DONATION_COMPLETED_ROUTING_KEY = "donation.completed";
    public static final String CAMPAIGN_CREATED_ROUTING_KEY = "campaign.created";
    public static final String CAMPAIGN_COMPLETED_ROUTING_KEY = "campaign.completed";

    @Bean
    public TopicExchange donationExchange() {
        return new TopicExchange(DONATION_EXCHANGE);
    }

    @Bean
    public TopicExchange campaignExchange() {
        return new TopicExchange(CAMPAIGN_EXCHANGE);
    }

    @Bean
    public Queue donationCompletedQueue() {
        return new Queue(DONATION_COMPLETED_QUEUE, true);
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
    public Binding donationCompletedBinding() {
        return BindingBuilder
                .bind(donationCompletedQueue())
                .to(donationExchange())
                .with(DONATION_COMPLETED_ROUTING_KEY);
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

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
