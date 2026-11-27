package com.sportflow.notificationstats.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationStatsConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationStatsConsumer.class);

    @RabbitListener(queues = "${sportflow.rabbitmq.queue:notification.queue}")
    public void handleEvent(String message) {
        log.info("Received event from RabbitMQ: {}", message);
        // Event processing and persistence will be implemented in task B7/B8
    }
}
