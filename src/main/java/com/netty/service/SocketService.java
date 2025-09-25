package com.netty.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.netty.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import com.netty.model.Message;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
@Slf4j
public class SocketService {

    private final MessageRepository messageRepository;

    public SocketService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void sendMessage(String room, String eventName, SocketIOClient senderClient, Message message) {
        // Set the room in the message entity (if not already set)
        message.setRoom(room);

        // Save message to the database
        Message savedMessage = messageRepository.save(message);
        log.info("Message saved with id: {}", savedMessage.getId());

        // Broadcast to all clients in room
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            client.sendEvent(eventName, savedMessage);
        }
    }

    public List<Message> getRoomMessages(String room) {
        return messageRepository.findByRoomOrderByTimestampAsc(room);
    }
}

