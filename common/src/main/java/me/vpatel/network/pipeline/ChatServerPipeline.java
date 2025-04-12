package me.vpatel.network.pipeline;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import me.vpatel.network.ConvoChannelHandler;

public class ChatServerPipeline extends ChannelInitializer<SocketChannel>
{
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast("lengthDecoder", new ChatServerPacketLengthDecoder());
        //pipeline.addLast("decoder", new ChatServerPacketDecoder(packetRegistry, packetHandler));

        pipeline.addLast("lengthEncoder", new ChatServerPacketLengthEncoder());
        pipeline.addLast("encoder", new ChatServerPacketEncoder());

        pipeline.addLast("handler", new ConvoChannelHandler());
    }
}
