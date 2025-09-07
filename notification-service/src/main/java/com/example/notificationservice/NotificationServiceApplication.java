package com.example.notificationservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.notificationservice.controller.SSEController;

@SpringBootApplication
@EnableRabbit
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

@Service
class UserEventListener {

    private final SSEController sseController;

    @Autowired
    public UserEventListener(SSEController sseController) {
        this.sseController = sseController;
    }

    @RabbitListener(queues = "user.created.queue")
    public void handleUserCreatedEvent(Object event) {
        System.out.println("Received event: " + event);
        sseController.sendEventToClients(event);
    }

    @RabbitListener(queues = "user.creation.failed.queue")
        public void handleUserCreationFailedEvent(Object event) {
        System.out.println("Received user creation failed event: " + event);
        sseController.sendEventToClients(event);
    }
}
