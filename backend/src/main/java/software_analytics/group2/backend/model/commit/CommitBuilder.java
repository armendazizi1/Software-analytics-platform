package software_analytics.group2.backend.model.commit;

import lombok.Getter;
import software_analytics.group2.backend.interfaces.Builder;
import software_analytics.group2.backend.model.CommitFilesDifference;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.model.metric.ComparisonMetric;
import software_analytics.group2.backend.model.metric.Metric;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;

import java.util.Date;
import java.util.Set;

@Getter
public class CommitBuilder implements Builder {

    // basic commit
    private final String projectName;
    private String id;
    private PersonByEmail author;
    private Date date;
    private String message;
    private CommitFilesDifference difference;
    private Metric metric;
    private ComparisonMetric comparisonMetric;
    // inheritance fields
    private Issue issue;
    private Set<String> inducingBugFixedBy;
    private Set<String> resolvingBugIntroducedBy;
    private PullRequest pullRequest;


    public CommitBuilder(String projectName) {
        this.projectName = projectName;
    }

    public CommitBuilder(Commit commit) {
        this.projectName = commit.getProjectName();
        this.id = commit.getId();
        this.author = commit.getAuthor();
        this.date = commit.getDate();
        this.message = commit.getMessage();
        this.difference = commit.getDifference();
        this.metric = commit.getMetric();
        // inherit fields
        if (commit instanceof CommitClosingIssue)
            this.issue = ((CommitClosingIssue) commit).getIssue();

        if (commit instanceof CommitInducingBug)
            this.resolvingBugIntroducedBy = ((CommitInducingBug) commit).getInducingBugFixedBy();
        else if (commit instanceof CommitInducingBugResolvingBug)
            this.resolvingBugIntroducedBy = ((CommitInducingBugResolvingBug) commit).getInducingBugFixedBy();
        else if (commit instanceof CommitClosingIssueInducingBug)
            this.resolvingBugIntroducedBy = ((CommitClosingIssueInducingBug) commit).getInducingBugFixedBy();
        else if (commit instanceof CommitClosingIssueInducingBugResolvingBug)
            this.resolvingBugIntroducedBy = ((CommitClosingIssueInducingBugResolvingBug) commit).getInducingBugFixedBy();

        if (commit instanceof CommitResolvingBug)
            this.resolvingBugIntroducedBy = ((CommitResolvingBug) commit).getResolvingBugIntroducedBy();
        else if (commit instanceof CommitClosingIssueInducingBugResolvingBug)
            this.resolvingBugIntroducedBy =
                    ((CommitClosingIssueInducingBugResolvingBug) commit).getResolvingBugIntroducedBy();
    }

    public CommitBuilder id(String id) {
        this.id = id;
        return this;
    }


    public CommitBuilder author(PersonByEmail author) {
        this.author = author;
        return this;
    }


    public CommitBuilder date(Date date) {
        this.date = date;
        return this;
    }


    public CommitBuilder message(String message) {
        this.message = message;
        return this;
    }

    public CommitBuilder difference(CommitFilesDifference difference) {
        this.difference = difference;
        return this;
    }

    public CommitBuilder metric(Metric metric) {
        this.metric = metric;
        return this;
    }

    public CommitBuilder comparisonMetric(ComparisonMetric comparisonMetric) {
        this.comparisonMetric = comparisonMetric;
        return this;
    }

    public CommitBuilder issue(Issue issue) {
        this.issue = issue;
        return this;
    }

    public CommitBuilder inducingBugFixedBy(Set<String> inducingBugFixedBy) {
        this.inducingBugFixedBy = inducingBugFixedBy;
        return this;
    }

    public CommitBuilder resolvingBugIntroducedBy(Set<String> resolvingBugIntroducedBy) {
        this.resolvingBugIntroducedBy = resolvingBugIntroducedBy;
        return this;
    }

    public CommitBuilder pullRequest(PullRequest pullRequest) {
        this.pullRequest = pullRequest;
        return this;
    }

    /**
     * @return new Commit object
     */
    public Commit build() {
        Commit result;
        if (issue != null) {
            if (inducingBugFixedBy != null) {
                if (resolvingBugIntroducedBy != null) {
                    result = new CommitClosingIssueInducingBugResolvingBug(this);
                } else {
                    result = new CommitClosingIssueInducingBug(this);
                }
            } else if (resolvingBugIntroducedBy != null) {
                result = new CommitClosingIssueResolvingBug(this);
            } else {
                result = new CommitClosingIssue(this);
            }
        } else if (inducingBugFixedBy != null) {
            if (resolvingBugIntroducedBy != null) {
                result = new CommitInducingBugResolvingBug(this);
            } else {
                result = new CommitInducingBug(this);
            }
        } else if (resolvingBugIntroducedBy != null) {
            result = new CommitResolvingBug(this);
        } else {
            result = new Commit(this);
        }
        return result;
    }
}
