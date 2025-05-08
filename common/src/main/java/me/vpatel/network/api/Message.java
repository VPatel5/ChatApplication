package me.vpatel.network.api;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Date;

public class Message {

    private long internalId;
    private ConvoUser sender;
    private ConvoGroup group;
    private Date timestamp;
    private String message;

    public long getInternalId() {
        return internalId;
    }

    public void setInternalId(long internalId) {
        this.internalId = internalId;
    }

    public ConvoUser getSender() {
        return sender;
    }

    public void setSender(ConvoUser sender) {
        this.sender = sender;
    }

    public ConvoGroup getGroup() {
        return group;
    }

    public void setGroup(ConvoGroup group) {
        this.group = group;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
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
        Message message1 = (Message) o;
        return internalId == message1.internalId && Objects.equal(sender, message1.sender) && Objects.equal(group, message1.group) && Objects.equal(timestamp, message1.timestamp) && Objects.equal(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(internalId, sender, group, timestamp, message);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("internalId", internalId)
                .add("sender", sender)
                .add("group", group)
                .add("timestamp", timestamp)
                .add("message", message)
                .toString();
    }
}