package com.netty.model;

import lombok.Data;
import jakarta.persistence.*;


@Data
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String room;
    private String sender;

    private boolean systemMessage;

    private String timestamp;

    private String replyToMessage;
    private String replyToSender;

    public Message() {
    }

    public Message(String message, String sender, String timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public Message(String s) {
        this.message = s;
    }
}
