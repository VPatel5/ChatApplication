package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.CryptUtil;
import me.vpatel.network.protocol.DataTypes;
import me.vpatel.network.protocol.ConvoPacket;

import java.security.PublicKey;

public class ServerEncryptionRequestPacket extends ConvoPacket {

    private PublicKey publicKey;
    private byte[] verifyToken;

    public ServerEncryptionRequestPacket() {

    }

    public ServerEncryptionRequestPacket(PublicKey publicKey, byte[] verifyToken) {
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getVerifyToken() {
        return verifyToken;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeByteArray(publicKey.getEncoded(), buf);
        DataTypes.writeByteArray(verifyToken, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        publicKey = CryptUtil.decode(DataTypes.readByteArray(buf));
        verifyToken = DataTypes.readByteArray(buf);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("publicKey", publicKey)
                .add("verifyToken", verifyToken)
                .toString();
    }
}