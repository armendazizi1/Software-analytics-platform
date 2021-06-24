package software_analytics.group2.backend.model.project;

import lombok.Getter;
import software_analytics.group2.backend.model.Coupling;
import software_analytics.group2.backend.model.matrix.MatrixHandler;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.commit.Commit;
import software_analytics.group2.backend.interfaces.Builder;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class ProjectBuilder implements Builder {

    private final String name;
    private Set<PersonByEmail> people;
    private Set<PersonByEmail> peopleLogin;
    private List<Commit> commits;
    private List<Issue> issues;
    private List<PullRequest> pullRequests;
    private List<Pair<Long, Long>> closedIssues;
    private Coupling coupling;
    private Map<String, Set<String>> bugs;
    private MatrixHandler matrixHandler;


    public ProjectBuilder(String name) {
        this.name = name;
    }


    public ProjectBuilder people(Set<PersonByEmail> people) {
        this.people = people;
        return this;
    }
    public ProjectBuilder peopleLogin(Set<PersonByEmail> peopleLogin) {
        this.peopleLogin = peopleLogin;
        return this;
    }

    public ProjectBuilder commits(List<Commit> commits) {
        this.commits = commits;
        return this;
    }

    public ProjectBuilder issues(List<Issue> issues) {
        this.issues = issues;
        return this;
    }

    public ProjectBuilder pullRequests(List<PullRequest> pullRequests) {
        this.pullRequests = pullRequests;
        return this;
    }

    public ProjectBuilder closedIssues(List<Pair<Long, Long>> closedIssues) {
        this.closedIssues = closedIssues;
        return this;
    }

    public ProjectBuilder coupling(Coupling coupling) {
        this.coupling = coupling;
        return this;
    }

    public ProjectBuilder bugs(Map<String, Set<String>> bugs) {
        this.bugs = bugs;
        return this;
    }

    public ProjectBuilder matrixHandler(MatrixHandler matrixHandler) {
        this.matrixHandler = matrixHandler;
        return this;
    }

    public Project build() {
        return new Project(this);
    }
}
