package me.vpatel.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.vpatel.db.UserDao;
import me.vpatel.db.tables.UsersTable;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.server.ConvoServer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UsersHandler {

    private final ConvoServer server;

    private final LoadingCache<Long, ConvoUser> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public ConvoUser load(Long id) {
                    UsersTable table = server.getDbHandler().jdbi().withExtension(UserDao.class, handle -> handle.getById(id));

                    if (table == null) {
                        return null;
                    }

                    return table.convert();
                }
            });

    public UsersHandler(ConvoServer server) {
        this.server = server;
    }

    public ConvoUser createOrLoadUser(UUID id, String name) {
        UsersTable table = server.getDbHandler().jdbi().withExtension(UserDao.class, handle -> {
            UsersTable usersTable = handle.getByUUID(id.toString());
            if (usersTable == null) {
                if (!handle.create(new UsersTable(id.toString(), name))){
                    return null;
                }
                usersTable = handle.getByUUID(id.toString());
            }

            return usersTable;
        });

        if (table == null) {
            return null;
        }

        return table.convert();
    }

    public ConvoUser getUser(String name) {
        UsersTable table = server.getDbHandler().jdbi().withExtension(UserDao.class, handle -> handle.getByName(name));

        if (table == null) {
            return null;
        }

        return table.convert();
    }

    public ConvoUser getUser(UUID id) {
        UsersTable table = server.getDbHandler().jdbi().withExtension(UserDao.class, handle -> handle.getByUUID(id.toString()));

        if (table == null) {
            return null;
        }

        return table.convert();
    }

    public List<ConvoUser> getAll()
    {
        List<UsersTable> users = server.getDbHandler().jdbi().withExtension(UserDao.class, UserDao::getAll);

        return users.stream().map(UsersTable::convert).collect(Collectors.toList());
    }

    public ConvoUser getOrCacheUser(long id) {
        try {
            return cache.get(id);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}