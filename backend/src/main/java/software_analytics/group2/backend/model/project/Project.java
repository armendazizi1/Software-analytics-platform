package software_analytics.group2.backend.model.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import software_analytics.group2.backend.DefectPrediction;
import software_analytics.group2.backend.model.Coupling;
import software_analytics.group2.backend.model.matrix.MatrixHandler;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.commit.Commit;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;
import software_analytics.group2.backend.service.DatabaseService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Project {

    @Id
    private String projectId;
    private String name;
    @DBRef
    private Set<PersonByEmail> people;
    @DBRef
    private Set<PersonByEmail> peopleLogin;
    @DBRef
    private List<Commit> commits;
    @DBRef
    private List<Issue> issues;
    @DBRef
    private List<PullRequest> pullRequests;
    private List<Pair<Long, Long>> closedIssues;
    @DBRef
    private Coupling coupling;
    private Map<String, Set<String>> bugs;
    @DBRef
    private MatrixHandler matrixHandler;
    @DBRef
    private DefectPrediction defectPrediction;



    public Project(ProjectBuilder builder) {
        name = builder.getName();
        people = builder.getPeople();
        peopleLogin=builder.getPeopleLogin();
        commits = builder.getCommits();
        issues = builder.getIssues();
        pullRequests = builder.getPullRequests();
        closedIssues = builder.getClosedIssues();
        coupling = builder.getCoupling();
        bugs = builder.getBugs();
        matrixHandler = builder.getMatrixHandler();

        if (people == null || commits == null || issues == null || closedIssues == null ||
            matrixHandler == null || coupling == null || bugs == null || peopleLogin==null||pullRequests==null) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get the current number of commits of the project
     *
     * @return number of commits of the project
     */
    public int getNumberOfCommits() {
        return commits.size();
    }

    /**
     * Get the current number of issues of the project
     *
     * @return number of issues of the project
     */
    public int getNumberOfIssues() {
        return issues.size();
    }

    /**
     * Updated the references of the data structures of this class
     *
     * @param databaseService:  service to talk to the database
     */
    public void saveProjectFieldsInDB(DatabaseService databaseService) {
        people.forEach(databaseService::savePerson);
        peopleLogin.forEach(databaseService::savePerson);
        issues.forEach(databaseService::saveIssue);
        pullRequests.forEach(databaseService::savePullRequest);
        databaseService.saveCoupling(coupling);
    }

}

