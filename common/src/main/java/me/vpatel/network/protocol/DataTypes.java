package me.vpatel.network.protocol;

import io.netty.buffer.ByteBuf;
import me.vpatel.network.api.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DataTypes {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static String readString(ByteBuf buf) {
        int length = buf.readInt();

        byte[] bytes = new byte[length];
        buf.readBytes(bytes);

        return new String(bytes, CHARSET);
    }

    public static void writeString(String string, ByteBuf buf) {
        if (string == null) {
            buf.writeInt(0);
            return;
        }
        byte[] bytes = string.getBytes(CHARSET);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static byte[] readByteArray(ByteBuf buf) {
        int len = buf.readInt();
        byte[] data = new byte[len];
        buf.readBytes(data);
        return data;
    }

    public static void writeByteArray(byte[] arr, ByteBuf buf) {
        buf.writeInt(arr.length);
        buf.writeBytes(arr);
    }

    public static boolean readBoolean(ByteBuf buf) {
        return buf.readByte() != 0;
    }

    public static void writeBoolean(boolean value, ByteBuf buf) {
        buf.writeByte(value ? 1 : 0);
    }

    public static ConvoUser readUser(ByteBuf buf) {
        long id = buf.readLong();
        UUID uuid = UUID.fromString(readString(buf));
        String name = readString(buf);
        ConvoUser user = new ConvoUser(uuid, name);
        user.setInternalId(id);
        return user;
    }

    public static void writeUser(ConvoUser user, ByteBuf buf) {
        buf.writeLong(user.getInternalId());
        writeString(user.getId().toString(), buf);
        writeString(user.getName(), buf);
    }

    public static Invite readInvite(ByteBuf buf) {
        InviteType type = InviteType.values()[buf.readInt()];
        long id = buf.readLong();
        ConvoUser user = readUser(buf);
        ConvoUser inviter = readUser(buf);
        if (type == InviteType.GROUP) {
            ConvoGroup group = readGroup(buf);
            Invite invite = new Invite(user, group, inviter);
            invite.setInternalId(id);
            return invite;
        } else {
            Invite invite = new Invite(user, inviter);
            invite.setInternalId(id);
            return invite;
        }
    }

    public static void writeInvite(Invite invite, ByteBuf buf) {
        buf.writeInt(invite.getType().ordinal());
        buf.writeLong(invite.getInternalId());
        writeUser(invite.getUser(), buf);
        writeUser(invite.getInviter(), buf);
        if (invite.getType() == InviteType.GROUP) {
            writeGroup(invite.getGroup(), buf);
        }
    }

    public static ConvoGroup readGroup(ByteBuf buf) {
        ConvoGroup group = new ConvoGroup();
        group.setOwner(readUser(buf));
        group.setName(readString(buf));
        group.setInternalId(buf.readLong());

        int size = buf.readInt();
        if (size > 0) {
            Set<ConvoUser> users = new HashSet<>();
            for (int i = 0; i < size; i++) {
                users.add(readUser(buf));
            }
            group.setUsers(users);
        }
        return group;
    }

    public static void writeGroup(ConvoGroup group, ByteBuf buf) {
        writeUser(group.getOwner(), buf);
        writeString(group.getName(), buf);
        buf.writeLong(group.getInternalId());

        if (group.getUsers() != null) {
            buf.writeInt(group.getUsers().size());
            for (ConvoUser user : group.getUsers()) {
                writeUser(user, buf);
            }
        } else {
            buf.writeInt(0);
        }
    }

    public static Message readMessage(ByteBuf buf) {
        Message message = new Message();
        message.setInternalId(buf.readLong());
        MessageType type = MessageType.values()[buf.readInt()];
        message.setMessageType(type);
        message.setRecipient(readUser(buf));
        message.setSender(readUser(buf));
        if (type == MessageType.GROUP) {
            message.setGroup(readGroup(buf));
        }
        message.setTimestamp(new Date(buf.readLong()));
        message.setMessage(readString(buf));
        return message;
    }

    public static void writeMessage(Message message, ByteBuf buf) {
        buf.writeLong(message.getInternalId());
        buf.writeInt(message.getMessageType().ordinal());
        writeUser(message.getRecipient(), buf);
        writeUser(message.getSender(), buf);
        if (message.getMessageType() == MessageType.GROUP) {
            writeGroup(message.getGroup(), buf);
        }
        buf.writeLong(message.getTimestamp().getTime());
        writeString(message.getMessage(), buf);
    }
}
