package software_analytics.group2.backend.model.matrix;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import software_analytics.group2.backend.model.file.FileDeveloperResponse;
import software_analytics.group2.backend.model.person.PersonResponse;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class MatrixResponse {
	List<FileDeveloperResponse> percentages;

	public MatrixResponse() {
		this.percentages = new ArrayList<>();
	}

	public void add(String file, PersonResponse developer, int percentage) {
		percentages.add(new FileDeveloperResponse(file, developer.getName(), percentage));
	}
}
