package me.vpatel.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.vpatel.console.ConvoConsole;
import me.vpatel.network.pipeline.ConvoPipeline;
import me.vpatel.network.protocol.ConvoPacketRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Convo {

    private static final Logger log = LogManager.getLogger(Convo.class);

    private final int port;

    private ConvoConsole console;
    private ConvoPacketRegistry packetRegistry;

    public Convo(int port)
    {
        this.port = port;
    }

    public static void main(String[] args)
    {
        log.info("Starting up chat server");

        Convo chatServer = new Convo(8080);
        chatServer.start();
    }

    public void start()
    {
        log.info("Starting up server console");
        console = new ConvoConsole();
        console.start();

        log.info("Initializing");
        this.packetRegistry = new ConvoPacketRegistry();

        log.info("Booting up server socket");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ConvoPipeline(packetRegistry));

            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error occurred starting socket", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

