package software_analytics.group2.backend.model.commit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import software_analytics.group2.backend.interfaces.Closing;
import software_analytics.group2.backend.interfaces.Inducing;
import software_analytics.group2.backend.interfaces.Resolving;
import software_analytics.group2.backend.interfaces.Structure;
import software_analytics.group2.backend.model.CommitFilesDifference;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.metric.ComparisonMetric;
import software_analytics.group2.backend.model.metric.Metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "commits")
public class Commit implements Structure {

    @Id
    private String id;
    private String projectName;
    @DBRef
    private PersonByEmail author;
    private Date date;
    private String message;
    @DBRef
    private CommitFilesDifference difference;
    @DBRef
    private Metric metric;
    @DBRef
    private ComparisonMetric comparisonMetric;
    private int authorExperience;

    public Commit(Commit commit) {
        this.id = commit.getId();
        this.projectName = commit.getProjectName();
        this.author = commit.getAuthor();
        this.date = commit.getDate();
        this.message = commit.getMessage();
        this.difference = commit.getDifference();
        this.metric = commit.getMetric();
        this.comparisonMetric = commit.getComparisonMetric();
    }

    public Commit(CommitBuilder commitBuilder) {
        id = commitBuilder.getId();
        projectName = commitBuilder.getProjectName();
        author = commitBuilder.getAuthor();
        date = commitBuilder.getDate();
        message = commitBuilder.getMessage();
        difference = commitBuilder.getDifference();
        metric = commitBuilder.getMetric();
        comparisonMetric = commitBuilder.getComparisonMetric();

        if (id == null || projectName == null || author == null || date == null || message == null ||
            difference == null) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Method to add the label list for each commit in the analysed project.
     *
     * @return : list of commit labels.
     */
    public List<String> computeLabels() {
        List<String> labels = new ArrayList<>();
        // closingIssue resolvesBug, inducingBug
        if (this instanceof Closing && ((Closing) this).closingType().equals("issue"))
            labels.add("closingIssue");
        if (this instanceof Resolving && ((Resolving) this).resolvingType().equals("bug"))
            labels.add("resolvesBug");
        if (this instanceof Inducing && ((Inducing) this).inducingType().equals("bug"))
            labels.add("inducingBug");
        return labels;
    }

    @Override
    public String toString() {
        return "Commit{" +
               "id='" + id + '\'' +
               ", projectName='" + projectName + '\'' +
               ", author=" + author.toString() +
               ", date=" + date +
               ", message='" + message + '\'' +
               ", difference=" + difference.toString() +
               ", metric=" + metric +
               ", comparisonMetric=" + comparisonMetric +
               '}';
    }
}