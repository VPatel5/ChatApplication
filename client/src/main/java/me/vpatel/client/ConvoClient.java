package me.vpatel.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.vpatel.client.ui.LoginUI;
import me.vpatel.client.ui.UIScreenManager;
import me.vpatel.console.ConvoConsole;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.pipeline.ConvoPipeline;
import me.vpatel.network.protocol.ConvoPacketHandler;
import me.vpatel.network.protocol.ConvoPacketRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class ConvoClient {

    private static final Logger log = LogManager.getLogger(ConvoClient.class);

    private ConvoConsole console;
    private ConvoClientHandler handler;
    private ConvoPacketRegistry packetRegistry;
    private ConvoPacketHandler packetHandler;
    private ConvoUser convoUser;
    private AuthHandler authHandler;

    public static void main(String[] args) {
        AppContext.getClient().init();
        new Thread(() -> AppContext.getClient().connect("localhost", 8080)).start();
        SwingUtilities.invokeLater(() -> UIScreenManager.showScreen(new LoginUI()));
    }

    public void init()
    {
        log.info("Initializing Client");
        this.console = new ConvoConsole(this);
        this.console.start();

        this.handler = new ConvoClientHandler();

        this.packetRegistry = new ConvoPacketRegistry();
        this.packetRegistry.init();

        this.packetHandler = new ConvoClientPacketHandler(handler);
        this.packetHandler.init();

        this.convoUser = new ConvoUser();

        this.authHandler = new AuthHandler();
    }

    public void connect(final String hostname, final int port) {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ConvoPipeline(packetRegistry, handler, packetHandler));

            // wait till connection should be closed
            bootstrap.connect(hostname, port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public ConvoConsole getConsole() {
        return console;
    }

    public ConvoClientHandler getHandler() {
        return handler;
    }

    public void setUser(ConvoUser convoUser) {
        this.convoUser = convoUser;
    }

    public ConvoUser getUser() {
        return convoUser;
    }

    public AuthHandler getAuthHandler() {
        return authHandler;
    }
}
