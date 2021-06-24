package software_analytics.group2.backend.analysis;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import software_analytics.group2.backend.command.Terminal;
import software_analytics.group2.backend.command.TerminalCommand;
import software_analytics.group2.backend.controller.TerminalController;
import software_analytics.group2.backend.interfaces.Command;
import software_analytics.group2.backend.utility.FileUtility;
import software_analytics.group2.backend.utility.Timer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FetchRepositoryData {

    private static final Logger LOGGER = Logger.getLogger(FetchRepositoryData.class.getName());
    private static int cloningFailed = 0;

    private FetchRepositoryData() {
    }

    /**
     * Method that allows to clone the repository passed as parameter.
     *
     * @param repoPath      : url of the analysed repository.
     * @param directoryPath : path in which to save the cloned project.
     */
    public static void cloneRepository(String repoPath, String directoryPath) {
        try {
            LOGGER.log(Level.INFO, () -> "Cloning " + repoPath + " into " + directoryPath);
            Timer cloningTime = new Timer();
            Git.cloneRepository().setURI(repoPath).setDirectory(Paths.get(directoryPath).toFile()).call();
            cloningTime.stopTimer();
            LOGGER.log(Level.INFO, () -> "Completed " + repoPath + " cloning in " + cloningTime.computeTotalTime() + " ms");
        } catch (GitAPIException e) {
            LOGGER.log(Level.WARNING, "Error occurs during cloning of the repository");
            if (cloningFailed < 10) {
                cloningFailed++;
                cloneRepository(repoPath, directoryPath);
            }
        }
    }

    /**
     * Method that allows to create the log file with a custom data structure
     *
     * @param projectName : url of the repository to analyse.
     */
    public static void fetchRepoStatistics(String projectPath, String projectName, String repoPath) {
        cloningFailed = 0;
        String dataPath = "https://api.github.com/repos/" + repoPath
                .replace("https://github.com/", "")
                .replace(".git", "");

        String command = "./repoStatistics.sh " + projectName + " " + dataPath;

        Terminal terminal = new Terminal(command, null, "./src/main/resources/script");
        Command terminalExecution = new TerminalCommand(terminal);
        TerminalController terminalController = new TerminalController(terminalExecution);

        try {
            terminalController.executeProcess();
            fetchAllIssuePages(projectPath, projectName, dataPath);
            fetchAllPullRequestPages(projectPath, projectName, dataPath);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "File not found in the given path", e);
        } catch (InterruptedException e) {
            Thread current = Thread.currentThread();
            String message = "Thread" + current.getName() + " interruption";
            LOGGER.log(Level.SEVERE, message, e);
            current.interrupt();
        }
    }

    /**
     * Method to all the issue that are in the other pages.
     *
     * @param projectName : name of the analysed project.
     * @param issuePath   : path to the folder containing the issue files.
     */
    private static void fetchAllIssuePages(String projectsPath, String projectName, String issuePath) throws IOException, InterruptedException {
        int position = 28;
        String directoryPath = projectsPath + projectName;
        String line13;

        Thread.sleep(4000);
        line13 = FileUtility.getLine(12, directoryPath + "/issue/issue1.txt");

        assert line13 != null;
        int[] pageValues = verifyMoreData(line13);
        if (pageValues.length != 0) {
            String command = "../../../script/pageIssue.sh " + issuePath + " " + pageValues[0] + " " + pageValues[1];
            position = getPosition(position, directoryPath, command);
        }
        convertDataFile(position, directoryPath, "/issue/");
    }

    /**
     * Method to all the issue that are in the other pages.
     *
     * @param projectName     : name of the analysed project.
     * @param pullRequestPath : path to the folder containing the pull request files.
     */
    private static void fetchAllPullRequestPages(String projectsPath, String projectName, String pullRequestPath) throws IOException, InterruptedException {
        int position = 28;
        String directoryPath = projectsPath + projectName;
        String line13;

        Thread.sleep(4000);
        line13 = FileUtility.getLine(12, directoryPath + "/pullRequest/pullRequest1.txt");

        assert line13 != null;
        int[] pageValues = verifyMoreData(line13);
        if (pageValues.length != 0) {
            String command = "../../../script/pagePullRequest.sh " + pullRequestPath + " " + pageValues[0] + " " + pageValues[1];
            position = getPosition(position, directoryPath, command);
        }
        convertDataFile(position, directoryPath, "/pullRequest/");
    }

    /**
     * Method to get the right line position where called function starts copying.
     *
     * @param position      : initial position
     * @param directoryPath : correct path to the directory.
     * @param command       : command string to execute.
     * @return : the right position value.
     * @throws IOException          : exception handled.
     * @throws InterruptedException : exception handled.
     */
    private static int getPosition(int position, String directoryPath, String command) throws IOException, InterruptedException {
        Terminal terminal = new Terminal(command, null, directoryPath);
        Command terminalExecution = new TerminalCommand(terminal);
        TerminalController terminalController = new TerminalController(terminalExecution);
        terminalController.executeProcess();
        int result = terminal.getProcess().waitFor();
        LOGGER.log(Level.INFO, () -> "Statistics process result: " + result);
        position += 1;
        return position;
    }

    /**
     * Method to compute the issue page range.
     *
     * @param line13 : line that contain the issue page range data.
     * @return : array containing the range of the issue pages
     */
    public static int[] verifyMoreData(String line13) {
        String pattern = "=[0-9]+&p";
        Pattern r = Pattern.compile(pattern);

        if (line13.contains("Link")) {
            Matcher m = r.matcher(line13);
            int count = 0;
            int[] pageValues = new int[2];
            while (m.find())
                pageValues[count++] = Integer.parseInt(m.group(0)
                        .replace("=", "")
                        .replace("&p", ""));
            return pageValues;
        }
        return new int[0];
    }

    /**
     * Method to convert all issue file from txt to json.
     *
     * @param position      : file starting line.
     * @param directoryPath : path to the directory that contains all issue files.
     */
    private static void convertDataFile(int position, String directoryPath, String directoryName) {

        String base = directoryPath + directoryName;

        Set<String> files = Stream
                .of(Objects.requireNonNull(new File(base).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String file : files) {
            String fileName = file.substring(0, file.length() - 4);
            boolean isDeleted = FileUtility.copyFile(
                    new File(base + fileName + ".txt"),
                    new File(base + fileName + ".json"),
                    position);
            if (isDeleted)
                LOGGER.log(Level.INFO, () -> "Deleted " + file + " file");
        }
    }
}
