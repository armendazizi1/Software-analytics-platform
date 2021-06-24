package software_analytics.group2.backend.model.commit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import software_analytics.group2.backend.interfaces.Closing;
import software_analytics.group2.backend.model.repo.data.issue.Issue;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "commits")
public class CommitClosingIssue extends Commit implements Closing {
    @DBRef
    @Field(name = "issue_c")
    private Issue issue;

    public CommitClosingIssue(CommitBuilder commitBuilder) {
        super(commitBuilder);
        this.issue = commitBuilder.getIssue();
    }


    @Override
    public String closingType() {
        return "issue";
    }
}
