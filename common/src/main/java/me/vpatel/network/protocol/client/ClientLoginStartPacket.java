package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.DataTypes;
import me.vpatel.network.protocol.ConvoPacket;

public class ClientLoginStartPacket extends ConvoPacket {

    private String username;

    public ClientLoginStartPacket() {

    }

    public ClientLoginStartPacket(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(username, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        this.username = DataTypes.readString(buf);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .toString();
    }
}