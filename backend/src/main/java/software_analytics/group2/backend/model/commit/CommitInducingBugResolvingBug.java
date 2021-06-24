package software_analytics.group2.backend.model.commit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import software_analytics.group2.backend.interfaces.Inducing;
import software_analytics.group2.backend.interfaces.Resolving;


import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commits")
public class CommitInducingBugResolvingBug extends CommitResolvingBug implements Inducing, Resolving {
	@Field(name = "inducingBugFixedBy_ir")
	private Set<String> inducingBugFixedBy;

	public CommitInducingBugResolvingBug(CommitBuilder commitBuilder) {
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

	/**
	 * @return the type of what it resolving
	 */
	@Override
	public String resolvingType() {
		return "bug";
	}

}
