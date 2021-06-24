package software_analytics.group2.backend.parser;

import software_analytics.group2.backend.DefectPrediction;
import software_analytics.group2.backend.builder.trainingset.TrainingSetBuilder;
import software_analytics.group2.backend.model.project.Project;
import software_analytics.group2.backend.model.project.ProjectBuilder;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;
import software_analytics.group2.backend.service.DatabaseService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectParser {

    private static final Logger LOGGER = Logger.getLogger(ProjectParser.class.getName());

    private final DatabaseService databaseService;
    private final IssueParser issueParser;
    private final PullRequestParser pullRequestParser;
    private final CommitParser commitParser;

    public ProjectParser(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.issueParser = new IssueParser(databaseService);
        this.pullRequestParser = new PullRequestParser(databaseService);
        this.commitParser = new CommitParser(databaseService);
    }

    /**
     * Method to parse a project.
     *
     * @param projectName   :   name of the project to analyze
     * @param directoryPath : path to the project to analyze
     * @return the Object that represents the project
     */
    public Project parseNewProject(String projectName, String directoryPath) {
        ProjectBuilder projectBuilder = new ProjectBuilder(projectName);

        List<Issue> issueList = issueParser.parseRepoData(projectName, directoryPath);
        List<PullRequest> pullRequestList = pullRequestParser.parseRepoData(projectName, directoryPath, projectBuilder);
        projectBuilder.issues(issueList);
        projectBuilder.pullRequests(pullRequestList);
        projectBuilder.closedIssues(issueParser.getClosedIssues(issueList));

        commitParser.parseCommits(projectName, directoryPath, projectBuilder);

        Project project = projectBuilder.build();


        TrainingSetBuilder trainingSetBuilder = new TrainingSetBuilder(directoryPath);
        trainingSetBuilder.createFile(project);


        DefectPrediction defectPrediction = new DefectPrediction(projectName);
        defectPrediction.evaluateClassifierAndPredict(directoryPath + "/training.arff", directoryPath + "/test.arff");
        databaseService.saveDefectPrediction(defectPrediction);
        project.setDefectPrediction(defectPrediction);
        databaseService.saveProject(project);
        LOGGER.log(Level.INFO, () -> "Parsing completed with " + project.getNumberOfCommits() + " commits and " +
                project.getNumberOfIssues() + " issues");
        return project;
    }

    /**
     * Method to parse an existing project for update.
     *
     * @param projectName:   name of the project to analyze
     * @param directoryPath: path to the project to analyze
     * @return the Object that represents the project
     */
    public Project updateProject(String projectName, String directoryPath) {
        Project project = databaseService.getProjectByName(projectName);

        TrainingSetBuilder trainingSetBuilder = new TrainingSetBuilder(directoryPath);
        trainingSetBuilder.createFile(project);

        issueParser.updateRepoData(directoryPath, project);
        pullRequestParser.updateRepoData(directoryPath, project);
        commitParser.updateCommits(directoryPath, project);

        DefectPrediction defectPrediction = databaseService.getDefectPredictionByName(projectName);
        defectPrediction.evaluateClassifierAndPredict(directoryPath + "/training.arff", directoryPath + "/test.arff");
        project.setDefectPrediction(defectPrediction);
        databaseService.saveDefectPrediction(defectPrediction);

        project.setDefectPrediction(defectPrediction);
        databaseService.saveProject(project);
        return project;
    }
}
