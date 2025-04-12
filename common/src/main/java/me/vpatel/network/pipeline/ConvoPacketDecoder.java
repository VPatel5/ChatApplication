package me.vpatel.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import me.vpatel.network.ConvoChannelHandler;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.ConvoPacketHandler;
import me.vpatel.network.protocol.ConvoPacketRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ConvoPacketDecoder extends ByteToMessageDecoder {

    private static final Logger log = LogManager.getLogger(ConvoPacketDecoder.class);

    private final ConvoPacketRegistry packetRegistry;
    private final ConvoPacketHandler packetHandler;

    public ConvoPacketDecoder(ConvoPacketRegistry packetRegistry, ConvoPacketHandler packetHandler) {
        this.packetRegistry = packetRegistry;
        this.packetHandler = packetHandler;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        ConvoConnection connection = ctx.pipeline().get(ConvoChannelHandler.class).getConnection();

        if (in.readableBytes() < 4) {
            log.warn("Can't decode incoming packet, it only contains {} bytes!", in.readableBytes());
            in.skipBytes(in.readableBytes());
            return;
        }

        int packetId = in.readInt();
        if (log.isDebugEnabled()) {
            log.debug("got packet id {}, bytes to read {}", packetId, in.readableBytes());
        }
        Class<? extends ConvoPacket> packetClass = packetRegistry.getPacket(packetHandler.getDirection(), packetId);

        if (packetClass == null) {
            log.warn("Couldn't find a packet class for {}:{}", packetHandler.getDirection(), packetId);
            in.skipBytes(in.readableBytes());
            return;
        }

        ConvoPacket packet = null;
        for (Constructor<?> constructor : packetClass.getConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                try {
                    packet = (ConvoPacket) constructor.newInstance();
                } catch (IllegalAccessException e) {
                    log.warn("Can't construct packet for id {}", packetId, e);
                    in.skipBytes(in.readableBytes());
                    return;
                } catch (InstantiationException e) {
                    log.warn("Can't construct packet for id {}", packetId, e);
                    in.skipBytes(in.readableBytes());
                    return;
                } catch (InvocationTargetException e) {
                    log.warn("Can't construct packet for id {}", packetId, e);
                    in.skipBytes(in.readableBytes());
                    return;
                }
            }
        }

        if (packet == null) {
            log.warn("Couldn't construct packet {}:{} {}", packetHandler.getDirection(), packetId, packetClass.getName());
            in.skipBytes(in.readableBytes());
            return;
        }

        packet.setDirection(packetHandler.getDirection());
        packet.setId(packetId);

        try {
            packet.fromWire(in);
        } catch (Exception ex) {
            log.warn("Error while decoding packet {}:{} {}", packetHandler.getDirection(), packetId, packetClass.getName(), ex);
            in.skipBytes(in.readableBytes());
            return;
        }

        log.debug("packet is {}", packet);

        try {
            packetHandler.handle(connection, packet);
        } catch (Exception ex) {
            log.warn("Error while packet processing", ex);
        }

        if (in.readableBytes() > 0) {
            log.warn("Didn't fully read packet {}! {} bytes to go", packet.getClass().getSimpleName(), in.readableBytes());
            in.skipBytes(in.readableBytes());
        }
    }
}
