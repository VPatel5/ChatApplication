package me.vpatel.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.vpatel.network.protocol.ChatServerPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatServerChannelHandler extends SimpleChannelInboundHandler<ChatServerPacket> {

    private static final Logger log = LogManager.getLogger(ChatServerChannelHandler.class);

    private ChatServerConnection connection;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[+] Channel connected: {}", ctx.channel().remoteAddress());

        this.connection = new ChatServerConnection(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[-] Channel disconnected: {}", ctx.channel().remoteAddress());

        this.connection = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if ("Connection reset".equals(cause.getMessage())) {
            log.error("{}: Connection reset.", this.connection.getRemoteAddress());
        } else if ("An established connection was aborted by the software in your host machine".equals(cause.getMessage()) ||
                "An existing connection was forcibly closed by the remote host".equals(cause.getMessage())) {
            log.error("{}: Disconnected.", this.connection.getRemoteAddress());
        } else {
            log.error("{}: Exception caught, closing channel.", this.connection.getRemoteAddress(), cause);
        }

        this.connection = null;

        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ChatServerPacket chatServerPacket) throws Exception {

    }
}
