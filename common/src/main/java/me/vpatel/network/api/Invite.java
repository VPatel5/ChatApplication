package me.vpatel.network.api;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Invite {

    private ConvoUser user;
    private ConvoGroup group;
    private ConvoUser inviter;
    private InviteType type;
    private long internalId;

    public Invite(ConvoUser user, ConvoUser inviter) {
        this.user = user;
        this.inviter = inviter;
        this.type = InviteType.FRIEND;
    }

    public Invite(ConvoUser user, ConvoGroup group, ConvoUser inviter) {
        this.user = user;
        this.group = group;
        this.inviter = inviter;
        this.type = InviteType.GROUP;
    }

    public void setInternalId(long internalId) {
        this.internalId = internalId;
    }

    public long getInternalId() {
        return internalId;
    }

    public ConvoUser getUser() {
        return user;
    }

    public void setUser(ConvoUser user) {
        this.user = user;
    }

    public ConvoGroup getGroup() {
        return group;
    }

    public void setGroup(ConvoGroup group) {
        this.group = group;
    }

    public ConvoUser getInviter() {
        return inviter;
    }

    public void setInviter(ConvoUser inviter) {
        this.inviter = inviter;
    }

    public InviteType getType() {
        return type;
    }

    public void setType(InviteType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invite invite = (Invite) o;
        return internalId == invite.internalId &&
                Objects.equal(user, invite.user) &&
                Objects.equal(group, invite.group) &&
                Objects.equal(inviter, invite.inviter) &&
                type == invite.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(user, group, inviter, type, internalId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("user", user)
                .add("group", group)
                .add("inviter", inviter)
                .add("type", type)
                .add("internalId", internalId)
                .toString();
    }
}