package com.fanshuai.domain;

public enum MessageType {
    HEART_BEAT(1),
    REQUEST(2),
    RESPONSE(3);

    public int value;

    MessageType(int value) {
        this.value = value;
    }

    public static MessageType getMessageType(int type) {
        for (MessageType messageType : MessageType.values()) {
            if (type == messageType.value) {
                return messageType;
            }
        }

        return null;
    }
}
