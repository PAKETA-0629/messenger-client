package com.example.messengerclient.controller;

import com.example.messengerclient.model.Conversation;
import com.example.messengerclient.model.Message;
import com.example.messengerclient.model.User;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DataResolver {

    private final LoginFxController loginFxController;
    private final MainFxController mainFxController;
    private final ConfigurableApplicationContext applicationContext;

    @Autowired
    public DataResolver(ConfigurableApplicationContext applicationContext, FxWeaver fxWeaver, LoginFxController loginFxController, MainFxController mainFxController, FxWeaver fxWeaver1) {
        this.applicationContext = applicationContext;
        this.loginFxController = loginFxController;
        this.mainFxController = mainFxController;
    }

    public void handleConversation(List<Conversation> conversations) {
        mainFxController.setConversation(conversations);
    }


    public void handleUserList(List<User> users) {

    }


    public void handleUser(User user) {
        mainFxController.setUser(user);
    }


    public void handleMessage(Message message) {
        mainFxController.receiveMessage(message);
    }


    public void deleteMessage(Message message) {
        mainFxController.deleteMessage(message);
    }


    public void successLogin() {
       loginFxController.successLogin();
    }


    public void failedLogin() {
        loginFxController.failedLogin();
    }


    public void failedConnection() {
        loginFxController.failedConnection();
    }


}
