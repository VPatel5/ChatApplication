package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

import java.util.ArrayList;
import java.util.List;

public class ClientGeminiRequestPacket extends ConvoPacket {

    private List<String> messageHistory;
    private String userInput;

    public ClientGeminiRequestPacket() {}

    public ClientGeminiRequestPacket(List<String> messageHistory, String userInput) {
        this.messageHistory = messageHistory;
        this.userInput = userInput;
    }

    @Override
    public void toWire(ByteBuf buf) {
        buf.writeInt(messageHistory.size());
        for (String msg : messageHistory) {
            DataTypes.writeString(msg, buf);
        }

        DataTypes.writeString(userInput, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        int size = buf.readInt();

        messageHistory = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            messageHistory.add(DataTypes.readString(buf));
        }

        userInput = DataTypes.readString(buf);
    }

    public List<String> getMessageHistory() {
        return messageHistory;
    }

    public String getUserInput() {
        return userInput;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("history", messageHistory)
                .add("input", userInput)
                .toString();
    }
}
