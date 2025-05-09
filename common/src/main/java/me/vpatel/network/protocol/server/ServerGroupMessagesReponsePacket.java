package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

import java.util.ArrayList;
import java.util.List;

public class ServerGroupMessagesReponsePacket extends ConvoPacket {

    private List<Message> messages;

    private String groupName;

    public ServerGroupMessagesReponsePacket() {}

    public ServerGroupMessagesReponsePacket(List<Message> messages, String groupName) {
        this.groupName = groupName;
        this.messages = messages;
    }

    @Override
    public void toWire(ByteBuf buf)
    {
        DataTypes.writeString(groupName, buf);
        buf.writeInt(messages.size());
        for (Message message : messages) {
            DataTypes.writeMessage(message, buf);
        }
    }

    @Override
    public void fromWire(ByteBuf buf) {
        groupName = DataTypes.readString(buf);
        messages = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            messages.add(DataTypes.readMessage(buf));
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("groupName", this.groupName)
                .add("messages", this.messages)
                .toString();
    }
}
