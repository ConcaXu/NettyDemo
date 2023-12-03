package com.zx;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class WebSocketServer {

    public static void main(String[] args) throws Exception {
        new WebSocketServer().run(8080);
    }

    public void run(int port) throws Exception {
        // 用于接收传入连接请求
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 用于处理已建立的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            /* ServerBootstrap Netty的引导类用于设置和启动服务器 **/
            ServerBootstrap bootstrap = new ServerBootstrap();
            /* 将事件循环组配置到引导类中 **/
            bootstrap.group(bossGroup, workerGroup)
                    /*
                     * OioServerSocketChannel：使用旧的阻塞式I/O（Old I/O）通道，通常用于兼容旧的阻塞式代码或特定的需求。
                     * EpollServerSocketChannel：在Linux系统上使用Epoll事件模型的通道，可以提供更高性能的事件处理。
                     * KqueueServerSocketChannel：在BSD和macOS系统上使用Kqueue事件模型的通道，用于提供高性能的事件处理。
                     * LocalServerChannel：用于本地通信，通过Unix域套接字（Unix Domain Socket）实现。
                     * SocketServerChannel：Socket通道，可用于特定的传输协议。
                     */
                    // 配置通道为NIO
                    .channel(NioServerSocketChannel.class)
                    // 配置通道的初始化器，用于设置通道处理器，也就是在建立连接后，对通道的数据进行处理
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 用于处理HTTP编解码
                            ch.pipeline().addLast(new HttpServerCodec());
                            // 将HTTP消息的多个部分合并成一个完整的FullHttpRequest或FullHttpResponse对象
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            // 于处理WebSocket协议升级和握手操作
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/chat"));
                            // 自定义的WebSocket消息处理器，处理WebSocket消息
                            ch.pipeline().addLast(new WebSocketServerHandler());
                        }
                    })
                    // 设置服务器套接字的参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("WebSocket Server 启动，端口：" + port);

            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

