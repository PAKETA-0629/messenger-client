package com.example.messengerclient.service;

import com.example.messengerclient.websocket.MyStompSessionHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@Slf4j
@Component
@Scope("singleton")
public class StompService extends StompSessionHandlerAdapter {

    @Value("${websocket.url}")
    private String URL;
    private StompSession session;
    private final WebSocketStompClient stompClient;
    private final ConfigurableApplicationContext applicationContext;


    @Autowired
    public StompService(ConfigurableApplicationContext applicationContext) {

        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        this.stompClient = stompClient;
        this.applicationContext = applicationContext;

    }


    @SneakyThrows
    public void connect(String login, String password) {
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("login", login);
        stompHeaders.add("passcode", password);

        if (session != null && session.isConnected()) {
            disconnect();
        }

        ListenableFuture<StompSession> listenableFuture = stompClient.connect("ws://" + URL,
                new WebSocketHttpHeaders(), stompHeaders, applicationContext.getBean(MyStompSessionHandler.class));
        this.session = listenableFuture.get();
    }


    public void sendMessage(String endpoint, Object payload) {

        session.send(endpoint, payload);
    }


    public void getUser() {
        session.send("/app/user/get", null);
    }


    public void pullAllData() {
        session.send("/app/pull/all", null);
    }


    public void disconnect() {
        if (session.isConnected()) {
            session.disconnect();
        }
        session = null;
        log.info("Disconnect Session");
    }

}