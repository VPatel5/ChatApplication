package me.vpatel.db;

import me.vpatel.db.tables.MessageTable;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Timestamped;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterBeanMapper(MessageTable.class)
public interface MessageDao {

    @SqlUpdate(
            "create table if not exists convo_message (" +
                    " id bigint primary key auto_increment," +
                    " sender_id bigint not null," +
                    " recipient_id bigint," +
                    " group_id bigint," +
                    " message varchar(255) not null," +
                    " timestamp timestamp not null" +
                    ")"
    )
    boolean createTable();

    @Timestamped
    @SqlUpdate(
            "insert into convo_message (sender_id, recipient_id, group_id, message, timestamp) " +
                    " values (:message.senderId, :message.recipientId, :message.groupId, :message.message, :now)"
    )
    void saveMessage(@BindBean("message") MessageTable message);

    @SqlQuery(
            "select * from convo_message " +
                    "where group_id = :groupId " +
                    "order by timestamp " +
                    "limit :count"
    )
    List<MessageTable> retrieveGroupMessages(long groupId, long count);

    @SqlQuery(
            "SELECT * " +
                    "  FROM convo_message " +
                    " WHERE group_id = -1 " +
                    "   AND (sender_id = :user1 OR recipient_id = :user1) " +
                    " ORDER BY timestamp " +
                    " LIMIT :limit"
    )
    List<MessageTable> retrievePrivateMessages(long user1, long limit);
}
