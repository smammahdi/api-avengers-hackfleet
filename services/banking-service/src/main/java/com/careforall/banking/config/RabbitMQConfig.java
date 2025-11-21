package com.careforall.banking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration
 *
 * Sets up:
 * - Queues for receiving payment requests from Payment Service
 * - Exchange and bindings for publishing banking events
 * - JSON message converter
 */
@Configuration
public class RabbitMQConfig {

    // ==================== QUEUES ====================

    /**
     * Queue for receiving payment requests (authorization and capture) from Payment Service
     */
    @Bean
    public Queue bankingRequestQueue() {
        return QueueBuilder.durable("banking.request.queue")
                .build();
    }

    /**
     * Queue for publishing banking response events back to Payment Service
     */
    @Bean
    public Queue bankingResponseQueue() {
        return QueueBuilder.durable("banking.response.queue")
                .build();
    }

    // ==================== EXCHANGE ====================

    /**
     * Exchange for banking service communication
     */
    @Bean
    public TopicExchange bankingExchange() {
        return new TopicExchange("banking.exchange");
    }

    // ==================== BINDINGS ====================

    /**
     * Bind request queue to banking.authorize routing key
     */
    @Bean
    public Binding authorizeBinding() {
        return BindingBuilder
                .bind(bankingRequestQueue())
                .to(bankingExchange())
                .with("banking.authorize");
    }

    /**
     * Bind request queue to banking.capture routing key
     */
    @Bean
    public Binding captureBinding() {
        return BindingBuilder
                .bind(bankingRequestQueue())
                .to(bankingExchange())
                .with("banking.capture");
    }

    /**
     * Bind response queue to banking.response routing key
     */
    @Bean
    public Binding responseBinding() {
        return BindingBuilder
                .bind(bankingResponseQueue())
                .to(bankingExchange())
                .with("banking.response");
    }

    // ==================== MESSAGE CONVERTER ====================

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
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
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
