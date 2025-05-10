package me.vpatel.network.api;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.List;
import java.util.UUID;

public class ConvoUser {

    private long internalId;
    private UUID id;
    private String name;
    private List<ConvoUser> friends;

    public ConvoUser() {

    }
    public ConvoUser(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public ConvoUser(UUID id, int internalId, String name) {
        this.id = id;
        this.internalId = internalId;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getInternalId() {
        return internalId;
    }

    public void setInternalId(long internalId) {
        this.internalId = internalId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConvoUser user = (ConvoUser) o;
        return internalId == user.internalId &&
                Objects.equal(id, user.id) &&
                Objects.equal(name, user.name) &&
                Objects.equal(friends, user.friends);
    }

    public static final ConvoUser AI_USER = new ConvoUser(
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            0,
            "AI"
    );

    @Override
    public int hashCode() {
        return Objects.hashCode(internalId, id, name, friends);
    }
}