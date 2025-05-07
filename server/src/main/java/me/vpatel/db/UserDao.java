package me.vpatel.db;

import me.vpatel.db.tables.UsersTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterBeanMapper(UsersTable.class)
public interface UserDao {

    @SqlUpdate("create table if not exists convo_user(" +
            "id bigint primary key not null auto_increment," +
            "uuid varchar(36) unique," +
            "name varchar(255) not null," +
            "passwordHash varchar(255) not null," +
            "salt varchar(255) not null," +
            "timestamp timestamp not null" +
            ")")
    boolean createTable();

    @SqlQuery("select * from convo_user where id = :id")
    UsersTable getById(long id);

    @SqlQuery("select * from convo_user where uuid = :uuid")
    UsersTable getByUUID(String uuid);

    @SqlQuery("select * from convo_user where name = :name")
    UsersTable getByName(String name);

    @SqlUpdate("insert into convo_user (uuid, name, passwordHash, salt, timestamp) " +
            "values (:uuid, :name, :passwordHash, :salt, :now)")
    @Timestamped
    boolean create(@BindBean UsersTable user);
}