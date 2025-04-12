package me.vpatel.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.vpatel.network.protocol.ChatServerPacket;

public class ChatServerChannelHandler extends SimpleChannelInboundHandler<ChatServerPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ChatServerPacket chatServerPacket) throws Exception {

    }
}
