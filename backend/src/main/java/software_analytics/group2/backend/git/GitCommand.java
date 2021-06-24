package software_analytics.group2.backend.git;

import software_analytics.group2.backend.command.Terminal;
import software_analytics.group2.backend.command.TerminalCommand;
import software_analytics.group2.backend.controller.TerminalController;
import software_analytics.group2.backend.interfaces.Command;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that allows you to clone a git project and create its log file
 */
public final class GitCommand {

    private static final Logger LOGGER = Logger.getLogger(GitCommand.class.getName());

    private GitCommand() {
    }

    /**
     * Method that checkout current commitId version of project
     *
     * @param commitId    : commit that we want to checkout
     * @param projectName : analysed project
     */
    public static void checkoutCommit(String commitId, String projectName) {

        String command = "./checkoutCommit.sh " + projectName + " " + commitId;
        Terminal terminal = new Terminal(command, null, "./src/main/resources/script");
        Command terminalExecution = new TerminalCommand(terminal);
        TerminalController terminalController = new TerminalController(terminalExecution);

        try {
            terminalController.executeProcess();
            int result = terminal.getProcess().waitFor();
            LOGGER.log(Level.INFO, () -> "Checkout Commit process result: " + result);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "File not found in the given path", e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
