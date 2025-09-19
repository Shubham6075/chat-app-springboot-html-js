package com.netty.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;


@Data
@JsonSerialize
public class Message {
    private MessageType type;
    private String message;
    private String room;
    private String sender;
    private boolean systemMessage;
    private String timestamp;

    public Message() {

    }

    public Message(MessageType type, String message, String sender, String timestamp) {
        this.type = type;
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public Message(String s) {
        this.message = s;
    }
}
