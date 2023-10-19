package com.example.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.SneakyThrows;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class QueueService {
    @Autowired
    private JmsTemplate jmsTemplate;

    @SneakyThrows
    public void sendToTopic(String topicName, String jsonPayload, String messageType) {
        jmsTemplate.send(new ActiveMQTopic("users_events"), session -> {
            Message message = session.createObjectMessage(jsonPayload);
            message.setStringProperty("messageType", messageType);
            return message;
        });
    }
}
