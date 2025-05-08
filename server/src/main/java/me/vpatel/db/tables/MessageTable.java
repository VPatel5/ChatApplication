package me.vpatel.db.tables;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import me.vpatel.api.UsersHandler;
import me.vpatel.network.api.Message;

import java.time.OffsetDateTime;
import java.util.Date;

public class MessageTable {

    private long id;
    private long user;
    private long groupId;
    private OffsetDateTime timestamp;
    private String message;

    public static Message convert(MessageTable table, UsersHandler usersHandler) {
        //TODO: Groups handler
        Message message = new Message();
        message.setInternalId(table.getId());
        message.setSender(usersHandler.getOrCacheUser(table.getUser()));
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

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long group) {
        this.groupId = group;
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
        return id == that.id && user == that.user && groupId == that.groupId && Objects.equal(timestamp, that.timestamp) && Objects.equal(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, user, groupId, timestamp, message);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("user", user)
                .add("group", groupId)
                .add("timestamp", timestamp)
                .add("message", message)
                .toString();
    }
}
