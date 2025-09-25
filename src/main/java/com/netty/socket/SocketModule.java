package com.netty.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.extern.slf4j.Slf4j;
import com.netty.model.Message;
import com.netty.service.SocketService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SocketModule {

    private final SocketIOServer server;
    private final SocketService socketService;

    private final Map<UUID, String> connectedUsers = new ConcurrentHashMap<>();

    public SocketModule(SocketIOServer server, SocketService socketService) {
        this.server = server;
        this.socketService = socketService;

        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("send_message", Message.class, onChatReceived());

        // Typing event
        server.addEventListener("typing", String.class, (client, typingData, ackRequest) -> {
            String username = connectedUsers.get(client.getSessionId());
            String room = client.getHandshakeData().getSingleUrlParam("room");
            if (room != null && username != null) {
                server.getRoomOperations(room).sendEvent("typing", username);
            }
        });
    }

    @PostConstruct
    public void startSocketServer() {
        log.info("🚀 Starting Socket.IO server on port {}", server.getConfiguration().getPort());
        server.start();
    }

    @PreDestroy
    public void stopSocketServer() {
        log.info("🛑 Stopping Socket.IO server...");
        server.stop();
    }

    private DataListener<Message> onChatReceived() {
        return (senderClient, data, ackSender) -> {
            String senderUsername = connectedUsers.get(senderClient.getSessionId());
            if (senderUsername == null) {
                senderUsername = "Unknown";
            }
            data.setSender(senderUsername);
            data.setTimestamp(Instant.now().toString());
            data.setSystemMessage(false);

            if (data.getReplyToMessage() != null) {
                log.info("↩️ [{}] replied to [{}]: {} -> {}",
                        senderUsername,
                        data.getReplyToSender(),
                        data.getReplyToMessage(),
                        data.getMessage());
            } else {
                log.info("📩 [{}] sent message: {}", senderUsername, data.getMessage());
            }

            socketService.sendMessage(
                    data.getRoom(),
                    "get_message",
                    senderClient,
                    data
            );
        };
    }

    private ConnectListener onConnected() {
        return client -> {
            String room = client.getHandshakeData().getSingleUrlParam("room");
            String username = client.getHandshakeData().getSingleUrlParam("username");

            if (room != null && username != null) {
                client.joinRoom(room);
                connectedUsers.put(client.getSessionId(), username);
                log.info("✅ [{}] joined room [{}]", username, room);


                var history = socketService.getRoomMessages(room);
                for (Message past : history) {
                    client.sendEvent("get_message", past);
                }


                Message joinMsg = new Message(username + " has joined the room.");
                joinMsg.setRoom(room);
                joinMsg.setSystemMessage(true);
                joinMsg.setTimestamp(Instant.now().toString());
                socketService.sendMessage(room, "get_message", client, joinMsg);
            }
        };
    }


    private DisconnectListener onDisconnected() {
        return client -> {
            String username = connectedUsers.get(client.getSessionId());
            String room = client.getHandshakeData().getSingleUrlParam("room");

            if (username != null && room != null) {
                connectedUsers.remove(client.getSessionId());
                log.info("❌ [{}] disconnected", username);

                Message leaveMsg = new Message(username + " has left the room.");
                leaveMsg.setRoom(room);
                leaveMsg.setSystemMessage(true);
                leaveMsg.setTimestamp(Instant.now().toString());
                socketService.sendMessage(room, "get_message", client, leaveMsg);
            }
        };
    }
}
