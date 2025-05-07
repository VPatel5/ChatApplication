package me.vpatel.network;

import com.google.common.base.MoreObjects;
import io.netty.channel.ChannelHandlerContext;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.pipeline.ConvoPacketDecrypter;
import me.vpatel.network.pipeline.ConvoPacketEncrypter;
import me.vpatel.network.protocol.ConvoPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.net.SocketAddress;

public class ConvoConnection {

    private static final Logger log = LogManager.getLogger(ConvoConnection.class);

    private final ChannelHandlerContext ctx;
    private boolean authFinished = false;

    private ConvoUser user = new ConvoUser();

    public ConvoConnection(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public SocketAddress getRemoteAddress() {
        return ctx.channel().remoteAddress();
    }

    public void sendPacket(ConvoPacket packet) {
        ctx.channel().writeAndFlush(packet);
    }

    public void close(String reason) {
        ctx.channel().close();
    }

    public void clientClose() {
        ctx.channel().close();
    }

    public void setUser(ConvoUser user) {
        this.user = user;
    }

    public ConvoUser getUser() {
        return user;
    }

    public void initUser(String username) {
        user.setName(username);
    }

    public void setAuthFinished(boolean authFinished) {
        this.authFinished = authFinished;
    }

    public boolean isAuthFinished() {
        return authFinished;
    }

    public void enableEncryption(SecretKey key) {
        log.info("Enabling encryption");
        ctx.channel().pipeline().addBefore("lengthDecoder", "decrypter", new ConvoPacketDecrypter(CryptUtil.createContinuousCipher(Cipher.DECRYPT_MODE, key)));
        ctx.channel().pipeline().addBefore("lengthEncoder", "encrypter", new ConvoPacketEncrypter(CryptUtil.createContinuousCipher(Cipher.ENCRYPT_MODE, key)));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("addr", getRemoteAddress())
                .add("user", user)
                .toString();
    }
}
