package com.careforall.campaign.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Campaign Service
 *
 * Configures exchanges, queues, and bindings for campaign events.
 */
@Configuration
public class RabbitMQConfig {

    public static final String CAMPAIGN_EXCHANGE = "campaign.exchange";
    public static final String CAMPAIGN_QUEUE = "campaign.queue";
    public static final String CAMPAIGN_ROUTING_KEY = "campaign.event";

    /**
     * Campaign exchange for publishing events
     */
    @Bean
    public TopicExchange campaignExchange() {
        return new TopicExchange(CAMPAIGN_EXCHANGE);
    }

    /**
     * Campaign queue for storing events
     */
    @Bean
    public Queue campaignQueue() {
        return new Queue(CAMPAIGN_QUEUE, true);
    }

    /**
     * Binding between exchange and queue
     */
    @Bean
    public Binding campaignBinding(Queue campaignQueue, TopicExchange campaignExchange) {
        return BindingBuilder
            .bind(campaignQueue)
            .to(campaignExchange)
            .with(CAMPAIGN_ROUTING_KEY);
    }

    /**
     * JSON message converter for serializing events
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitMQ template with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
