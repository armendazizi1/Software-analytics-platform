package software_analytics.group2.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software_analytics.group2.backend.DefectPrediction;
import software_analytics.group2.backend.model.Coupling;
import software_analytics.group2.backend.model.matrix.MatrixHandler;
import software_analytics.group2.backend.model.file.ProjectFile;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.commit.Commit;
import software_analytics.group2.backend.model.CommitFilesDifference;
import software_analytics.group2.backend.model.metric.ComparisonMetric;
import software_analytics.group2.backend.model.metric.Metric;
import software_analytics.group2.backend.model.project.Project;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;
import software_analytics.group2.backend.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that allows to access and manipulate through CRUD operation the database objects.
 */
@Service
public class DatabaseService {

    @Autowired
    private PersonByEmailRepository personByEmailRepository;
    @Autowired
    private CommitRepository commitRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private MetricRepository metricRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ComparisonMetricRepository comparisonMetricRepository;
    @Autowired
    private CouplingRepository couplingRepository;
    @Autowired
    private FileDifferenceRepository fileDifferenceRepository;
    @Autowired
    private PullRequestRepository pullRequestRepository;
    @Autowired
    private MatrixHandlerRepository matrixHandlerRepository;
    @Autowired
    private DefectPredictionRepository defectPredictionRepository;


    /**
     * Method that allows to save a DefectPrediction object in the predictions collection.
     *
     * @param defectPrediction : DefectPrediction object to save
     */
    public void saveDefectPrediction(DefectPrediction defectPrediction){defectPredictionRepository.save(defectPrediction);}


    /**
     * Method to find a Defect Prediction object of a project inside the  collection through project name.
     *
     * @param projectName : name of the searched project.
     * @return : DefectPrediction object
     */
    public DefectPrediction getDefectPredictionByName(String projectName){
        return defectPredictionRepository.findDefectPredictionByProjectName(projectName);
    }
    /**
     * Method that allows to save a person object in the people collection.
     *
     * @param matrix : MatrixHandler object to save
     */
    public void saveMatrixHandler(MatrixHandler matrix) {
        matrixHandlerRepository.save(matrix);
    }

    /**
     * Method to find a matrix handler object of a project inside the  collection through project name.
     *
     * @param projectName : name of the searched project.
     * @return : project object
     */
    public MatrixHandler getMatrixHandlerByName(String projectName) {
        return matrixHandlerRepository.findMatrixHandlerByProjectName(projectName);
    }


    /**
     * Method that allows to save a person object in the people collection.
     *
     * @param person : person object to save
     */
    public void savePerson(PersonByEmail person) {
        personByEmailRepository.save(person);
    }

    /**
     * Method that allows to find a person in the people collection
     * through the given name and email.
     *
     * @param name   : person name
     * @param email: person email
     * @return : person object
     */
    public PersonByEmail findPerson(String name, String email) {
        List<PersonByEmail> personList = personByEmailRepository.findPeopleByNameAndEmail(name, email);
        return (personList.isEmpty()) ? null : personList.get(0);
    }

    public List<PersonByEmail> getAllPersonByLogin(){
        List<PersonByEmail> people=personByEmailRepository.findAll();
        List<PersonByEmail> login=new ArrayList<>();
        for(PersonByEmail person:people){
            if(person.getLogin()!=null)
                login.add(person);
        }
        return login;
    }
    /**
     * Method that allows to find a person in the people collection
     * through the given login.
     *
     * @param login   : person login name
     * @return : person object
     */
    public PersonByEmail getPersonByLogin(String login){
        return personByEmailRepository.findPersonByLogin(login);
    }
    /**
     * Method that allows to save a commit object in the commits collection.
     *
     * @param commit : commit object to save.
     */
    public void saveCommit(Commit commit) {
        commitRepository.save(commit);
    }

    /**
     * Method that delete a given commit in the commits collection.
     *
     * @param commit : commit object to delete.
     */
    public void deleteCommit(Commit commit) {
        commitRepository.delete(commit);
    }

    /**
     * Method that allows to save a file object in the projectFiles collection.
     *
     * @param file : commit file to save.
     */
    public void saveFile(ProjectFile file) {
        fileRepository.save(file);
    }

    /**
     * Method that delete a given file in the projectFiles collection.
     *
     * @param file : file object to delete.
     */
    public void deleteFile(ProjectFile file) {
        fileRepository.delete(file);
    }

    /**
     * Method that allows to save an issue object in the issues collection.
     *
     * @param issue : issue object to save.
     */
    public void saveIssue(Issue issue) {
        issueRepository.save(issue);
    }

    /**
     * Method to delete a given issue
     *
     * @param issue : issue to delete.
     */
    public void deleteIssue(Issue issue) {
        issueRepository.delete(issue);
    }

    /**
     * Method that allows to save an PullRequest object in the pullRequest collection.
     *
     * @param pullRequest : pullRequest object to save.
     */
    public void savePullRequest(PullRequest pullRequest) {
        pullRequestRepository.save(pullRequest);
    }

    /**
     * Method to delete a given PullRequest
     *
     * @param pullRequest : pullRequest to delete.
     */
    public void deletePullRequest(PullRequest pullRequest) {
        pullRequestRepository.delete(pullRequest);
    }

    /**
     * Method that allows to save a metric object in the metrics collection.
     *
     * @param metric : metric object to save.
     */
    public void saveMetric(Metric metric) {
        metricRepository.save(metric);
    }

    /**
     * Method to save coupling object within the database.
     *
     * @param coupling : coupling object to save.
     */
    public void saveCoupling(Coupling coupling) {
        couplingRepository.save(coupling);
    }


    /**
     * Method that allows to save a project object in the projects collection.
     *
     * @param project : project object to save.
     */
    public void saveProject(Project project) {
        projectRepository.save(project);
    }

    /**
     * Method to save the given commitFilesDifference object.
     *
     * @param commitFilesDifference : object to save within the database.
     */
    public void saveFileDifference(CommitFilesDifference commitFilesDifference) {
        fileDifferenceRepository.save(commitFilesDifference);
    }

    /**
     * Method that find a project object inside the projects collection through project name.
     *
     * @param projectName : name of the searched project.
     * @return : project object
     */
    public Project getProjectByName(String projectName) {
        return projectRepository.findByName(projectName);
    }

    /**
     * Method that finds all the project name within the projects collection.
     *
     * @return : list of all analysed project name.
     */
    public List<String> getAllRepositoryName() {
        return projectRepository.findAll().stream().map(Project::getName).collect(Collectors.toList());
    }

    /**
     * Method that finds an issue object through the given issueId and project name.
     *
     * @param issueId     : id of the searched issue.
     * @param projectName : name of the project.
     * @return : issue object.
     */
    public Issue getIssueByIdAndProjectName(Long issueId, String projectName) {
        return issueRepository.findIssueByIssueIdAndProjectName(issueId, projectName);
    }

    /**
     * Method that finds a pull request object through the given issueId and project name.
     *
     * @param pullRequestId : id of the searched pull request.
     * @param projectName   : name of the project.
     * @return : issue object.
     */
    public PullRequest getPullRequestByIdAndProjectName(Long pullRequestId, String projectName) {
        return pullRequestRepository.findPullRequestByPullIdAndProjectName(pullRequestId, projectName);
    }

    /**
     * Method that finds a commit object through the given commitId and project name.
     *
     * @param commitId    : id of the searched commit.
     * @param projectName : name of the project.
     * @return : commit object.
     */
    public Commit getCommitByIdAndProjectName(String commitId, String projectName) {
        return commitRepository.findCommitByIdAndProjectName(commitId, projectName);
    }

    /**
     * Method that delete the given project object inside the projects collection.
     *
     * @param project : project object to delete.
     */
    public void deleteProjectByName(Project project) {
        projectRepository.delete(project);
    }

    /**
     * Method to get all metric objects in the metrics collection.
     *
     * @return : list of all metric objects in the database.
     */
    public List<Metric> getAllMetric() {
        return metricRepository.findAll();
    }

    /**
     * Method that allows to save a comparisonMetric object in the comparisonMetrics collection.
     *
     * @param comparisonMetric : comparisonMetric object to save.
     */
    public void saveComparisonMetric(ComparisonMetric comparisonMetric) {
        comparisonMetricRepository.save(comparisonMetric);
    }

    /**
     * Method to get all comparisonMetric objects in the comparisonMetrics collection.
     *
     * @return : list of all comparisonMetric objects in the database.
     */
    public List<ComparisonMetric> getAllComparisonMetrics() {
        return comparisonMetricRepository.findAll();
    }

    /**
     * Method that finds a metric object through the given commitId and repository name.
     *
     * @param commitId : id of the commit.
     * @param repoName : name of the project.
     * @return : metric object.
     */
    public Metric getMetricByCommitId(String commitId, String repoName) {
        return metricRepository.findMetricByCommitAndRepoName(commitId, repoName);
    }

    /**
     * Method that finds a comparisonMetric object through the given commitId and repository name.
     *
     * @param commitId    : id of the commit.
     * @param projectName : name of the project.
     * @return : comparisonMetric object.
     */
    public ComparisonMetric getComparisonMetricByCommitId(String commitId, String projectName) {
        return comparisonMetricRepository.findComparisonMetricByCurrentCommitAndRepoName(commitId, projectName);
    }

    public PersonByEmail getPersonByEmail(String email){
        return personByEmailRepository.findPersonByEmail(email);
    }
}