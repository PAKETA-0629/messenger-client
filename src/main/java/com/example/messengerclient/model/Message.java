package com.example.messengerclient.model;

import com.example.messengerclient.util.MessageStatus;
import com.example.messengerclient.util.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    private long id;
    private long conversation;
    private Participant sender;
    private MessageType messageType;
    private MessageStatus messageStatus;
    private String text;
    private Timestamp createAt;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Message)){
            return false;
        }

        Message message = (Message) object;
        return this.conversation == message.getConversation() &&
                this.sender.equals(message.getSender()) &&
                this.messageType.equals(message.getMessageType()) &&
                this.text.equals(message.getText()) &&
                this.createAt.equals(message.getCreateAt());
    }
}
