package me.vpatel.network.pipeline;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import me.vpatel.network.ConvoChannelHandler;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacketHandler;
import me.vpatel.network.protocol.ConvoPacketRegistry;

public class ConvoPipeline extends ChannelInitializer<SocketChannel> {

    private final ConvoPacketRegistry packetRegistry;
    private final ConvoHandler handler;
    private final ConvoPacketHandler packetHandler;

    public ConvoPipeline(ConvoPacketRegistry packetRegistry, ConvoHandler handler, ConvoPacketHandler packetHandler) {
        this.packetRegistry = packetRegistry;
        this.handler = handler;
        this.packetHandler = packetHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

         pipeline.addLast("lengthDecoder", new ConvoPacketLengthDecoder());
         pipeline.addLast("decoder", new ConvoPacketDecoder(packetRegistry, packetHandler));
         pipeline.addLast("lengthEncoder", new ConvoPacketLengthEncoder());
         pipeline.addLast("encoder", new ConvoPacketEncoder(packetRegistry));
         pipeline.addLast("handler", new ConvoChannelHandler(handler));
    }
}
