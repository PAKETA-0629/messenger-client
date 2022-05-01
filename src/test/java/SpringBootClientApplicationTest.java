import com.example.messengerclient.util.MessageStatus;
import com.example.messengerclient.util.MessageType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.sql.Date;
import java.sql.Timestamp;

class SpringBootClientApplicationTest {


    @Test
    void testEncodeStompMessage() {
        com.example.messengerclient.model.Message[] payload = new com.example.messengerclient.model.Message[64*8-4];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = com.example.messengerclient.model.Message.builder().text("hello worldhello world12345").build();
        }
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message message;

        message = new MappingJackson2MessageConverter().toMessage(payload, accessor.getMessageHeaders());

        Message<byte[]> newMessage = (Message<byte[]>) message;
        System.out.println(newMessage);
        System.out.println(newMessage.getPayload().length);
        byte[] payloadArray = newMessage.getPayload();
        for (int i = 0; i < payloadArray.length;i++) {
            if (payloadArray[i] == 104) {
                System.out.println(i);
            }
        }
    }

    @SneakyThrows
    @Test
    void testSendMessage() {

        String URL = "localhost:8080/ws";

        StompSession session;
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("login", "@kyrylo0629");
        stompHeaders.add("passcode", "password12345");

        ListenableFuture<StompSession> listenableFuture = stompClient.connect("ws://" + URL,
                new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter(){});
        session = listenableFuture.get();

        com.example.messengerclient.model.Message[] payload = new com.example.messengerclient.model.Message[64 * 8];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = com.example.messengerclient.model.Message.builder().text("hello worldhello world12345").build();
        }
        stompClient.setInboundMessageSizeLimit(64 * 1024);
        session.send("/app/message/send", payload);
    }


    @SneakyThrows
    @Test
    void test() {
        String URL = "localhost:8080/ws";

        StompSession session;
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("login", "@kyrylo0629");
        stompHeaders.add("passcode", "password12345");

        ListenableFuture<StompSession> listenableFuture = stompClient.connect("ws://" + URL,
                new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter(){});
        session = listenableFuture.get();
        com.example.messengerclient.model.Message message = com.example.messengerclient.model.Message.builder()
                .text("hello")
                .createAt(new Timestamp(System.currentTimeMillis()))
                .messageStatus(MessageStatus.SENT)
                .conversation(1L)
                .messageType(MessageType.TEXT)
                .build();

        session.send("/app/message/send", message);
    }
}
