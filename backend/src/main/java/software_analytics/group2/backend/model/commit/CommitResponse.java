package software_analytics.group2.backend.model.commit;

import lombok.Getter;
import lombok.Setter;
import software_analytics.group2.backend.model.CommitFilesDifference;
import software_analytics.group2.backend.model.person.PersonResponse;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class CommitResponse {

	private String id;
	private String projectName;
	private PersonResponse author;
	private Date date;
	private String message;
	private List<String> labels;
	private CommitFilesDifference difference;

	private Issue issue;
	private Set<String> inducingBugFixedBy;
	private Set<String> resolvingBugIntroducedBy;
	private PullRequest pullRequest;

	public CommitResponse(Commit commit) {
		this.id = commit.getId();
		this.projectName = commit.getProjectName();
		this.author = new PersonResponse(commit.getAuthor(), projectName);
		this.date = commit.getDate();
		this.message = commit.getMessage();
		this.labels = commit.computeLabels();
		this.difference = commit.getDifference();
		if (commit instanceof CommitClosingIssue)
			this.issue = ((CommitClosingIssue) commit).getIssue();

		if (commit instanceof CommitInducingBug)
			this.resolvingBugIntroducedBy = ((CommitInducingBug) commit).getInducingBugFixedBy();
		else if (commit instanceof CommitClosingIssueInducingBug)
			this.resolvingBugIntroducedBy = ((CommitClosingIssueInducingBug) commit).getInducingBugFixedBy();
		else if (commit instanceof CommitClosingIssueInducingBugResolvingBug)
			this.resolvingBugIntroducedBy = ((CommitClosingIssueInducingBugResolvingBug) commit).getInducingBugFixedBy();
		else if (commit instanceof CommitInducingBugResolvingBug)
			this.resolvingBugIntroducedBy = ((CommitInducingBugResolvingBug) commit).getInducingBugFixedBy();

		if (commit instanceof CommitClosingIssueInducingBugResolvingBug)
			this.resolvingBugIntroducedBy =
					((CommitClosingIssueInducingBugResolvingBug) commit).getResolvingBugIntroducedBy();
		else if (commit instanceof CommitResolvingBug)
			this.resolvingBugIntroducedBy = ((CommitResolvingBug) commit).getResolvingBugIntroducedBy();
	}
}
