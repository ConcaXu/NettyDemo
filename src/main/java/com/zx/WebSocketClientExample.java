package com.zx;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class WebSocketClientExample extends WebSocketClient {

    public WebSocketClientExample(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed. Code: " + code + ", Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public static void main(String[] args) throws URISyntaxException {
        WebSocketClientExample client = new WebSocketClientExample(new URI("ws://localhost:8080/chat"));

        // 连接到服务器
        client.connect();

        // 发送消息
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter message (type 'exit' to close): ");
            String message = scanner.nextLine();
            if ("exit".equalsIgnoreCase(message)) {
                break;
            }
            client.send(message);
        }

        // 关闭连接
        client.close();
    }
}

