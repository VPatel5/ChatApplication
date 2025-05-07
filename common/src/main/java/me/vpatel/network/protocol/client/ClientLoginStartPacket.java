package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.DataTypes;
import me.vpatel.network.protocol.ConvoPacket;

public class ClientLoginStartPacket extends ConvoPacket {

    private String username;
    private String password;

    public ClientLoginStartPacket() {

    }

    public ClientLoginStartPacket(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(username, buf);
        DataTypes.writeString(password, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        this.username = DataTypes.readString(buf);
        this.password = DataTypes.readString(buf);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .toString();
    }
}