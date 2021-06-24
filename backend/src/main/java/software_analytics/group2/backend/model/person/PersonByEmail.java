package software_analytics.group2.backend.model.person;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import software_analytics.group2.backend.interfaces.Inducing;
import software_analytics.group2.backend.interfaces.Person;
import software_analytics.group2.backend.model.commit.Commit;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "people")
public class PersonByEmail implements Person {

    @Id
    private String id;
    private String login;
    private String name;
    private String email;
    private Map<String, Set<String>> commitsMap;
    private Map<String, Set<String>> openedPR;
    private Map<String, Set<String>> reviewedPR;
    private Map<String, Integer> buggyCommits;


    public PersonByEmail(String login) {
        this.login = login;
        this.openedPR = new HashMap<>();
        this.reviewedPR = new HashMap<>();
        this.buggyCommits = new HashMap<>();
    }

    public PersonByEmail(String name, String email) {
        this.name = name;
        this.email = email;
        this.commitsMap = new HashMap<>();
        this.openedPR = new HashMap<>();
        this.reviewedPR = new HashMap<>();
        this.buggyCommits = new HashMap<>();
    }

    public PersonByEmail(String name, String email, Map<String, Set<String>> commitsMap) {
        this.name = name;
        this.email = email;
        this.commitsMap = commitsMap;
        this.buggyCommits = new HashMap<>();
    }

    /**
     * Method to save a commit of a project in the map
     *
     * @param commit:       commit object
     * @param projectName:  name of the project
     */
    public void addCommit(Commit commit, String projectName) {
        if (commitsMap.containsKey(projectName))
            commitsMap.get(projectName).add(commit.getId());
        else
            commitsMap.put(projectName, new HashSet<>(Arrays.asList(commit.getId())));
        if (commit instanceof Inducing && ((Inducing) commit).inducingType().equals("bug"))
            buggyCommits.put(projectName, buggyCommits.getOrDefault(projectName, 0));
    }

    /**
     * Method that compute experience of a developer
     */
    public int computeExpertise(String projectName) {
        return commitsMap.getOrDefault(projectName, new HashSet<>()).size();
    }

    /**
     * Method to save opened pull requests of a developer
     */
    public void addOpenedPR(String prId, String projectName, String prType) {
        String join = prType + " " + prId;
        if (openedPR.containsKey(projectName)) {
            openedPR.get(projectName).add(join);
        } else {
            openedPR.put(projectName, new HashSet<>(Arrays.asList(join)));
        }
    }

    /**
     * Method that compute percentage of accepted pull requests among the ones she opened
     */
    public double computeAcceptedPercentage(String projectName) {
        double totalSize =  openedPR.getOrDefault(projectName, new HashSet<>()).size();
        if (totalSize != 0) {
            AtomicInteger countAccepted = new AtomicInteger(0);
            openedPR.entrySet().forEach(entry ->
                entry.getValue().forEach(s -> {
                    if (s.contains("accepted"))
                        countAccepted.getAndIncrement();
                })
            );
            return (countAccepted.get() / totalSize)*100;

        } else
            return 0;

    }
    /**
     * Method to save reviewed pull requests of a developer
     */
    public void addReviewedPR(String prId, String projectName, String prType) {
        String join = prType + " " + prId;
        if (reviewedPR.containsKey(projectName)) {
            reviewedPR.get(projectName).add(join);
        } else {
            reviewedPR.put(projectName, new HashSet<>(Arrays.asList(join)));
        }
    }

    /**
     * Method that compute percentage of accepted pull requests among the ones she reviewed.
     */
    public double computeReviewedPercentage(String projectName) {
        double totalSize = reviewedPR.getOrDefault(projectName, new HashSet<>()).size();
        if (totalSize != 0) {
            AtomicInteger countAccepted = new AtomicInteger(0);
            reviewedPR.entrySet().forEach(entry ->
                entry.getValue().forEach(s -> {
                    if (s.contains("accepted"))
                        countAccepted.getAndIncrement();

                })
            );
            return (countAccepted.get() / totalSize)*100;

        } else
            return 0;
    }

    /**
     * Get the ratio of buggy commits over the total number of commits of the developer
     *
     * @param projectName:  name of the project
     * @return              ratio of buggy commits
     */
    public float computeRatioOfBuggyCommits(String projectName) {
        Set<String> commits = commitsMap.get(projectName);
        Integer buggy = buggyCommits.get(projectName);
        if (commits == null || commits.isEmpty() || buggy == null || buggy == 0)
            return 0;
        return (float) buggy/commits.size();
    }
}
