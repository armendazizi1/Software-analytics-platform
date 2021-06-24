package software_analytics.group2.backend.model.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software_analytics.group2.backend.model.Coupling;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.Quartet;
import software_analytics.group2.backend.model.Triplet;
import software_analytics.group2.backend.model.matrix.MatrixResponse;
import software_analytics.group2.backend.model.person.PersonIdentifiedByLogin;
import software_analytics.group2.backend.model.person.PersonResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private String projectId;
    private String name;
    private List<Triplet<String, Date, List<String>>> commits;
    private List<Quartet<Long, Long, String, String>> issues;
    private List<Pair<Long, Long>> closedIssues;
    private Coupling coupling;
    private Map<String, Set<String>> bugs;
    private Set<PersonResponse> people;
    private Set<PersonIdentifiedByLogin> peopleLogin;
    private MatrixResponse matrix;
    private  Pair<Double,Double> precRec;
    private List<Pair<String, Double>> predictedCommits;

    public ProjectResponse(Project project) {
        String projectName = project.getName();
        this.projectId = project.getProjectId();
        this.name = projectName;
        this.commits = project.getCommits().stream().map(commit -> new Triplet<>
                (commit.getId(), commit.getDate(), commit.computeLabels())).collect(Collectors.toList());
        this.issues = project.getIssues().stream().map(issue -> new Quartet<>
                (issue.getIssueId(), issue.getNumber(), issue.getTitle(), issue.getState())).collect(Collectors.toList());
        this.closedIssues = project.getClosedIssues();
        this.coupling = project.getCoupling();
        this.bugs = project.getBugs();
        this.people = project.getPeople().stream().map(person -> new PersonResponse(person, projectName))
                .collect(Collectors.toSet());
        this.peopleLogin=project.getPeopleLogin().stream().map(person -> new PersonIdentifiedByLogin(person, projectName))
                .collect(Collectors.toSet());
        this.matrix = project.getMatrixHandler().computeBestDeveloperForEachFile();

       this.precRec=new Pair<>(project.getDefectPrediction().getPrecision(),project.getDefectPrediction().getRecall());
       this.predictedCommits=project.getDefectPrediction().getPredictedCommits();


    }

    public int getNumberOfCommits() {
        return commits.size();
    }

    public int getNumberOfIssues() {
        return issues.size();
    }
}
