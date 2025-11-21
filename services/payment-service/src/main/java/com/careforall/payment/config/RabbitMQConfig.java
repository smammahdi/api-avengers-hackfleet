package com.careforall.payment.config;

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
 * Configures exchanges, queues, and bindings for:
 * 1. Listening to DONATION_CREATED events from donation-service
 * 2. Publishing PAYMENT_COMPLETED and PAYMENT_FAILED events
 * 3. Sending authorization/capture requests to banking-service
 * 4. Listening to banking response events from banking-service
 */
@Configuration
public class RabbitMQConfig {

    // ========== Payment Event Publishing (Outbound) ==========
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    // ========== Donation Event Listening (Inbound) ==========
    public static final String DONATION_EXCHANGE = "donation.exchange";
    public static final String DONATION_QUEUE = "donation.payment.queue";
    public static final String DONATION_CREATED_ROUTING_KEY = "donation.created";

    // ========== Banking Service Integration ==========
    public static final String BANKING_EXCHANGE = "banking.exchange";
    public static final String BANKING_REQUEST_QUEUE = "banking.request.queue";
    public static final String BANKING_RESPONSE_QUEUE = "banking.response.queue";
    public static final String BANKING_AUTHORIZE_ROUTING_KEY = "banking.authorize";
    public static final String BANKING_CAPTURE_ROUTING_KEY = "banking.capture";
    public static final String BANKING_RESPONSE_ROUTING_KEY = "banking.response";

    // ========== Payment Exchange and Queues (for publishing events) ==========

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
            .bind(paymentQueue())
            .to(paymentExchange())
            .with(PAYMENT_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder
            .bind(paymentQueue())
            .to(paymentExchange())
            .with(PAYMENT_FAILED_ROUTING_KEY);
    }

    // ========== Donation Exchange and Queues (for consuming events) ==========

    @Bean
    public TopicExchange donationExchange() {
        return new TopicExchange(DONATION_EXCHANGE);
    }

    @Bean
    public Queue donationQueue() {
        return QueueBuilder.durable(DONATION_QUEUE)
            .withArgument("x-dead-letter-exchange", "donation.dlx")
            .withArgument("x-dead-letter-routing-key", "donation.payment.failed")
            .build();
    }

    @Bean
    public Binding donationBinding() {
        return BindingBuilder
            .bind(donationQueue())
            .to(donationExchange())
            .with(DONATION_CREATED_ROUTING_KEY);
    }

    // ========== Dead Letter Queue Configuration ==========

    @Bean
    public Queue donationDeadLetterQueue() {
        return new Queue("donation.payment.dlq", true);
    }

    @Bean
    public TopicExchange donationDeadLetterExchange() {
        return new TopicExchange("donation.dlx");
    }

    @Bean
    public Binding donationDeadLetterBinding() {
        return BindingBuilder
            .bind(donationDeadLetterQueue())
            .to(donationDeadLetterExchange())
            .with("donation.payment.failed");
    }

    // ========== Banking Service Exchange and Queues ==========

    @Bean
    public TopicExchange bankingExchange() {
        return new TopicExchange(BANKING_EXCHANGE);
    }

    @Bean
    public Queue bankingRequestQueue() {
        return new Queue(BANKING_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue bankingResponseQueue() {
        return new Queue(BANKING_RESPONSE_QUEUE, true);
    }

    @Bean
    public Binding bankingAuthorizeBinding() {
        return BindingBuilder
            .bind(bankingRequestQueue())
            .to(bankingExchange())
            .with(BANKING_AUTHORIZE_ROUTING_KEY);
    }

    @Bean
    public Binding bankingCaptureBinding() {
        return BindingBuilder
            .bind(bankingRequestQueue())
            .to(bankingExchange())
            .with(BANKING_CAPTURE_ROUTING_KEY);
    }

    @Bean
    public Binding bankingResponseBinding() {
        return BindingBuilder
            .bind(bankingResponseQueue())
            .to(bankingExchange())
            .with(BANKING_RESPONSE_ROUTING_KEY);
    }

    // ========== Message Converter ==========

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
