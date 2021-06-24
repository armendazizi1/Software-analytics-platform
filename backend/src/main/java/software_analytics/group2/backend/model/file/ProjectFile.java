package software_analytics.group2.backend.model.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import software_analytics.group2.backend.model.commit.Commit;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "projectFiles")
public class ProjectFile {
	@Id String id;
	String project;
	String path;
	@DBRef
	List<Commit> commits;

	public ProjectFile(String project, String path, List<Commit> commits) {
		this.project = project;
		this.path = path;
		this.commits = commits;
	}

	public void addCommit(Commit commit)  {
		this.commits.add(commit);
	}

}
