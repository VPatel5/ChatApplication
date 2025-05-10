package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

public class ServerGeminiResponsePacket extends ConvoPacket {

    private String aiResponse;

    public ServerGeminiResponsePacket() {}

    public ServerGeminiResponsePacket(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(aiResponse, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        aiResponse = DataTypes.readString(buf);
    }

    public String getAiResponse() {
        return aiResponse;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("response", aiResponse)
                .toString();
    }
}
