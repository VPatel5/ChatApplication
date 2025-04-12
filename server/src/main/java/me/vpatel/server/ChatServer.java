package me.vpatel.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.vpatel.console.ServerConsole;
import me.vpatel.network.pipeline.ChatServerPipeline;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatServer {

    private static final Logger log = LogManager.getLogger(ChatServer.class);

    private final int port;

    private ServerConsole console;

    public ChatServer(int port)
    {
        this.port = port;
    }

    public static void main(String[] args)
    {
        log.info("Starting up chat server");

        ChatServer chatServer = new ChatServer(8080);
        chatServer.start();
    }

    public void start()
    {
        log.info("Starting up server console");
        console = new ServerConsole();
        console.start();

        log.info("Booting up server socket");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChatServerPipeline());

            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error occurred starting socket", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

