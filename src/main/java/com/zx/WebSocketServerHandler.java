package com.zx;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Scanner;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        System.err.println("ctx:: " + ctx);
        System.err.println("msg:: " + msg);
        // 处理接收到的客户端消息
        String clientMessage = msg.text();
        System.out.println("Received message from client: " + clientMessage);

        // 示例：回复客户端
        ctx.channel().writeAndFlush(new TextWebSocketFrame("Server: " + clientMessage));

        // 接收服务端输入并发送给客户端
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter message to send to client: ");
        String serverMessage = scanner.nextLine();
        ctx.channel().writeAndFlush(new TextWebSocketFrame(serverMessage));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) {
//        ctx.channel().writeAndFlush(new TextWebSocketFrame("欢迎进入直播间!"));
//        System.err.println("channelActive::" + "已连接");
//    }
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

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.err.println("channelRead msg::" + msg);
//        super.channelRead(ctx, msg);
//    }
}
