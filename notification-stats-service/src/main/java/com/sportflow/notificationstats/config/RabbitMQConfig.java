package com.sportflow.notificationstats.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${sportflow.rabbitmq.exchange:sportflow.events}")
    private String exchangeName;

    @Value("${sportflow.rabbitmq.queue:notification.queue}")
    private String queueName;

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding registrationBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("registration.*");
    }

    @Bean
    public Binding competitionBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("competition.*");
    }

    @Bean
    public Binding matchBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue).to(eventsExchange).with("match.*");
    }
}
