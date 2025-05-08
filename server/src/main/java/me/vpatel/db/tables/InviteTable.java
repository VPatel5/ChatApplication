package me.vpatel.db.tables;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import me.vpatel.api.UsersHandler;
import org.jdbi.v3.core.enums.EnumByOrdinal;

import java.time.OffsetDateTime;

public class InviteTable {

    private long id;
    private long user;
    private long groupId;
    private long inviter;
    @EnumByOrdinal
    private InviteType type;
    private OffsetDateTime timestamp;
    private boolean active;

    public InviteTable() {

    }

    public InviteTable(long user, long inviter) {
        this.user = user;
        this.inviter = inviter;
        this.type = InviteType.FRIEND;
    }

    public InviteTable(long user, long inviter, long groupId) {
        this.user = user;
        this.inviter = inviter;
        this.groupId = groupId;
        this.type = InviteType.GROUP;
    }

    public static InviteTable of(Invite invite) {
        InviteTable table;
        if (invite.getType() == InviteType.FRIEND) {
            table = new InviteTable(invite.getUser().getInternalId(), invite.getInviter().getInternalId());
        } else {
            table = new InviteTable(invite.getUser().getInternalId(), invite.getInviter().getInternalId(), invite.getGroup().getInternalId());
        }
        table.setId(invite.getInternalId());
        return table;
    }

    public Invite convert(UsersHandler usersHandler, GroupsHandler groupsHandler) {
        FalcunUser falcunUser = usersHandler.getOrCacheUser(user);
        FalcunUser falcunInviter = usersHandler.getOrCacheUser(inviter);
        Invite invite;
        if (getType() == InviteType.FRIEND) {
            invite = new Invite(falcunUser, falcunInviter);
        } else {
            FalcunGroup group = groupsHandler.getOrCacheGroup(groupId);
            invite = new Invite(falcunUser, group,falcunInviter);
        }
        invite.setInternalId(getId());
        return invite;
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

    public long getInviter() {
        return inviter;
    }

    public void setInviter(long inviter) {
        this.inviter = inviter;
    }

    @EnumByOrdinal
    public InviteType getType() {
        return type;
    }

    @EnumByOrdinal
    public void setType(InviteType type) {
        this.type = type;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InviteTable that = (InviteTable) o;
        return id == that.id &&
                user == that.user &&
                groupId == that.groupId &&
                inviter == that.inviter &&
                active == that.active &&
                type == that.type &&
                Objects.equal(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, user, groupId, inviter, type, timestamp, active);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("user", user)
                .add("groupId", groupId)
                .add("inviter", inviter)
                .add("type", type)
                .add("timestamp", timestamp)
                .add("active", active)
                .toString();
    }
}
