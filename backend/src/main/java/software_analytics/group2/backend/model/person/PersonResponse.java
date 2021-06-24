package software_analytics.group2.backend.model.person;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponse {
	private String name;
	private String email;
	private int expertise;

	public PersonResponse(PersonByEmail person, String projectName) {
		this.name = person.getName();
		this.email = person.getEmail();
		this.expertise = person.computeExpertise(projectName);
	}
}
