package software_analytics.group2.backend.parser;

import software_analytics.group2.backend.model.commit.CommitBuilder;
import software_analytics.group2.backend.utility.FileUtility;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SZZ algorithm implementation
 */
public final class CommitInducingBugParser {

    private static CommitInducingBugParser instance = null;
    private static String path = null;

    private static final String PROJECTPATH = "./src/main/resources/projects/";

    private CommitInducingBugParser() {
    }

    /**
     * Method to get the CommitInducingBugParser instance.
     *
     * @return CommitInducingBugParser instance.
     */
    public static CommitInducingBugParser getInstance() {
        if (instance == null)
            instance = new CommitInducingBugParser();
        return instance;
    }

    /**
     * Method to get the list of commits inducing bugs.
     *
     * @param projectName : name of the project.
     * @param commit      : commit to check if it induces a bug or not.
     * @return : list of commits inducing bugs.
     * @throws IOException : exception handled.
     */
    public Set<String> getCommitInducingBugs(String projectName, CommitBuilder commit) throws IOException {
        if (checkBugFixingCommit(commit))
            return extractCommitFixingBug(commit.getId(), projectName, PROJECTPATH + projectName, commit.getDifference().getModified());
        return new HashSet<>();
    }

    /**
     * Method that compute the absolute path inside the analysed project.
     *
     * @param fileName     : to search within the analyzed project.
     * @param sourceFolder : file where to start the search.
     * @return : the absolute path inside the analysed project.
     */
    protected static String findFilePath(String fileName, final File sourceFolder) {
        for (final File file : Objects.requireNonNull(sourceFolder.listFiles()))
            if (file.isDirectory())
                findFilePath(fileName, file);
            else if (file.getName().equals(fileName))
                path = file.getPath();
        return path;
    }

    /**
     * Method to verify if a commit has resolved such bugs.
     *
     * @param commit : commit to verify.
     * @return : true in case the commit message contains the following keywords, otherwise false.
     */
    private boolean checkBugFixingCommit(CommitBuilder commit) {
        String commitTitle = commit.getMessage().toLowerCase();
        if (commitTitle.contains("fix") || commitTitle.contains("solve"))
            return commitTitle.contains("bug") || commitTitle.contains("issue") ||
                    commitTitle.contains("problem") || commitTitle.contains("error");
        return false;
    }

    /**
     * Method to get the file path of a given file.
     *
     * @param fileName    : name of the file.
     * @param projectPath : path to the analysed project.
     * @return : the file path inside the project.
     */
    private String getFilePath(String fileName, String projectPath) {
        if (fileName.contains("/")) {
            String[] fileNameSplit = fileName.split("/");
            return findFilePath(fileNameSplit[fileNameSplit.length - 1], new File(projectPath));
        }
        return findFilePath(fileName, new File(projectPath));
    }

    /**
     * Method to get the commits inducing bugs.
     *
     * @param commitId    : id of the commit.
     * @param projectName : name of the analysed project.
     * @param projectPath : path to the analysed project.
     * @param fileNames   : files changed by this given commit.
     * @return : set of commits inducing bugs.
     * @throws IOException : exception handled.
     */
    private Set<String> extractCommitFixingBug(String commitId, String projectName, String projectPath, List<String> fileNames) throws IOException {
        Set<String> bugInducingCommits = new HashSet<>();
        for (String fileName : fileNames)
            if (fileName.endsWith(".java")) {
                String absolutePath = getFilePath(fileName, projectPath);
                String relativePath = absolutePath.replace(projectPath + File.separator, "");
                List<Integer> commentLines = FileUtility.extractCommentLines(absolutePath);
                bugInducingCommits.addAll(executeGitCommands(commitId, projectName, relativePath, commentLines));
            }
        bugInducingCommits.remove(commitId.substring(0, 8));
        return bugInducingCommits;
    }

    /**
     * Method that performs the git diff and blame commands.
     *
     * @param commitId     : id of the commit.
     * @param projectName  name of the analysed project.
     * @param relativePath :  relative path to the analysed project.
     * @param commentLines : list of comment line.
     * @return : set of commits inducing bugs.
     * @throws IOException : exception handled.
     */
    private Set<String> executeGitCommands(String commitId, String projectName, String relativePath, List<Integer> commentLines) throws IOException {
        List<Integer> impactedLines = executeGitDiff(commitId, projectName, relativePath);
        removeCommentLines(impactedLines, commentLines);
        return executeGitBlame(commitId, projectName, relativePath, impactedLines);
    }

    /**
     * Method to get the starting line of a git diff block.
     *
     * @param line : line that contains the numbers of the changed lines.
     * @return : starting line of git diff block.
     */
    private Integer extractImpactedLinesNumber(String line) {
        int startingLine = 0;
        Pattern pattern = Pattern.compile("[+|-][0-9]+(,)[0-9]+");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String[] number = matcher.group(0).split(",");
            return Math.abs(Integer.parseInt(number[0]));
        }
        return startingLine;
    }

    /**
     * Method to execute terminal commands.
     *
     * @param command : command to be executed.
     * @param path    : where we want to execute the command.
     * @return : command output (inputStream).
     * @throws IOException : exception handled.
     */
    private InputStream executeCommand(String command, String path) throws IOException {
        Process process = Runtime.getRuntime().exec(command, null, new File(path));
        return process.getInputStream();
    }

    /**
     * Method to execute the git diff command and get the lines impacted by the git diff command.
     *
     * @param commitId : id of the commit analysed.
     * @param filePath : where we want to execute the git diff command.
     * @return : lines impacted by the git diff command.
     * @throws IOException : exception handled.
     */
    private List<Integer> executeGitDiff(String commitId, String projectName, String filePath) throws IOException {
        String command = "git diff " + commitId + "^:" + filePath + " " + commitId + ":" + filePath;
        return getDiffLines(executeCommand(command, PROJECTPATH + projectName));
    }

    /**
     * Method to get the lines impacted by the git diff command.
     *
     * @param inputStream : git diff output.
     * @return : lines impacted by the git diff command.
     * @throws IOException : exception handled.
     */
    private List<Integer> getDiffLines(InputStream inputStream) throws IOException {
        List<Integer> impactedLines = new ArrayList<>();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(inputStream));
        int cnt = 0;
        String line;
        while ((line = stdInput.readLine()) != null) {
            if (line.startsWith("@@"))
                cnt = extractImpactedLinesNumber(line) - 1;

            if (!line.startsWith("+ ")) {
                if (line.startsWith("- ")) {
                    impactedLines.add(cnt);
                }
                cnt++;
            }
        }
        return impactedLines;
    }

    /**
     * Method that removes the comment lines from the impacted lines.
     *
     * @param impactedLines : list of all impacted lines
     * @param commentLines  : list of all comment line.
     */
    private void removeCommentLines(List<Integer> impactedLines, List<Integer> commentLines) {
        impactedLines.removeIf(commentLines::contains);
    }

    /**
     * Method to execute the git blame command.
     *
     * @param commitId    : id of the analysed commit.
     * @param projectName : name of the analysed project.
     * @param filePath    : where we want to execute the git blame command.
     * @throws IOException : exception handled.
     */
    private Set<String> executeGitBlame(String commitId, String projectName, String filePath, List<Integer> impactedLines) throws IOException {
        String newCommitId = commitId + "^";
        String command = "./gitBlame.sh " + projectName + " " + newCommitId + " " + filePath;
        return getGitBlame(executeCommand(command, "./src/main/resources/script"), impactedLines);
    }

    /**
     * Method to extract the commit id impacted by the git blame command.
     *
     * @param inputStream   : git blame output.
     * @param impactedLines : number of lines to look at.
     * @return : set of commits inducing bugs.
     * @throws IOException : exception handled.
     */
    private Set<String> getGitBlameLines(InputStream inputStream, List<Integer> impactedLines) throws IOException {
        Set<String> bugInducingCommitId = new HashSet<>();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(inputStream));
        int cnt = 1;
        String line;
        while ((line = stdInput.readLine()) != null) {
            if (impactedLines.contains(cnt)) {
                bugInducingCommitId.add(line.substring(0, 8));
            }
            cnt++;
        }
        return bugInducingCommitId;
    }

    /**
     * Method to get execute the git blame command.
     *
     * @param inputStream   : git blame output.
     * @param impactedLines : list of impacted lines from git diff command.
     * @return : set of commits inducing bugs.
     * @throws IOException : exception handled.
     */
    private Set<String> getGitBlame(InputStream inputStream, List<Integer> impactedLines) throws IOException {
        return getGitBlameLines(inputStream, impactedLines);
    }
}
