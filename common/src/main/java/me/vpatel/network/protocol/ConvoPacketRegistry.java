package me.vpatel.network.protocol;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoPacketRegistry {

    private static final Logger log = LogManager.getLogger(ConvoPacketRegistry.class);

    private final BiMap<Integer, Class<? extends ConvoPacket>> serverRegistry = HashBiMap.create();
    private final BiMap<Integer, Class<? extends ConvoPacket>> clientRegistry = HashBiMap.create();

    public void init()
    {

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
