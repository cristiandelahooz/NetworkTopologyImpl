package org.sakidoa.core;

import org.sakidoa.core.enums.MessageType;

public  class Message {
    private final MessageType type;
    private final String senderId;
    private final Object payload;
    private final long timestamp;
    private String receiverId;

    public Message(MessageType type, String senderId, Object payload, long timestamp) {
        this.type = type;
        this.senderId = senderId;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public Message(MessageType type, String senderId, Object payload) {
        this(type, senderId, payload, System.currentTimeMillis());
    }


    // Getters
    public MessageType getType() { return type; }
    public String getSenderId() { return senderId; }
    public Object getPayload() { return payload; }
    public long getTimestamp() { return timestamp; }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String toId) {
        this.receiverId = toId;
    }
}
