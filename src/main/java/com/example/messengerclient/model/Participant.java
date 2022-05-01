package com.example.messengerclient.model;

import com.example.messengerclient.util.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Participant {

    private User user;
    private Role role;
    private long conversation;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Participant)){
            return false;
        }

        Participant participant = (Participant) object;
        return this.role.equals(participant.getRole()) &&
                this.conversation == participant.getConversation() &&
                this.user.equals(participant.getUser());
    }

}
