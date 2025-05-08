package me.vpatel.db;

import me.vpatel.db.tables.GroupTable;
import me.vpatel.db.tables.UsersTable;
import me.vpatel.network.api.ConvoUser;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterBeanMapper(GroupTable.class)
public interface GroupDao {

    @SqlUpdate("create table if not exists convo_group (" +
            "id bigint primary key auto_increment," +
            "name varchar(255) not null," +
            "owner bigint not null," +
            "timestamp timestamp not null" +
            ")")
    boolean createTable();

    @SqlUpdate("create table if not exists convo_group_members (" +
            "userId bigint not null ," +
            "groupId varchar(255) not null," +
            "timestamp timestamp not null" +
            ")")
    boolean createMembershipTable();

    @SqlQuery("select * from convo_group where id = :id")
    GroupTable getById(long id);

    @SqlQuery("select * from convo_group g where name = :name and :user.internalId in (select userId from convo_group_members gm where g.id = gm.groupId)")
    GroupTable getGroup(String name, @BindBean("user") ConvoUser user);

    @Timestamped
    @GetGeneratedKeys
    @SqlUpdate("insert into convo_group (name, owner, timestamp) values (:name, :owner.internalId, :now)")
    long createGroup(String name, @BindBean("owner") ConvoUser owner);

    @SqlUpdate("delete from convo_group where id = :id")
    boolean deleteGroup(long id);

    @Timestamped
    @SqlUpdate("insert into convo_group_members (userId, groupId, timestamp) values ( :user.internalId, :groupId, :now)")
    void addUser(long groupId, @BindBean("user") ConvoUser user);

    @SqlUpdate("delete from convo_group_members where groupId = :groupId and userId = :user.internalId")
    void removeUser(long groupId, @BindBean("user") ConvoUser user);

    @RegisterBeanMapper(UsersTable.class)
    @SqlQuery("select * from convo_user where id in (select userId from convo_group_members where groupId = :groupId)")
    List<UsersTable> getGroupMembers(long groupId);

    @SqlQuery("select * from convo_group where id in (select groupId from convo_group_members where userId = :user.internalId)")
    List<GroupTable> getGroups(@BindBean("user") ConvoUser user);
}