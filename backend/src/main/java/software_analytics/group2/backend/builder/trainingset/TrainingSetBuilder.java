package software_analytics.group2.backend.builder.trainingset;

import software_analytics.group2.backend.interfaces.Inducing;
import software_analytics.group2.backend.model.CommitFilesDifference;
import software_analytics.group2.backend.model.commit.Commit;
import software_analytics.group2.backend.model.project.Project;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainingSetBuilder {

    private static final Logger LOGGER = Logger.getLogger(TrainingSetBuilder.class.getName());

    private final String directoryPath;

    public TrainingSetBuilder(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Method to create the training and test file with the required metrics.
     *
     * @param project: name of the analysed project.
     */
    public void createFile(Project project) {
        List<Commit> commits = project.getCommits();
        Map<String, Integer> lastMonthCommits = getPreviousMonthCommits(commits);

        String projectName = project.getName();
        List<Commit> firstTenCommits = commits.subList(0, 10);
        List<Commit> lastCommits = commits.subList(10, commits.size());
        String path = directoryPath + System.getProperty("file.separator");
        FileWriter testFile = null;
        FileWriter trainFile = null;
        try {
            testFile = new FileWriter(path + "test.arff");
            addHeaderFile(testFile);
            trainFile = new FileWriter(path + "training.arff");
            addHeaderFile(trainFile);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error while trying to create the test and training set files");
        }

        assert trainFile != null;
        try {
            addFileElements(projectName, lastCommits, trainFile, lastMonthCommits);
            LOGGER.log(Level.INFO, "Created the training set file");
            addFileElements(projectName, firstTenCommits, testFile, lastMonthCommits);
            LOGGER.log(Level.INFO, "Created the test set file");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error while trying to add elements to the test and training set files");
        }
    }

    /**
     * @param projectName      : name of the analysed project.
     * @param commits          : list of project commits.
     * @param fileWriter       : file in which add the computed data.
     * @param lastMonthCommits map composed of the authors and
     *                         the number of commit in the las month.
     * @throws IOException : exception handled
     */
    private void addFileElements(String projectName, List<Commit> commits, FileWriter fileWriter,
                                 Map<String, Integer> lastMonthCommits) throws IOException {
        for (Commit commit : commits)
            fileWriter.write(computeLine(projectName, commit, lastMonthCommits));
        fileWriter.close();
    }

    /**
     * Method to get the previous moth date.
     *
     * @param today : date of today.
     * @return : the date of the previous moth starting from today.
     */
    private Date getPreviousMonthDate(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }

    /**
     * Method to get the ap composed of the authors and
     * the number of commit in the las month.
     *
     * @param commits : list of project commits.
     * @return : a map composed of the authors and
     * the number of commit in the las month.
     */
    private Map<String, Integer> getPreviousMonthCommits(List<Commit> commits) {
        var today = new Date();
        var previousMonth = getPreviousMonthDate(today);

        Map<String, Integer> lastMonthCommits = new HashMap<>();

        for (Commit commit : commits) {
            String author = commit.getAuthor().getEmail();
            if (previousMonth.before(commit.getDate()))
                break;

            if (lastMonthCommits.containsKey(author)) {
                int value = lastMonthCommits.get(author) + 1;
                lastMonthCommits.put(author, value);
            } else
                lastMonthCommits.putIfAbsent(author, 1);
        }
        return lastMonthCommits;
    }

    /**
     * Method to construct the line with the required metrics.
     *
     * @param projectName      : name of the analysed project.
     * @param commit           : commit analysed in that moment.
     * @param commitsPerformed : map composed of the authors and the number of commit in the las month.
     * @return
     */
    private String computeLine(String projectName, Commit commit, Map<String, Integer> commitsPerformed) {
        StringBuilder line = new StringBuilder();
        CommitFilesDifference diff = commit.getDifference();
        line.append(diff.getAllModifiedSize()).append(",");
        line.append(diff.getAdded().size()).append(",");
        line.append(diff.getDeleted().size()).append(",");
        line.append(diff.getAddedLines()).append(",");
        line.append(diff.getDeletedLines()).append(",");
        line.append(commit.getAuthor().getCommitsMap().get(projectName).size()).append(",");
        line.append(commit.getAuthorExperience()).append(",");
        line.append(commit.getAuthor().computeRatioOfBuggyCommits(projectName)).append(",");
        line.append(commitsPerformed.getOrDefault(commit.getAuthor().getEmail(), 0)).append(",");
        boolean isBuggy = commit instanceof Inducing && ((Inducing) commit).inducingType().equals("bug");
        line.append(isBuggy).append("\n");
        return line.toString();
    }

    /**
     * Method to add the header on the given file.
     *
     * @param file : file in which add the header.
     */
    private void addHeaderFile(FileWriter file) {
        String[] headerList = {"@relation JITDP", "@attribute modifiedFiles numeric",
                "@attribute addedFiles numeric", "@attribute deletedFiles numeric",
                "@attribute addedLines numeric", "@attribute deletedLines numeric",
                "@attribute developerPastCommits numeric",
                "@attribute avgPastCommitsOnImpactedFiles numeric",
                "@attribute percBuggyCommits numeric",
                "@attribute commitsPreviousWeek numeric",
                "@attribute buggy {true,false}", "@data"};

        Arrays.stream(headerList).forEach(h -> {
            try {
                file.write(h + "\n");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error while trying to add the header of the test and training set files");
            }
        });
    }
}
