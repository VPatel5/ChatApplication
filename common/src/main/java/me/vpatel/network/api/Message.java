package me.vpatel.network.api;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Date;

public class Message {
    private long internalId;
    private ConvoUser sender;
    private ConvoUser recipient;   // null for group
    private ConvoGroup group;      // null for private
    private Date timestamp;
    private String message;
    private MessageType messageType;

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

    public ConvoUser getRecipient() {
        return recipient;
    }

    public void setRecipient(ConvoUser recipient) {
        this.recipient = recipient;
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

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message that)) return false;
        return internalId == that.internalId
                && Objects.equal(sender,    that.sender)
                && Objects.equal(recipient, that.recipient)
                && Objects.equal(group,     that.group)
                && Objects.equal(timestamp, that.timestamp)
                && Objects.equal(message,   that.message)
                && Objects.equal(messageType,   that.messageType);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(internalId, sender, recipient, group, timestamp, message, messageType);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper ts = MoreObjects.toStringHelper(this)
                .add("internalId", internalId)
                .add("sender", sender)
                .add("timestamp", timestamp)
                .add("message", message)
                .add("type", messageType);
        if (recipient != null) ts.add("recipient", recipient);
        if (group     != null) ts.add("group",     group);
        return ts.toString();
    }
}
