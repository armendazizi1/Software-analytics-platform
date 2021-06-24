package software_analytics.group2.backend.parser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software_analytics.group2.backend.model.Label;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.project.Project;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.model.repo.data.issue.IssueClosed;
import software_analytics.group2.backend.service.DatabaseService;
import software_analytics.group2.backend.utility.FileUtility;
import software_analytics.group2.backend.utility.ListUtility;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class that mainly handles issue objects.
 */
public class IssueParser extends DataParser {

    private static final Logger LOGGER = Logger.getLogger(IssueParser.class.getName());

    private final DatabaseService databaseService;
    private final String delimiter;

    public IssueParser(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.delimiter = System.getProperty("file.separator");
    }

    /**
     * Method that allows to parse the issue of the analysed project.
     *
     * @param projectName   : name of the project.
     * @param directoryPath : path to the main project folder.
     * @return : map with the parsed issue.
     */
    public List<Issue> parseRepoData(String projectName, String directoryPath) {
        String path = directoryPath + "/issue";
        Set<String> files = FileUtility.listFiles(path);
        List<Issue> issueList = new ArrayList<>();

        for (String fileName : files) {
            File file = new File(path + delimiter + fileName);
            try {
                for (Object object : (JSONArray) new JSONParser().parse(new FileReader(file)))
                    fetchData(object, projectName, issueList);
            } catch (IOException | ParseException e) {
                LOGGER.log(Level.SEVERE, "Error occurs during the parsing of the issues", e);
            }
        }
        return issueList;
    }

    /**
     * Method to get pull requests and issues.
     *
     * @param object      : JSON object
     * @param projectName : analysed project
     * @param issueList   : list where add the issues.
     */
    private void fetchData(Object object, String projectName, List<Issue> issueList) {
        JSONObject data = (JSONObject) object;
        String htmlUrl = (String) data.get("html_url");
        if (!htmlUrl.contains("pull"))
            addIssue(parseIssueData(data, projectName), issueList);
    }

    /**
     * Method that allows to verify whether there are new issues.
     *
     * @param directoryPath : path to the main project folder.
     * @param project       : project in which to
     */
    public List<Pair<Long, Long>> updateRepoData(String directoryPath, Project project) {
        String path = directoryPath + "/issue";
        Set<String> files = FileUtility.listFiles(path);
        List<Issue> newIssueList = new ArrayList<>();
        List<Pair<Long, Long>> closedIssues = new ArrayList<>();

        for (String fileName : files) {
            File file = new File(path + delimiter + fileName);
            try {
                for (Object object : (JSONArray) new JSONParser().parse(new FileReader(file)))
                    updateData(object, project, newIssueList, closedIssues);
            } catch (IOException | ParseException e) {
                LOGGER.log(Level.SEVERE, "Error occurs during the updating of the issues", e);
            }
        }
        project.setIssues(ListUtility.verifyMergeList(newIssueList, project.getIssues()));
        return closedIssues;
    }

    /**
     * Method to get the updated data.
     *
     * @param object       : JSON object.
     * @param project      : analysed project data.
     * @param issues       : list of issues.
     * @param closedIssues : list where add the closed issues id.
     */
    private void updateData(Object object, Project project, List<Issue> issues, List<Pair<Long, Long>> closedIssues) {
        String projectName = project.getName();
        JSONObject data = (JSONObject) object;
        String htmlUrl = (String) data.get("html_url");
        if (!htmlUrl.contains("pull"))
            checkIssue(parseIssueData(data, projectName), project, issues, closedIssues);
    }

    /**
     * Method to check if a issue is new or has changed its state.
     *
     * @param issue        : new parsed issue
     * @param project      : analysed project
     * @param newIssueList : list to add the issue if any.
     */
    private void checkIssue(Issue issue, Project project, List<Issue> newIssueList, List<Pair<Long, Long>> closedIssues) {
        Issue databaseIssue = databaseService.getIssueByIdAndProjectName(issue.getIssueId(), project.getName());

        if (databaseIssue == null) {
            addIssue(issue, newIssueList);
            addClosedIssue(issue.getIssueId(), issue.getNumber(), issue.getState(), closedIssues);
        } else {
            changeIssueState(issue, project, closedIssues);
            databaseIssue.setState(issue.getState());
            databaseService.saveIssue(issue);
        }
    }

    /**
     * Method to the new closed issue to the given list.
     *
     * @param issueId      : id of the given issue.
     * @param issueNumber  : number of the given issue.
     * @param state        : state of the given issue.
     * @param closedIssues : list to add an issue if its state is equal to closed.
     */
    private void addClosedIssue(Long issueId, Long issueNumber, String state, List<Pair<Long, Long>> closedIssues) {
        if (state.equals("closed"))
            closedIssues.add(new Pair<>(issueId, issueNumber));
    }

    /**
     * Method to verify if the given issue has changed its status and set the new status.
     *
     * @param issue   : issue that changed its status
     * @param project : analysed project
     */
    private void changeIssueState(Issue issue, Project project, List<Pair<Long, Long>> closedIssues) {
        List<Issue> oldIssueList = project.getIssues();
        IntStream.range(0, oldIssueList.size())
                .filter(i -> (oldIssueList.get(i).getIssueId() == issue.getIssueId()))
                .filter(i -> !(oldIssueList.get(i).getState().equals(issue.getState())))
                .findFirst().ifPresent(i -> {
            Issue iss = oldIssueList.get(i);
            Long issueId = iss.getIssueId();
            Long issueNumber = iss.getNumber();
            String issueState = issue.getState();
            oldIssueList.set(i, issue);
            addClosedIssue(issueId, issueNumber, issueState, closedIssues);
        });
    }

    /**
     * Method that appends the issue object to the given list.
     *
     * @param issue     : issue object to add to the list.
     * @param issueList : list in which add the issue.
     */
    private void addIssue(Issue issue, List<Issue> issueList) {
        issueList.add(issue);
        databaseService.saveIssue(issue);
    }


    /**
     * Method to get the list of closed issue.
     *
     * @param issues: issues of the project
     * @return : list of pairs of issue id and number
     */
    public List<Pair<Long, Long>> getClosedIssues(List<Issue> issues) {
        // quartet : id, number, title, status (open or closed)
        return issues.stream()
                .filter(issue -> issue.getState().equals("closed"))
                .map(issue -> new Pair<>(issue.getIssueId(), issue.getNumber()))
                .collect(Collectors.toList());
    }

    /**
     * Method to parse an issue.
     *
     * @param issue       : a JSONObject representing an issue.
     * @param projectName : name of the project.
     * @return : issue object.
     */
    public Issue parseIssueData(JSONObject issue, String projectName) {
        long id = (long) issue.get("id");
        String url = (String) issue.get("url");
        String nodeId = (String) issue.get("node_id");
        long number = (long) issue.get("number");
        String title = (String) issue.get("title");
        List<Label> labels = parseLabels(issue);
        String state = (String) issue.get("state");
        boolean locked = (boolean) issue.get("locked");
        PersonByEmail assignee = parseAssignee(issue);
        List<PersonByEmail> assignees = parseAssignees(issue);
        LocalDateTime createdAt = parseDate(issue, "created_at");
        LocalDateTime updatedAt = parseDate(issue, "updated_at");
        LocalDateTime closedAt = parseDate(issue, "closed_at");
        String body = (String) issue.get("body");

        Issue newIssue = new Issue(id, url, nodeId, number, title, labels, state, locked, assignee, assignees, createdAt,
                updatedAt, body, projectName);
        if (closedAt != null)
            newIssue = new IssueClosed(newIssue, closedAt);
        return newIssue;
    }
}