package software_analytics.group2.backend.model.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FileDeveloperResponse {
	String file;
	String developer;
	int percentage;
}
