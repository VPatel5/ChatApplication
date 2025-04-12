package me.vpatel.console;

import me.vpatel.client.ConvoClient;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public class ConvoConsole extends SimpleTerminalConsole {

    private static final Logger log = LogManager.getLogger(ConvoConsole.class);

    private final ConvoClient client;

    public ConvoConsole(ConvoClient client)
    {
        this.client = client;
    }

    @Override
    public void start() {
        new Thread(super::start, "TerminalThread").start();
    }

    @Override
    protected boolean isRunning() {
        return true;
    }

    @Override
    protected void runCommand(String s) {
        try {
            executeCommand(s);
        } catch (Exception ex)
        {
            log.error("Error occurred executing command {}", s, ex);
        }
    }

    public void executeCommand(String s)
    {
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder)
    {
        return super.buildReader(builder.appName("ConvoClient"));
    }
    @Override
    protected void shutdown() {
        log.info("Shutting down");
        System.exit(0);
    }
}
