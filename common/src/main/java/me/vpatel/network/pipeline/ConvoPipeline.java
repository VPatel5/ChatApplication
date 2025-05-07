package me.vpatel.network.pipeline;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import me.vpatel.network.ConvoChannelHandler;
import me.vpatel.network.ConvoWebSocketHandler;
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
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // --- WebSocket support ---
//        pipeline.addLast("httpServerCodec", new HttpServerCodec());
//        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
//        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
//        pipeline.addLast("websocketHandler", new WebSocketServerProtocolHandler("/ws", null, true));
//        pipeline.addLast("wsFrameAdapter", new ConvoWebSocketHandler(packetRegistry, handler, packetHandler));

        // --- Optional: if you still want to support raw TCP clients ---
         pipeline.addLast("lengthDecoder", new ConvoPacketLengthDecoder());
         pipeline.addLast("decoder", new ConvoPacketDecoder(packetRegistry, packetHandler));
         pipeline.addLast("lengthEncoder", new ConvoPacketLengthEncoder());
         pipeline.addLast("encoder", new ConvoPacketEncoder(packetRegistry));
         pipeline.addLast("handler", new ConvoChannelHandler(handler));
    }
}
