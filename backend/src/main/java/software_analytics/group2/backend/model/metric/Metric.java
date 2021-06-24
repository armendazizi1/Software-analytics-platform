package software_analytics.group2.backend.model.metric;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "metrics")
public class Metric {

    @Id
    private String metricId;
    private String commit;
    private String repoName;
    private double couplingBetweenObjects;
    private double linesOfCode;
    private double weightMethodClass;
    private double lackCohesionMethods;

    public Metric(String commit, String repoName, double couplingBetweenObjects, double linesOfCode, double weightMethodClass, double lackCohesionMethods) {
        this.commit = commit;
        this.repoName = repoName;
        this.couplingBetweenObjects = couplingBetweenObjects;
        this.linesOfCode = linesOfCode;
        this.weightMethodClass = weightMethodClass;
        this.lackCohesionMethods = lackCohesionMethods;
    }

    public Metric(String metricId, String commit, String repoName, double couplingBetweenObjects, double linesOfCode, double weightMethodClass, double lackCohesionMethods) {
        this.metricId = metricId;
        this.commit = commit;
        this.repoName = repoName;
        this.couplingBetweenObjects = couplingBetweenObjects;
        this.linesOfCode = linesOfCode;
        this.weightMethodClass = weightMethodClass;
        this.lackCohesionMethods = lackCohesionMethods;
    }

    public Metric(String repoName, double cob, double loc, double wmc, double lcom) {
        this.repoName = repoName;
        this.couplingBetweenObjects = cob;
        this.linesOfCode = loc;
        this.weightMethodClass = wmc;
        this.lackCohesionMethods = lcom;
    }
}

