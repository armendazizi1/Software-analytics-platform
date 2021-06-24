package software_analytics.group2.backend.analysis;

import software_analytics.group2.backend.metric.MetricCalculator;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.commit.Commit;
import software_analytics.group2.backend.model.metric.ComparisonMetric;
import software_analytics.group2.backend.model.metric.Metric;
import software_analytics.group2.backend.model.project.Project;
import software_analytics.group2.backend.model.project.ProjectResponse;
import software_analytics.group2.backend.parser.ProjectParser;
import software_analytics.group2.backend.service.DatabaseService;
import software_analytics.group2.backend.utility.FileUtility;
import software_analytics.group2.backend.utility.StringUtility;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public final class RepositoryAnalysis {

    private static RepositoryAnalysis instance = null;
    private static final Logger LOGGER = Logger.getLogger(RepositoryAnalysis.class.getName());

    private final DatabaseService databaseService;
    private final String projectsPath;
    private final String delimiter;

    private RepositoryAnalysis(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.projectsPath = "./src/main/resources/projects/";
        this.delimiter = System.getProperty("file.separator");
    }

    public static RepositoryAnalysis getInstance(DatabaseService databaseService) {

        if (databaseService == null)
            return null;

        if (instance == null)
            instance = new RepositoryAnalysis(databaseService);
        return instance;
    }

    /**
     * Method that allow to analyse a new repository.
     *
     * @param repoUrl : url of the repository to analyse.
     * @return : analyzed project object.
     */
    public ProjectResponse repositoryAnalysis(String repoUrl) {
        String repoPath = StringUtility.checkRepoUrl(repoUrl);
        Pair<String, String> projectPath = StringUtility.projectName(repoPath, delimiter);
        String projectName = projectPath.getLeft() + "/" + projectPath.getRight();
        String directoryPath = projectsPath + projectName;

        Project project = databaseService.getProjectByName(projectName);
        if (project != null)
            return new ProjectResponse(project);

        FetchRepositoryData.cloneRepository(repoPath, directoryPath);
        FetchRepositoryData.fetchRepoStatistics(projectsPath, projectName, repoUrl);
        ProjectParser projectParser = new ProjectParser(databaseService);
        project = projectParser.parseNewProject(projectName, directoryPath);

        if (FileUtility.deleteDirectory(new File(projectsPath + projectPath.getLeft() + '/')))
            LOGGER.log(Level.INFO, "Deleted Project Folder");
        return new ProjectResponse(project);
    }

    /**
     * Method that allow to update an existing repository.
     *
     * @param repoUrl : url of the repository to analyse.
     * @return : updated project object.
     */
    public ProjectResponse updateRepository(String repoUrl) {
        LOGGER.log(Level.INFO, "Repo update function");
        String repoPath = StringUtility.checkRepoUrl(repoUrl);
        Pair<String, String> projectPath = StringUtility.projectName(repoPath, delimiter);
        String projectName = projectPath.getLeft() + "/" + projectPath.getRight();
        String directoryPath = projectsPath + projectName;
        FetchRepositoryData.cloneRepository(repoPath, directoryPath);
        FetchRepositoryData.fetchRepoStatistics(projectsPath, projectName, repoUrl);
        ProjectParser projectParser = new ProjectParser(databaseService);
        Project project = projectParser.updateProject(projectName, directoryPath);
        FileUtility.deleteDirectory(new File(projectsPath + projectPath.getLeft() + delimiter));
        return new ProjectResponse(project);
    }

    /**
     * Method to compute the metrics on a given commit id.
     *
     * @param commit      :    commit on which to calculate the project metrics.
     * @param projectName : name of the analysed project
     * @return : pair that contains the commit data and the related metrics objects.
     */
    public Pair<Metric, ComparisonMetric> computeMetric(Commit commit, String projectName) {
        LOGGER.log(Level.INFO, "Start computing metric function");
        String commitId = commit.getId();

        Pair<Metric, ComparisonMetric> commitMetrics;
        Metric metric = commit.getMetric();
        ComparisonMetric comparisonMetric = commit.getComparisonMetric();

        if (metric != null && comparisonMetric != null) {
            LOGGER.log(Level.INFO, "Comparison Metric from database");
            commitMetrics = new Pair<>(metric, comparisonMetric);
        } else {
            LOGGER.log(Level.INFO, "Comparison Metric from cloning repository");
            String repoPath = StringUtility.checkRepoUrl(projectName);
            String directoryPath = projectsPath + projectName;
            FetchRepositoryData.cloneRepository(repoPath, directoryPath);
            MetricCalculator metricCalculator = new MetricCalculator(databaseService);
            commitMetrics = metricCalculator.extractComparisonMetric(commitId, projectName);
            commit.setMetric(commitMetrics.getLeft());
            commit.setComparisonMetric(commitMetrics.getRight());
            databaseService.saveCommit(commit);
            FileUtility.deleteDirectory(new File(projectsPath + projectName.split("/")[0] + '/'));
        }
        LOGGER.log(Level.INFO, "End compute metric function");
        return commitMetrics;
    }


    /**
     * Method to obtain the previous commit of the given commitId.
     *
     * @param currentCommitId : current commit id
     * @param projectName     : analysed project
     * @return : the previous commit of the given commitId.
     */
    public String getPreviousCommitId(String currentCommitId, String projectName) {
        Project project = databaseService.getProjectByName(projectName);
        List<Commit> commits = project.getCommits();
        int size = project.getNumberOfCommits();
        int index = IntStream.range(0, size).filter(i -> commits.get(i).getId().equals(currentCommitId))
                .findFirst().orElse(-1) + 1;
        if (index < size && index != -1)
            return commits.get(index).getId();
        return currentCommitId;
    }
}
