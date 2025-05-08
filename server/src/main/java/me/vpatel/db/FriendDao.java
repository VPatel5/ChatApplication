package me.vpatel.db;

import me.vpatel.db.tables.FriendTable;
import me.vpatel.db.tables.UsersTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterBeanMapper(FriendTable.class)
public interface FriendDao {

    @SqlUpdate("create table if not exists convo_friend(" +
            "user1 bigint not null," +
            "user2 bigint not null," +
            "timestamp timestamp not null," +
            "primary key (user1, user2)" +
            ")")
    boolean createTable();

    @SqlQuery("select * from convo_user " +
            "where id in (select user2 from convo_friend where user1 = :id) " +
            "or id in (select user1 from convo_friend where user2 = :id)")
    @RegisterBeanMapper(UsersTable.class)
    List<UsersTable> findFriends(@BindBean UsersTable user);

    @SqlUpdate("insert into convo_friend (user1, user2, timestamp) values (:user1.id, :user2.id, :now)")
    @Timestamped
    void addFriend(@BindBean("user1") UsersTable user1, @BindBean("user2") UsersTable user2);

    @SqlUpdate("delete from convo_friend " +
            "where (user1 = :user1.id and user2 = :user2.id) " +
            "or (user1 = :user2.id and user2 = :user1.id)")
    int removeFriend(@BindBean("user1") UsersTable user1, @BindBean("user2") UsersTable user2);
}