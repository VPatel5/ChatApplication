package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

public class ClientRegisterRequestPacket extends ConvoPacket {
    private String username;
    private String passwordHash;
    private String salt;
    private String email;

    public ClientRegisterRequestPacket() {}

    public ClientRegisterRequestPacket(String username, String passwordHash, String salt, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.email = email;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(username, buf);
        DataTypes.writeString(passwordHash, buf);
        DataTypes.writeString(salt, buf);
        DataTypes.writeString(email, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        username = DataTypes.readString(buf);
        passwordHash = DataTypes.readString(buf);
        salt = DataTypes.readString(buf);
        email = DataTypes.readString(buf);
    }

    // Getters
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("username", username)
                .add("email", email)
                .add("salt", salt)
                .add("hash", passwordHash)
                .toString();
    }
}
