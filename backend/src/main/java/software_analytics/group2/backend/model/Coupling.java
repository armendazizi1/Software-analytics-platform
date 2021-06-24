package software_analytics.group2.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "couplings")
public class Coupling {

    @Id
    private String id;
    private String projectName;
    private Map<Long, List<String>> commitToIssues;
    private Map<Long, List<String>> commitToPullRequest;

    public Coupling(String projectName, Map<Long, List<String>> commitToIssues, Map<Long, List<String>> commitToPullRequest) {
        this.projectName = projectName;
        this.commitToIssues = commitToIssues;
        this.commitToPullRequest = commitToPullRequest;
    }

    public List<Long> getIssuesNumbers() {
        return new ArrayList<>(commitToIssues.keySet());
    }

    public List<Long> getPullRequestsNumbers() {
        return new ArrayList<>(commitToPullRequest.keySet());
    }
}
