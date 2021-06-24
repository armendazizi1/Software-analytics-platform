package software_analytics.group2.backend.command;

import software_analytics.group2.backend.interfaces.Command;

public class TerminalCommand implements Command {

    private final Terminal terminal;

    public TerminalCommand(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public void execute() {
        terminal.runProcess();
    }
}
