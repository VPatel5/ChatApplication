package me.vpatel.network.protocol;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.vpatel.network.protocol.client.*;
import me.vpatel.network.protocol.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoPacketRegistry {

    private static final Logger log = LogManager.getLogger(ConvoPacketRegistry.class);

    private final BiMap<Integer, Class<? extends ConvoPacket>> serverRegistry = HashBiMap.create();
    private final BiMap<Integer, Class<? extends ConvoPacket>> clientRegistry = HashBiMap.create();

    public void init()
    {
        // TO SERVER
        register(PacketDirection.TO_SERVER, 0, ClientPingPacket.class);
        register(PacketDirection.TO_SERVER, 1, ClientLoginStartPacket.class);
        register(PacketDirection.TO_SERVER, 2, ClientEncryptionResponsePacket.class);
        register(PacketDirection.TO_SERVER, 3, ClientRegisterRequestPacket.class);
        register(PacketDirection.TO_SERVER, 4, ClientChatPacket.class);

        // TO CLIENT
        register(PacketDirection.TO_CLIENT, 0, ServerPongPacket.class);
        register(PacketDirection.TO_CLIENT, 1, ServerLoginSuccessPacket.class);
        register(PacketDirection.TO_CLIENT, 2, ServerEncryptionRequestPacket.class);
        register(PacketDirection.TO_CLIENT, 3, ServerRegisterResponsePacket.class);
        register(PacketDirection.TO_CLIENT, 4, ServerLoginFailPacket.class);
        register(PacketDirection.TO_CLIENT, 5, ServerAuthFinishedPacket.class);
        register(PacketDirection.TO_CLIENT, 6, ServerChatPacket.class);

    }

    public void register(PacketDirection direction, int packetId, Class<? extends ConvoPacket> packetClass) {
        if (direction == PacketDirection.TO_SERVER) {
            serverRegistry.put(packetId, packetClass);
        } else {
            clientRegistry.put(packetId, packetClass);
        }
    }

    public Class<? extends ConvoPacket> getPacket(PacketDirection direction, int packetId) {
        if (direction == PacketDirection.TO_SERVER) {
            return serverRegistry.get(packetId);
        } else {
            return clientRegistry.get(packetId);
        }
    }

    public void fillInfo(ConvoPacket packet) {
        Class<? extends ConvoPacket> clazz = packet.getClass();
        Integer id = serverRegistry.inverse().get(clazz);
        if (id == null) {
            id = clientRegistry.inverse().get(clazz);
        }

        if (id == null) {
            log.warn("Could not fill info for packet {}", packet);
        } else {
            packet.setId(id);
        }
    }
}
