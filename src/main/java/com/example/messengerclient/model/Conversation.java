package com.example.messengerclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    private long id;
    private boolean unread;
    private String title;
    private List<Message> messages;
    private List<Participant> participants;

}
