package software_analytics.group2.backend.interfaces;

import java.io.IOException;

public interface Command {
    void execute() throws IOException, InterruptedException;
}
