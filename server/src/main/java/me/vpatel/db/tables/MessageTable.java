package me.vpatel.db.tables;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import me.vpatel.api.GroupsHandler;
import me.vpatel.api.UsersHandler;
import me.vpatel.network.api.Message;
import me.vpatel.network.api.MessageType;

import java.time.OffsetDateTime;
import java.util.Date;

public class MessageTable {

    private long id;
    private long senderId;
    private long recipientId;
    private long groupId;
    private OffsetDateTime timestamp;
    private String message;

    public static Message convert(MessageTable table, UsersHandler usersHandler, GroupsHandler groupsHandler) {
        Message message = new Message();
        message.setInternalId(table.getId());
        message.setSender(usersHandler.getOrCacheUser(table.getSenderId()));
        if (table.getGroupId() == -1)
        {
            message.setRecipient(usersHandler.getOrCacheUser(table.getRecipientId()));
            message.setMessageType(MessageType.USER);
        }
        else
        {
            message.setGroup(groupsHandler.getOrCacheGroup(table.getGroupId()));
            message.setMessageType(MessageType.GROUP);
        }
        message.setTimestamp(new Date(table.getTimestamp().toEpochSecond()));
        message.setMessage(table.getMessage());
        return message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long group) {
        this.groupId = group;
    }

    public void setRecipientId(long recipientId) {
        this.recipientId = recipientId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageTable that = (MessageTable) o;
        return id == that.id && senderId == that.senderId && recipientId == that.recipientId && groupId == that.groupId && Objects.equal(timestamp, that.timestamp) && Objects.equal(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, senderId, recipientId, groupId, timestamp, message);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("senderId", senderId)
                .add("recipientId", recipientId)
                .add("groupId", groupId)
                .add("timestamp", timestamp)
                .add("message", message)
                .toString();
    }
}
