package com.zx;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;


public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // 存储所有连接的客户端Channel
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final AttributeKey<String> USERNAME_ATTRIBUTE_KEY = AttributeKey.valueOf("username");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String clientMessage = msg.text();
        System.out.println("Received message from client: " + clientMessage);

        // 解析消息
        String[] parts = clientMessage.split(":");
        if (parts.length == 2 && parts[0].trim().equals("Username")) {
            // 处理用户名信息
            handleUsername(ctx, parts[1].trim());
        } else if (parts.length >= 3 && parts[0].trim().equals("To")) {
            // 处理私聊消息
            handlePrivateMessage(ctx, parts[1].trim(), parts[2].trim());
        } else {
            // 处理普通聊天消息
            handleChatMessage(ctx, clientMessage);
        }

        // 示例：回复客户端
        ctx.channel().writeAndFlush(new TextWebSocketFrame("Server: " + clientMessage));

        // 示例：广播消息给所有连接的客户端
        broadcastMessage("Client " + ctx.channel().id() + ": " + clientMessage);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(msg.text()));
    }

    private void handleUsername(ChannelHandlerContext ctx, String username) {
        // 将用户名信息保存在Channel的Attribute中
        ctx.channel().attr(USERNAME_ATTRIBUTE_KEY).set(username);
        // 示例：向当前连接的客户端发送欢迎消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame("Welcome, " + username + "!"));
    }

    private void handlePrivateMessage(ChannelHandlerContext ctx, String targetUsername, String message) {
        String senderUsername = ctx.channel().attr(USERNAME_ATTRIBUTE_KEY).get();

        // 遍历所有连接的客户端，找到目标用户并发送消息
        for (Channel channel : channels) {
            String username = channel.attr(USERNAME_ATTRIBUTE_KEY).get();
            if (username != null && username.equals(targetUsername)) {
                channel.writeAndFlush(new TextWebSocketFrame("Private message from " + senderUsername + ": " + message));
                return; // 找到目标用户，发送消息后直接返回
            }
        }

        // 如果目标用户未找到，发送提示消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame("User " + targetUsername + " not found."));
    }


    private void handleChatMessage(ChannelHandlerContext ctx, String message) {
        // 处理聊天消息的逻辑，可以根据需要自行实现
        // 这里只是简单地广播消息给所有连接的客户端
        String username = ctx.channel().attr(USERNAME_ATTRIBUTE_KEY).get();
        broadcastMessage(username + ": " + message);
    }


    private void broadcastMessage(String message) {
        // 将消息发送给所有连接的客户端
        channels.writeAndFlush(new TextWebSocketFrame(message));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 使用TextWebSocketFrame发送消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame("欢迎进入直播间!"));
        System.err.println("channelActive::" + "已连接");
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush("欢迎下次再来");
        System.err.println("用户已离开");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        // 当有客户端断开连接时，从ChannelGroup中移除
        channels.remove(ctx.channel());
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // 当有新的客户端连接时，加入ChannelGroup
        channels.add(ctx.channel());
    }

}
