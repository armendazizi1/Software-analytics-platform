package software_analytics.group2.backend.model.repo.data.issue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import software_analytics.group2.backend.interfaces.Structure;
import software_analytics.group2.backend.model.Label;
import software_analytics.group2.backend.model.person.PersonByEmail;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "issues")
public class Issue implements Structure {

    @Id
    private long issueId;
    private String url;
    private String nodeId;
    private long number;
    private String title;
    private List<Label> labels;
    private String state;
    private boolean locked;
    private PersonByEmail assignee;
    private List<PersonByEmail> assignees;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private String body;
    private String projectName;

    public long getIssueId() {
        return issueId;
    }
}

