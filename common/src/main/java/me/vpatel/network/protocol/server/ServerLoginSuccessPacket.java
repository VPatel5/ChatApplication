package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.DataTypes;
import me.vpatel.network.protocol.ConvoPacket;

import java.util.UUID;

public class ServerLoginSuccessPacket extends ConvoPacket {

    private String username;
    private UUID uuid;

    public ServerLoginSuccessPacket() {

    }

    public ServerLoginSuccessPacket(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(username, buf);
        DataTypes.writeString(uuid.toString(), buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        username = DataTypes.readString(buf);
        uuid = UUID.fromString(DataTypes.readString(buf));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .add("uuid", uuid)
                .toString();
    }
}