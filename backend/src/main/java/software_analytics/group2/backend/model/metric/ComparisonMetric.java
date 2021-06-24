package software_analytics.group2.backend.model.metric;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comparisonMetrics")
public class ComparisonMetric {
    @Id
    private String metricId;
    private String currentCommit;
    private String previousCommit;
    private String repoName;
    private double couplingBetweenObjects;
    private double linesOfCode;
    private double weightMethodClass;
    private double lackCohesionMethods;

    public ComparisonMetric(String currentCommit, String previousCommit, String repoName, double couplingBetweenObjects,
                            double linesOfCode, double weightMethodClass, double lackCohesionMethods) {
        this.currentCommit = currentCommit;
        this.previousCommit = previousCommit;
        this.repoName = repoName;
        this.couplingBetweenObjects = couplingBetweenObjects;
        this.linesOfCode = linesOfCode;
        this.weightMethodClass = weightMethodClass;
        this.lackCohesionMethods = lackCohesionMethods;
    }
}
