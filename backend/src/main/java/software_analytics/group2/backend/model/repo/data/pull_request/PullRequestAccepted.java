package software_analytics.group2.backend.model.repo.data.pull_request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PullRequestAccepted extends PullRequest {

    private LocalDateTime closeAt;
    private LocalDateTime mergedAt;

    public PullRequestAccepted(PullRequest pr, LocalDateTime closeAt, LocalDateTime mergedAt) {
        super(pr.getPullId(), pr.getUrl(), pr.getNodeId(), pr.getNumber(), pr.getTitle(), pr.getLabels(),
                "closed", pr.isLocked(), pr.getUser(), pr.getAssignee(), pr.getAssignees(), pr.getCreateAt(),
                pr.getUpdateAt(), pr.getMergeCommitSha(), pr.getBody(), pr.getProjectName());
        this.closeAt = closeAt;
        this.mergedAt = mergedAt;
    }
}
