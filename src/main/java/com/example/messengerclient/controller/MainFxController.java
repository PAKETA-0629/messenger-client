package com.example.messengerclient.controller;

import com.example.messengerclient.ClientApplicationInitializer;
import com.example.messengerclient.model.Conversation;
import com.example.messengerclient.model.Participant;
import com.example.messengerclient.model.User;
import com.example.messengerclient.service.StompService;
import com.example.messengerclient.model.Message;
import com.example.messengerclient.util.MessageStatus;
import com.example.messengerclient.util.MessageType;
import com.example.messengerclient.util.Role;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@FxmlView("../templates/main-scene.fxml")
public class MainFxController {


    private static ClientApplicationInitializer clientApplicationInitializer;


    public static void setClientApplicationInitializer(ClientApplicationInitializer clientApplicationInitializer) {
        MainFxController.clientApplicationInitializer = clientApplicationInitializer;
    }


    @FXML
    private ScrollPane contactsList;
    @FXML
    private ScrollPane messagesList;
    @FXML
    private VBox messagesVBox;
    @FXML
    private Button sendButton;
    @FXML
    private TextField textField;
    @FXML
    private VBox contactsVBox;
    @FXML
    private Label convTitle;
    @FXML
    private HBox sendBar;



    private User user;
    private int currentConversation = -1;
    private List<Conversation> conversations;

    private final StompService stompService;

    @Autowired
    public MainFxController(StompService stompService) {
        this.stompService = stompService;
    }


    @FXML
    private void initialize() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        contactsList.setFitToWidth(true);
        messagesList.setFitToWidth(true);
        stompService.getUser();
        stompService.pullAllData();
    }


    @FXML
    private void sendMessage() {
        if (textField.getText().trim().equals("")) {
            return;
        }
        Message message = Message.builder()
                .text(textField.getText())
                .sender(new Participant(user, Role.PARTICIPANT, conversations.get(this.currentConversation).getId()))
                .createAt(new Timestamp(System.currentTimeMillis()))
                .messageStatus(MessageStatus.SENT)
                .conversation(conversations.get(this.currentConversation).getId())
                .messageType(MessageType.TEXT)
                .build();

        stompService.sendMessage("/app/message/send", message);
        conversations.get(currentConversation).getMessages().add(message);
        displayMessages(List.of(message));
        contactsVBox.getChildren().clear();
        Conversation conversation = conversations.get(this.currentConversation);
        sortConversations();
        this.currentConversation = conversations.indexOf(conversation);
        displayConversations(conversations);
        textField.setText("");

    }


    @FXML
    private void logout() {
        this.user = null;
        this.conversations = null;
        this.currentConversation = -1;
        clientApplicationInitializer.logout();
        stompService.disconnect();
    }


    public void setUser(User user) {
        this.user = user;
        clientApplicationInitializer.setTitle(user.getNickname());
    }


    public void setConversation(List<Conversation> conversations) {
        if (this.conversations == null) {
            this.conversations = conversations;
        } else {
            this.conversations.addAll(conversations);
        }
        sortConversations();
        contactsVBox.getChildren().clear();
        displayConversations(this.conversations);
    }


    public void deleteMessage(Message message) {
        Conversation conversation = conversations.stream().filter(conv -> conv.getId() == message.getConversation()).findFirst().get();
        conversation.getMessages().remove(message);
        if (conversations.get(this.currentConversation).equals(conversation)) {
            messagesVBox.getChildren().clear();
            displayMessages(conversation.getMessages());
        }
        contactsVBox.getChildren().clear();
        displayConversations(this.conversations);
    }

    @SneakyThrows
    public void receiveMessage(Message message) {

        Conversation conversation = conversations.stream().filter(lambda -> lambda.getId() == message.getConversation()).findFirst().orElse(null);
        if (conversation == null) throw new Exception();

        List<Message> messages = conversation.getMessages();
        int messageIndex = messages.indexOf(message);
        if (messageIndex == -1) {
            conversation.getMessages().add(message);
            conversation.setUnread(true);
            if (this.currentConversation == conversations.indexOf(conversation)) {
                displayMessages(List.of(message));
            }  else {
                contactsVBox.getChildren().get(conversations.indexOf(conversation)).setStyle("-fx-background-color: green");
            }
        } else {
            conversation.getMessages().get(messageIndex).setMessageStatus(message.getMessageStatus());
            if (currentConversation == conversations.indexOf(conversation)) {
                messagesVBox.getChildren().clear();
                displayMessages(conversation.getMessages());
            }
        }

        conversation = conversations.get(this.currentConversation);
        sortConversations();
        this.currentConversation = conversations.indexOf(conversation);
        contactsVBox.getChildren().clear();
        displayConversations(conversations);
    }


    public void sortConversations() {

        this.conversations = this.conversations.stream().sorted((o1, o2) -> {

            if (o1.getMessages().size() == 0 & o1.getMessages().size() == 0) {
                return 0;
            }
            else if (o1.getMessages().size() == 0 & o2.getMessages().size() > 0) {
                return 1;
            }
            else if (o1.getMessages().size() > 0 & o2.getMessages().size() == 0) {
                return -1;
            }
            Timestamp date1 = o1.getMessages().get(o1.getMessages().size() - 1).getCreateAt();
            Timestamp date2 = o2.getMessages().get(o2.getMessages().size() - 1).getCreateAt();
            if (date1.after(date2)) return -1;
            else if (date1.before(date2)) return 1;
            else if (date1.equals(date2)) return 0;
            return 0;
        }).collect(Collectors.toList());
    }

    public void displayConversations(List<Conversation> conversations) {

        for (Conversation conversation : conversations) {
            Label nickname = new Label();
            Label lastMessage = new Label();
            Label time = new Label();
            Label newMessage = new Label();
            FlowPane flowPane = new FlowPane();
            nickname.getStyleClass().add("nickname");
            lastMessage.getStyleClass().add("lastMessage");
            flowPane.getStyleClass().add("tile");

            List<Participant> participants = conversation.getParticipants();

            if (participants.size() > 2) {
                nickname.setText(conversation.getTitle());
            } else {
                if (participants.get(0).getUser().equals(user)) {
                    nickname.setText(participants.get(1).getUser().getNickname());
                } else {
                    nickname.setText(participants.get(0).getUser().getNickname());
                }
            }

            Message message = !conversation.getMessages().isEmpty() ? conversation.getMessages().get(conversation.getMessages().size() - 1) : null;
            lastMessage.setText(message != null ? message.getText() : "");
            time.setText(message != null ? message.getCreateAt().toString() : "");
            if (this.currentConversation != conversations.indexOf(conversation) && conversation.isUnread()) flowPane.setStyle("-fx-background-color: green");
            flowPane.getChildren().addAll(nickname, lastMessage, time, newMessage);
            flowPane.setOnMouseClicked(getMouseEvent(flowPane));
            contactsVBox.getChildren().add(flowPane);
        }
        contactsList.setContent(contactsVBox);
    }


    public void displayMessages(List<Message> messages) {

        for (Message message: messages) {
            Label label = new Label();
            label.getStyleClass().add("messageText");

            Label time = new Label();
            FlowPane flowPane = new FlowPane();

            flowPane.getStyleClass().add("message");
            flowPane.setHgap(20.0D);
            time.setText(message.getCreateAt().toString());
            label.setText(message.getText());
            ContextMenu contextMenu = new ContextMenu();
            MenuItem menuItem1 = new MenuItem("Edit");
            MenuItem menuItem2 = new MenuItem("Delete");
            contextMenu.getItems().addAll(menuItem1,menuItem2);
            menuItem1.setOnAction(event -> {

            });

            menuItem2.setOnAction(event -> {
                conversations.get(currentConversation).getMessages().remove(message);
                messagesVBox.getChildren().clear();
                messagesList.setContent(messagesVBox);
                displayMessages(conversations.get(currentConversation).getMessages());
                stompService.deleteMessage(message);
            });

            flowPane.getChildren().addAll(label);
            flowPane.setOnContextMenuRequested(event -> contextMenu.show(flowPane, event.getScreenX(), event.getScreenY()));
            if (message.getSender() == null || message.getSender().getUser().getNickname().equals(user.getNickname())) {
                flowPane.setStyle("-fx-background-color: blue");
                Label status = new Label();
                status.setText(message.getMessageStatus().toString());
                flowPane.getChildren().add(status);
            } else {

                if (message.getMessageStatus() != MessageStatus.READ) {
                    message.setMessageStatus(MessageStatus.READ);
                    stompService.changeStatus(message);
                }
                flowPane.setStyle("-fx-background-color: gray");
            }


            messagesVBox.getChildren().add(flowPane);
        }

        messagesList.setContent(messagesVBox);

    }


    private EventHandler<MouseEvent> getMouseEvent(FlowPane flowPane) {
        return mouseEvent -> {

            if (this.currentConversation != contactsVBox.getChildren().indexOf(flowPane)) {
                if (this.currentConversation != -1) {
                    contactsVBox.getChildren().get(currentConversation).setStyle(null);
                }
                this.currentConversation = contactsVBox.getChildren().indexOf(flowPane);
                messagesVBox.getChildren().clear();
                conversations.get(this.currentConversation).setUnread(false);
                displayMessages(conversations.get(this.currentConversation).getMessages());
            }
            sendBar.setVisible(true);
            flowPane.setStyle("-fx-background-color: #0000ff");

        };

    }

}
