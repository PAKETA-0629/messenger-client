package com.example.messengerclient.websocket;

import com.example.messengerclient.controller.DataResolver;
import com.example.messengerclient.model.Conversation;
import com.example.messengerclient.model.Message;
import com.example.messengerclient.model.User;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class MyStompSessionHandler extends StompSessionHandlerAdapter {

    private final DataResolver dataResolver;
    @Autowired
    public MyStompSessionHandler(DataResolver dataResolver) {
        this.dataResolver = dataResolver;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

        session.subscribe("/user/queue/user", this);
        session.subscribe("/user/queue/pull/all", this);
        session.subscribe("/user/queue/position-updates", this);

//        session.subscribe("/user/queue/position-updates", this);
//        session.subscribe("/user/queue/conversations/pull", this);
//        session.subscribe("/user/queue/contacts/pull", this);

//        session.send("/user/queue/get-user", new User());
        Platform.runLater(dataResolver::successLogin);

        log.info("Success connection");
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.error("Got an exception", exception);

    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        String payloadClass = headers.getFirst("payload-class");

        if (payloadClass == null) {
            return Object.class;
        }

        switch (payloadClass) {
            case "user" :
                return User.class;
            case "user-list" :
                return User[].class;
            case "conversation-array":
                return Conversation[].class;
            case "conversation-item":
                return Conversation.class;
            case "message":
                return Message.class;
            default : return Object.class;
        }
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        if (payload instanceof User) {
            Platform.runLater(() -> {
                dataResolver.handleUser((User) payload);

            });

        } else if (payload instanceof User[]) {
            Platform.runLater(() -> {
                List<User> list = Arrays.asList((User[]) payload);
                dataResolver.handleUserList(list);
            });
        } else if (payload instanceof Conversation[]) {
            Platform.runLater(() -> {

                List<Conversation> list = new ArrayList<>(Arrays.asList((Conversation[]) payload));
                dataResolver.handleConversation(list);
            });
        } else if (payload instanceof Conversation) {
            Platform.runLater(() -> {
//                List<Conversation> list = List.of((Conversation) payload);
                List<Conversation> list = new CopyOnWriteArrayList<>();
                list.add((Conversation) payload);
                dataResolver.handleConversation(list);
            });
        } else if (payload instanceof Message) {
            Platform.runLater(() -> {
                dataResolver.handleMessage((Message) payload);
            });
        }
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        
        if (exception.getMessage().startsWith("The HTTP request to initiate the WebSocket connection failed")) {

            log.error("No Internet Connection Or Server Is Down Or Session Connection Is Fail Or Wrong Ip");
            Platform.runLater(dataResolver::failedConnection);
        } else if (exception.getMessage().startsWith("Connection closed")) {
            exception.printStackTrace();
            log.error("Incorrect Login Or Password");
            Platform.runLater(dataResolver::failedLogin);
        }
    }

}
