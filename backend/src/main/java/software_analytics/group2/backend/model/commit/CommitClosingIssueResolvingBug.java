package software_analytics.group2.backend.model.commit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import software_analytics.group2.backend.interfaces.Closing;
import software_analytics.group2.backend.interfaces.Resolving;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commits")
public class CommitClosingIssueResolvingBug extends CommitResolvingBug implements Closing, Resolving{
	@Field(name = "issueID_cr")
	private Long issueID;
	@Field(name = "issueNumber_cr")
	private Long issueNumber;

	public CommitClosingIssueResolvingBug(CommitBuilder commitBuilder) {
		super(commitBuilder);
		this.issueID = commitBuilder.getIssue().getIssueId();
		this.issueNumber = commitBuilder.getIssue().getNumber();
	}

	/**
	 * @return the type of what it resolving
	 */
	@Override
	public String resolvingType() {
		return "bug";
	}


	/**
	 * @return the type of what it closes
	 */
	@Override
	public String closingType() {
		return "issue";
	}
}
