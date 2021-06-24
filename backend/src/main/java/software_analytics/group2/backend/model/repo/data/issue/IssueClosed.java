package software_analytics.group2.backend.model.repo.data.issue;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
public class IssueClosed extends Issue {
    private LocalDateTime closeAt;

    public IssueClosed(Issue issue, LocalDateTime closeAt) {
        super(issue.getIssueId(), issue.getUrl(), issue.getNodeId(), issue.getNumber(), issue.getTitle(), issue.getLabels(),
                "closed", issue.isLocked(), issue.getAssignee(), issue.getAssignees(), issue.getCreateAt(),
                issue.getUpdateAt(), issue.getBody(), issue.getProjectName());
        this.closeAt = closeAt;
    }

}

