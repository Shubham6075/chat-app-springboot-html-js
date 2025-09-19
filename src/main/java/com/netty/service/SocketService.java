package com.netty.service;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import com.netty.model.Message;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;




@Service
@Slf4j
public class SocketService {

    // Store messages in memory: room -> list of messages
    private final Map<String, List<Message>> roomMessages = new ConcurrentHashMap<>();

    public void sendMessage(String room, String eventName, SocketIOClient senderClient, Message message) {
        // Save message in memory
        roomMessages.computeIfAbsent(room, k -> new ArrayList<>()).add(message);

        // Broadcast to all clients in room
        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            client.sendEvent(eventName, message);
        }
    }

    // Fetch history for a room
    public List<Message> getRoomMessages(String room) {
        return roomMessages.getOrDefault(room, Collections.emptyList());
    }
}

