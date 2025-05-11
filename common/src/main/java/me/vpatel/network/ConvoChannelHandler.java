package me.vpatel.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoChannelHandler extends SimpleChannelInboundHandler<ConvoPacket> {

    private static final Logger log = LogManager.getLogger(ConvoChannelHandler.class);

    private ConvoConnection connection;
    private final ConvoHandler handler;

    public ConvoChannelHandler(ConvoHandler handler) {
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("[+] Channel connected: {}", ctx.channel().remoteAddress());

        this.connection = new ConvoConnection(ctx);
        if (handler != null) {
            handler.join(connection);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[-] Channel disconnected: {}", ctx.channel().remoteAddress());

        if (handler != null) {
            handler.leave(connection);
        }
        this.connection = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset".equals(cause.getMessage())) {
            log.error("{}: Connection reset.", this.connection.getRemoteAddress());
        } else if ("An established connection was aborted by the software in your host machine".equals(cause.getMessage()) ||
                "An existing connection was forcibly closed by the remote host".equals(cause.getMessage())) {
            log.error("{}: Disconnected.", this.connection.getRemoteAddress());
        } else {
            log.error("{}: Exception caught, closing channel.", this.connection.getRemoteAddress(), cause);
        }

        if (handler != null) {
            handler.leave(connection);
        }
        this.connection = null;

        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ConvoPacket chatServerPacket) {

    }

    public ConvoConnection getConnection() {
        return connection;
    }
}
