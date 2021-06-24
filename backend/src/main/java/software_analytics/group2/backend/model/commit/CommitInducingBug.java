package software_analytics.group2.backend.model.commit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import software_analytics.group2.backend.interfaces.Inducing;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commits")
public class CommitInducingBug extends Commit implements Inducing {
	@Field(name = "inducingBugFixedBy_i")
	private Set<String> inducingBugFixedBy;

	public CommitInducingBug(CommitBuilder commitBuilder) {
		super(commitBuilder);
		this.inducingBugFixedBy = commitBuilder.getInducingBugFixedBy();
	}

	/**
	 * @return the type of what it induces
	 */
	@Override
	public String inducingType() {
		return "bug";
	}
}
