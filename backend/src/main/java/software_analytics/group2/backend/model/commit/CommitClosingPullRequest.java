package software_analytics.group2.backend.model.commit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import software_analytics.group2.backend.interfaces.Closing;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commits")
public class CommitClosingPullRequest extends Commit implements Closing {
	@Field(name = "pullRequest_c")
	private PullRequest pullRequest;


	@Override
	public String closingType() {
		return "pull request";
	}
}
