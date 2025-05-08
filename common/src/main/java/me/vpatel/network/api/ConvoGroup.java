package me.vpatel.network.api;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Set;

public class ConvoGroup {

    private ConvoUser owner;
    private String name;
    private Set<ConvoUser> users;
    private long internalId;

    public long getInternalId() {
        return internalId;
    }

    public void setInternalId(long internalId) {
        this.internalId = internalId;
    }

    public ConvoUser getOwner() {
        return owner;
    }

    public void setOwner(ConvoUser owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ConvoUser> getUsers() {
        return users;
    }

    public void setUsers(Set<ConvoUser> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConvoGroup group = (ConvoGroup) o;
        return internalId == group.internalId &&
                Objects.equal(owner, group.owner) &&
                Objects.equal(name, group.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(owner, name, internalId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("owner", owner)
                .add("name", name)
                .add("users", users)
                .add("internalId", internalId)
                .toString();
    }
}