package software_analytics.group2.backend.controller;

import software_analytics.group2.backend.interfaces.Command;

import java.io.IOException;

public class TerminalController {

    private final Command command;

    public TerminalController(Command command) {
        this.command = command;
    }

    public void executeProcess() throws IOException, InterruptedException {
        command.execute();
    }
}
