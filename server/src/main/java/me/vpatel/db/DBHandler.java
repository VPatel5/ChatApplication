package me.vpatel.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.vpatel.server.ConvoServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class DBHandler {
    private static final Logger log = LogManager.getLogger(DBHandler.class);
    private static final Logger sqlLogger = LogManager.getLogger(log.getName() + ".sql");

    private Jdbi jdbi;

    public void setup() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" +
                ConvoServer.DB_HOST.get() + ":" +
                ConvoServer.DB_PORT.get() + "/" +
                ConvoServer.DB_NAME.get());

        config.setUsername(ConvoServer.DB_USER.get());
        config.setPassword(ConvoServer.DB_PASS.get());

        config.addDataSourceProperty("autoCommit", "false");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");

        HikariDataSource ds = new HikariDataSource(config);
        jdbi = Jdbi.create(ds);
        jdbi.installPlugin(new SqlObjectPlugin());

        if (sqlLogger.isDebugEnabled()) {
            SqlLogger debugLogger = new SqlLogger() {
                @Override
                public void logAfterExecution(StatementContext context) {
                    sqlLogger.debug("sql: {}", context.getRenderedSql());
                }
            };
            jdbi.setSqlLogger(debugLogger);
        }
    }

    public void createTables() {
        jdbi.withExtension(UserDao.class, userDao -> {
            userDao.createTable();
            return null;
        });
        jdbi.withExtension(FriendDao.class, friendDao -> {
            friendDao.createTable();
            return null;
        });
        jdbi.withExtension(InviteDao.class, inviteDao -> {
            inviteDao.createTable();
            return null;
        });
        jdbi.withExtension(GroupDao.class, groupDao -> {
            groupDao.createTable();
            groupDao.createMembershipTable();
            return null;
        });
        jdbi.withExtension(MessageDao.class, messageDao -> {
            messageDao.createTable();
            return null;
        });
    }

    public Jdbi jdbi() {
        return jdbi;
    }
}
