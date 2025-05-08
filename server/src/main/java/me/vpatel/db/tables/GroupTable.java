package me.vpatel.db.tables;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.time.OffsetDateTime;
import java.util.HashSet;

import me.vpatel.api.UsersHandler;
import me.vpatel.network.api.ConvoGroup;

public class GroupTable {

    private long id;
    private String name;
    private long owner;
    private OffsetDateTime timestamp;

    public ConvoGroup convert(UsersHandler usersHandler) {
        ConvoGroup group = new ConvoGroup();
        group.setOwner(usersHandler.getOrCacheUser(owner));
        group.setInternalId(id);
        group.setName(name);
        group.setUsers(new HashSet<>()); // users need to be filled separately
        return group;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupTable that = (GroupTable) o;
        return id == that.id &&
                Objects.equal(name, that.name) &&
                Objects.equal(owner, that.owner) &&
                Objects.equal(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, owner, timestamp);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("owner", owner)
                .add("timestamp", timestamp)
                .toString();
    }
}