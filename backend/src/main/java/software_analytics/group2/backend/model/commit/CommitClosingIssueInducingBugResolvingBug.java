package software_analytics.group2.backend.model.commit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import software_analytics.group2.backend.interfaces.Closing;
import software_analytics.group2.backend.interfaces.Inducing;
import software_analytics.group2.backend.interfaces.Resolving;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "commits")
public class CommitClosingIssueInducingBugResolvingBug extends CommitClosingIssue implements Closing, Inducing, Resolving {
	@Field(name = "inducingBugFixedBy_cir")
	private Set<String> inducingBugFixedBy;
	@Field(name = "resolvingBugIntroducedBy_r")
	private Set<String> resolvingBugIntroducedBy;


	public CommitClosingIssueInducingBugResolvingBug(CommitBuilder commitBuilder) {
		super(commitBuilder);
		this.inducingBugFixedBy = commitBuilder.getInducingBugFixedBy();
		this.resolvingBugIntroducedBy = commitBuilder.getResolvingBugIntroducedBy();
	}

	/**
	 * @return the type of what it induces
	 */
	@Override
	public String inducingType() {
		return "bug";
	}

	/**
	 * @return the type of what it resolving
	 */
	@Override
	public String resolvingType() {
		return "bug";
	}
}
