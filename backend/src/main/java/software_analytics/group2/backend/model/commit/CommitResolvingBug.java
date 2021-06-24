package software_analytics.group2.backend.model.commit;

import org.springframework.data.mongodb.core.mapping.Field;
import software_analytics.group2.backend.interfaces.Resolving;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commits")
public class CommitResolvingBug extends Commit implements Resolving {
	@Field(name = "resolvingBugIntroducedBy_r")
	private Set<String> resolvingBugIntroducedBy;

	public CommitResolvingBug(CommitBuilder commitBuilder) {
		super(commitBuilder);
		this.resolvingBugIntroducedBy = commitBuilder.getResolvingBugIntroducedBy();
	}

	/**
	 * @return the type of what it resolving
	 */
	@Override
	public String resolvingType() {
		return "bug";
	}

	@Override
	public String toString() {
		return "CommitResolvingBug{" +
			   "resolvingBugIntroducedBy=" + resolvingBugIntroducedBy +
			   "} " + super.toString();
	}
}
