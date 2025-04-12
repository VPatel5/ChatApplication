package me.vpatel.network.pipeline;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import me.vpatel.network.ConvoChannelHandler;
import me.vpatel.network.protocol.ConvoPacketRegistry;

public class ConvoPipeline extends ChannelInitializer<SocketChannel>
{
    private final ConvoPacketRegistry packetRegistry;

    public ConvoPipeline(ConvoPacketRegistry packetRegistry)
    {
        this.packetRegistry = packetRegistry;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast("lengthDecoder", new ConvoPacketLengthDecoder());
        //pipeline.addLast("decoder", new ChatServerPacketDecoder(packetRegistry, packetHandler));

        pipeline.addLast("lengthEncoder", new ConvoPacketLengthEncoder());
        pipeline.addLast("encoder", new ConvoPacketEncoder(packetRegistry));

        pipeline.addLast("handler", new ConvoChannelHandler());
    }
}
