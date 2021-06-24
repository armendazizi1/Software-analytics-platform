package software_analytics.group2.backend.parser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software_analytics.group2.backend.model.Label;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.project.Project;
import software_analytics.group2.backend.model.project.ProjectBuilder;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequestAccepted;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequestRejected;
import software_analytics.group2.backend.service.DatabaseService;
import software_analytics.group2.backend.utility.FileUtility;
import software_analytics.group2.backend.utility.ListUtility;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

public class PullRequestParser extends DataParser {

    private static final Logger LOGGER = Logger.getLogger(PullRequestParser.class.getName());

    private final DatabaseService databaseService;
    private final String delimiter;

    public PullRequestParser(DatabaseService databaseService) {
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
    public List<PullRequest> parseRepoData(String projectName, String directoryPath, ProjectBuilder projectBuilder) {
        String path = directoryPath + "/pullRequest";
        Set<String> files = FileUtility.listFiles(path);
        List<PullRequest> pullRequestList = new ArrayList<>();

        for (String fileName : files) {
            File file = new File(path + delimiter + fileName);
            try {
                for (Object object : (JSONArray) new JSONParser().parse(new FileReader(file))) {
                    JSONObject data = (JSONObject) object;
                    addPullRequest(parsePullRequestData(data, projectName), pullRequestList);
                }
            } catch (IOException | ParseException e) {
                LOGGER.log(Level.SEVERE, "Error occurs during the parsing of the pull requests", e);
            }
        }

        List<PersonByEmail> people = calculateAcceptedAndReviewedPR(projectName, pullRequestList);
        projectBuilder.peopleLogin(new HashSet<>(people));
        return pullRequestList;
    }

    /**
     * Method that calculates accepted and reviewed pull request persontage for each person identified by login
     *
     * @param projectName     : name of project
     * @param pullRequestList : list of pull request
     * @return : list of person who owns pull request,identified by login
     */
    private List<PersonByEmail> calculateAcceptedAndReviewedPR(String projectName, List<PullRequest> pullRequestList) {
        Iterator<PullRequest> iterator = pullRequestList.iterator();
        while (iterator.hasNext()) {

            PullRequest pr = iterator.next();
            if (!pr.getState().equals("closed"))
                iterator.remove();
        }

        List<PersonByEmail> people = new ArrayList<>();
        Set<String> logins = new HashSet<>();

        pullRequestList.forEach(pr ->
        {
            String type = getTypeOfPullRequest(pr);
            PersonByEmail openedPerson = getPersonLogin(pr.getUser());
            logins.add(openedPerson.getLogin());
            openedPerson.addOpenedPR(String.valueOf(pr.getPullId()), projectName, type);
            databaseService.savePerson(openedPerson);


            if (pr.getAssignee() != null) {
                PersonByEmail reviewedPerson = getPersonLogin(pr.getAssignee());
                logins.add(reviewedPerson.getLogin());
                reviewedPerson.addReviewedPR(String.valueOf(pr.getPullId()), projectName, type);
                databaseService.savePerson(reviewedPerson);
            }

            if (!pr.getAssignees().isEmpty()) {
                String finalType = type;
                pr.getAssignees().forEach(a -> {
                    if (a.getLogin() != null) {
                        PersonByEmail reviewedPersonMore = getPersonLogin(a);
                        logins.add(reviewedPersonMore.getLogin());
                        reviewedPersonMore.addReviewedPR(String.valueOf(pr.getPullId()), projectName, finalType);
                        databaseService.savePerson(reviewedPersonMore);
                    }

                });
            }


        });

        if (!logins.isEmpty())
            for (String login : logins) {
                people.add(databaseService.getPersonByLogin(login));
            }

        return people;

    }

    private String getTypeOfPullRequest(PullRequest pullRequest) {
        String type = null;
        if (pullRequest instanceof PullRequestRejected)
            type = "rejected";
        else if (pullRequest instanceof PullRequestAccepted)
            type = "accepted";
        return type;
    }

    /**
     * Method that allows to verify whether there are new issues.
     *
     * @param directoryPath : path to the main project folder.
     * @param project       : project in which to
     */
    public void updateRepoData(String directoryPath, Project project) {

        String path = directoryPath + "/pullRequest";
        Set<String> files = FileUtility.listFiles(path);
        List<PullRequest> newPullRequestList = new ArrayList<>();
        updateFilesData(project, path, files, newPullRequestList);

        project.setPullRequests(ListUtility.verifyMergeList(newPullRequestList, project.getPullRequests()));


        for (PullRequest pr : newPullRequestList) {
            if (pr.getState().equals("closed")) {
                String type = getStatusOfPullRequest(pr);
                PersonByEmail author = getPersonLogin(pr.getUser());
                author.addOpenedPR(String.valueOf(pr.getPullId()), project.getName(), type);


                databaseService.savePerson(author);

                if (pr.getAssignee() != null && pr.getAssignee().getLogin() != null) {
                    PersonByEmail reviewedPerson = getPersonLogin(pr.getAssignee());
                    reviewedPerson.addReviewedPR(String.valueOf(pr.getPullId()), project.getName(), type);
                    databaseService.savePerson(reviewedPerson);

                }

                if (!pr.getAssignees().isEmpty()) {
                    pr.getAssignees().forEach(a -> {
                        if (a.getLogin() != null) {
                            PersonByEmail reviewedPersonMore = getPersonLogin(a);
                            reviewedPersonMore.addReviewedPR(String.valueOf(pr.getPullId()), project.getName(), type);
                            databaseService.savePerson(reviewedPersonMore);


                        }

                    });
                }

            }
        }


        Set<PersonByEmail> allPeople = getPeopleOfAllPr(project);
        List<PersonByEmail> unique = allPeople.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(PersonByEmail::getLogin))),
                        ArrayList::new));
        project.setPeopleLogin(new HashSet<>(unique));


    }

    private Set<PersonByEmail> getPeopleOfAllPr(Project project) {
        Set<PersonByEmail> all = new HashSet<>();
        for (PullRequest pr : project.getPullRequests()) {
            all.add(getPersonLogin(pr.getUser()));
            if (pr.getAssignee() != null)
                all.add(getPersonLogin(pr.getAssignee()));

            if (!pr.getAssignees().isEmpty()) {
                pr.getAssignees().forEach(a -> {
                    if (a.getLogin() != null) {
                        all.add(getPersonLogin((PersonByEmail) a));
                    }
                });
            }

        }
        return all;
    }

    /**
     * Method to get the status of pull request
     *
     * @param pullRequest: pull request to analyze
     * @return string representing the status of the pr
     */
    private String getStatusOfPullRequest(PullRequest pullRequest) {
        String status = "";
        if (pullRequest instanceof PullRequestRejected)
            status = "rejected";
        else if (pullRequest instanceof PullRequestAccepted)
            status = "accepted";
        return status;
    }

    /**
     * Method to update the pull request data of the files
     *
     * @param project:            project currently analyzed
     * @param path:               path of the project folder
     * @param files:              name of files to update
     * @param newPullRequestList: list of the new pull requests
     */
    private void updateFilesData(Project project, String path, Set<String> files, List<PullRequest> newPullRequestList) {
        for (String fileName : files) {
            File file = new File(path + delimiter + fileName);
            try {
                for (Object object : (JSONArray) new JSONParser().parse(new FileReader(file)))
                    updateData(object, project, newPullRequestList);
            } catch (IOException | ParseException e) {
                LOGGER.log(Level.SEVERE, "Error occurs during the updating of the pullrequests", e);
            }
        }
    }

    /**
     * Method to get the updated data.
     *
     * @param object          : JSON object.
     * @param project         : analysed project data.
     * @param pullRequestList : list of pull requests.
     */
    private void updateData(Object object, Project project, List<PullRequest> pullRequestList) {
        String projectName = project.getName();
        JSONObject data = (JSONObject) object;
        checkPullRequest(parsePullRequestData(data, projectName), project, pullRequestList);
    }

    /**
     * Method to check if a issue is new or has changed its state.
     *
     * @param pullRequest        : new parsed pullRequest
     * @param project            : analysed project
     * @param newPullRequestList : list to add the pullRequest if any.
     */
    private void checkPullRequest(PullRequest pullRequest, Project project, List<PullRequest> newPullRequestList) {
        PullRequest databasePullRequest = databaseService.getPullRequestByIdAndProjectName(pullRequest.getPullId(),
                project.getName());

        if (databasePullRequest == null) {
            addPullRequest(pullRequest, newPullRequestList);
        } else {
            changePullRequestState(pullRequest, project);
            databasePullRequest.setState(pullRequest.getState());
            databaseService.savePullRequest(pullRequest);
        }
    }

    /**
     * Method to verify it a pull request object has changed its state.
     *
     * @param pullRequest : pull request object to verify.
     * @param project     : analysed project data.
     */
    private void changePullRequestState(PullRequest pullRequest, Project project) {
        List<PullRequest> oldIssueList = project.getPullRequests();
        IntStream.range(0, oldIssueList.size())
                .filter(i -> (oldIssueList.get(i).getPullId() == pullRequest.getPullId()))
                .filter(i -> !(oldIssueList.get(i).getState().equals(pullRequest.getState())))
                .findFirst().ifPresent(i -> oldIssueList.set(i, pullRequest));
    }

    /**
     * Method to add the object to the given list.
     *
     * @param pullRequest     : object to add to the given list.
     * @param pullRequestList : list where add the new object.
     */
    private void addPullRequest(PullRequest pullRequest, List<PullRequest> pullRequestList) {
        pullRequestList.add(pullRequest);
        databaseService.savePullRequest(pullRequest);
    }

    /**
     * Method to create the right object type of the pull request.
     *
     * @param closedAt    : date of closing pull request.
     * @param mergedAt    : date of merging pull request.
     * @param pullRequest : pull request object.
     * @return : right pull request type based on the given values.
     */
    private PullRequest createPullRequestCorrectType(LocalDateTime closedAt, LocalDateTime mergedAt, PullRequest pullRequest) {
        if (closedAt != null && mergedAt != null)
            pullRequest = new PullRequestAccepted(pullRequest, closedAt, mergedAt);
        else if (closedAt != null)
            pullRequest = new PullRequestRejected(pullRequest, closedAt, null);
        return pullRequest;
    }

    /**
     * Method to parse an issue.
     *
     * @param pullRequest : a JSONObject representing an issue.
     * @param projectName : name of the project.
     * @return : issue object.
     */
    private PullRequest parsePullRequestData(JSONObject pullRequest, String projectName) {
        long id = (long) pullRequest.get("id");
        String url = (String) pullRequest.get("url");
        String nodeId = (String) pullRequest.get("node_id");
        long number = (long) pullRequest.get("number");
        String title = (String) pullRequest.get("title");
        List<Label> labels = parseLabels(pullRequest);
        String state = (String) pullRequest.get("state");
        boolean locked = (boolean) pullRequest.get("locked");
        PersonByEmail user = parseUser(pullRequest);
        PersonByEmail assignee = parseAssignee(pullRequest);
        List<PersonByEmail> assignees = parseAssignees(pullRequest);
        LocalDateTime createdAt = parseDate(pullRequest, "created_at");
        LocalDateTime updatedAt = parseDate(pullRequest, "updated_at");
        LocalDateTime closedAt = parseDate(pullRequest, "closed_at");
        LocalDateTime mergedAt = parseDate(pullRequest, "merged_at");
        String mergeCommitSha = (String) pullRequest.get("merge_commit_sha");
        String body = (String) pullRequest.get("body");

        PullRequest newPullRequest = new PullRequest(id, url, nodeId, number, title, labels, state, locked, user,
                assignee, assignees, createdAt, updatedAt, mergeCommitSha, body, projectName);
        return createPullRequestCorrectType(closedAt, mergedAt, newPullRequest);
    }


    /**
     * Method to extract the get person by login from db,or saves to db if not exists
     *
     * @param personIdent: object that holds the information of the person identified by login
     * @return Person:      object that represents the person identified by login
     */
    private PersonByEmail getPersonLogin(PersonByEmail personIdent) {
        String login = personIdent.getLogin();

        if (databaseService.getPersonByLogin(login) != null) {
            return databaseService.getPersonByLogin(login);
        } else {
            databaseService.savePerson(personIdent);
            return personIdent;
        }


    }


}


