package software_analytics.group2.backend.command;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Terminal {

    private static final Logger LOGGER = Logger.getLogger(Terminal.class.getName());


    private final String command;
    private final String[] env;
    private final String path;

    private Process process;

    public Terminal(String command, String[] env, String path) {
        this.command = command;
        this.env = env;
        this.path = path;
    }

    public void runProcess() {
        try {
            this.process = Runtime.getRuntime().exec(command, env, new File(path));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Execution interrupted", e);
        }
    }

    public Process getProcess() {
        return process;
    }
}
