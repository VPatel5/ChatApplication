package me.vpatel.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.*;

public class ConvoWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final ConvoPacketRegistry registry;
    private final ConvoHandler handler;
    private final ConvoPacketHandler packetHandler;
    private ConvoConnection connection;

    public ConvoWebSocketHandler(ConvoPacketRegistry registry, ConvoHandler handler, ConvoPacketHandler packetHandler) {
        this.registry = registry;
        this.handler = handler;
        this.packetHandler = packetHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.connection = new ConvoConnection(ctx);
        handler.join(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        handler.leave(connection);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (!(frame instanceof BinaryWebSocketFrame)) {
            return;
        }

        ByteBuf in = frame.content();

        if (in.readableBytes() < 4) {
            return;
        }

        int packetId = in.readInt();
        Class<? extends ConvoPacket> clazz = registry.getPacket(packetHandler.getDirection(), packetId);
        if (clazz == null) return;

        try {
            ConvoPacket packet = clazz.getDeclaredConstructor().newInstance();
            packet.setDirection(packetHandler.getDirection());
            packet.setId(packetId);
            packet.fromWire(in);
            packetHandler.handle(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(ConvoPacket packet) {
        ByteBuf buf = Unpooled.buffer();
        registry.fillInfo(packet);
        buf.writeInt(packet.getId());
        packet.toWire(buf);
        connection.sendPacket(packet); // optionally wrap in BinaryWebSocketFrame
    }
}
