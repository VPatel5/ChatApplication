package me.vpatel.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.vpatel.api.FriendsHandler;
import me.vpatel.api.GroupsHandler;
import me.vpatel.api.UsersHandler;
import me.vpatel.console.ConvoConsole;
import me.vpatel.db.DBHandler;
import me.vpatel.network.pipeline.ConvoPipeline;
import me.vpatel.network.properties.Property;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacketRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoServer {

    private static final Logger log = LogManager.getLogger(ConvoServer.class);

    public static final Property<String> CHATGPT_KEY = new Property<>("chatgpt.key", String.class, "");
    public static final Property<String> GEMINI_KEY = new Property<>("gemini.key", String.class, "");
    public static final Property<String> DB_USER = new Property<>("db.user", String.class, "user");
    public static final Property<String> DB_PASS = new Property<>("db.pass", String.class, "pass");
    public static final Property<String> DB_NAME = new Property<>("db.name", String.class, "convo");
    public static final Property<String> DB_HOST = new Property<>("db.host", String.class, "localhost");
    public static final Property<Integer> DB_PORT = new Property<>("db.port", Integer.class, 3306);

    private final int port;

    private ConvoConsole console;
    private ConvoPacketRegistry packetRegistry;
    private ConvoServerHandler handler;
    private ConvoServerPacketHandler packetHandler;
    private AuthHandler authHandler;
    private DBHandler dbHandler;
    private UsersHandler usersHandler;
    private FriendsHandler friendsHandler;
    private GroupsHandler groupsHandler;

    public ConvoServer(int port)
    {
        this.port = port;
    }

    public static void main(String[] args)
    {
        log.info("Starting up chat server");

        ConvoServer chatServer = new ConvoServer(8080);
        chatServer.start();
    }

    public void start()
    {
        log.info("Starting up server console");
        console = new ConvoConsole();
        console.start();

        log.info("Initializing");
        this.handler = new ConvoServerHandler(this);

        this.packetRegistry = new ConvoPacketRegistry();
        this.packetRegistry.init();

        this.packetHandler = new ConvoServerPacketHandler(handler);
        this.packetHandler.init();

        dbHandler = new DBHandler();
        dbHandler.setup();
        dbHandler.createTables();

        this.authHandler = new AuthHandler(this);

        usersHandler = new UsersHandler(this);
        groupsHandler = new GroupsHandler(this);
        friendsHandler = new FriendsHandler(this);

        log.info("Booting up server socket");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ConvoPipeline(packetRegistry, handler, packetHandler));

            bootstrap.bind(port).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Error occurred starting socket", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public AuthHandler getAuthHandler() {
        return authHandler;
    }

    public DBHandler getDbHandler() {
        return dbHandler;
    }

    public UsersHandler getUsersHandler() {
        return usersHandler;
    }

    public FriendsHandler getFriendsHandler() {
        return friendsHandler;
    }

    public GroupsHandler getGroupsHandler() {
        return groupsHandler;
    }

    public ConvoServerHandler getHandler() {
        return handler;
    }
}

