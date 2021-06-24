package software_analytics.group2.backend;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software_analytics.group2.backend.analysis.RepositoryAnalysis;
import software_analytics.group2.backend.metric.MetricCalculator;
import software_analytics.group2.backend.model.*;
import software_analytics.group2.backend.model.commit.*;
import software_analytics.group2.backend.model.file.DiffFileRename;
import software_analytics.group2.backend.model.metric.ComparisonMetric;
import software_analytics.group2.backend.model.metric.Metric;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.person.PersonIdentifiedByLogin;
import software_analytics.group2.backend.model.person.PersonResponse;
import software_analytics.group2.backend.model.project.ProjectResponse;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.parser.IssueParser;
import software_analytics.group2.backend.service.DatabaseService;
import software_analytics.group2.backend.utility.FileUtility;
import software_analytics.group2.backend.utility.StringUtility;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BackendApplicationTests {
    @Autowired
    private DatabaseService databaseService;


    @Test
    @Order(1)
    void repositoryAnalysisTest() {
        RepositoryAnalysis repositoryAnalysis = RepositoryAnalysis.getInstance(databaseService);
        ProjectResponse project = repositoryAnalysis.repositoryAnalysis("https://github.com/ManbangGroup/Phantom.git");

        assertTrue(project.getMatrix().getPercentages().size() > 10);
        assertEquals(project.getMatrix().getPercentages().get(4).getDeveloper(), "shaobin0604");
        assertTrue(project.getMatrix().getPercentages().get(4).getPercentage() > 50);


        assertTrue(project.getPeople().size() > 1);
        assertTrue(project.getPeopleLogin().size() > 1);

        assertTrue(project.getPeopleLogin().stream().filter(p -> p.getLogin().equals("JianLin-Shen")).anyMatch(p -> ((PersonIdentifiedByLogin) p).getOpenedPRPercent() >= 50));
        assertTrue(project.getPeopleLogin().stream().filter(p -> p.getLogin().equals("shaobin0604")).anyMatch(p -> ((PersonIdentifiedByLogin) p).getReviewedPRPercent() >= 50));
        assertTrue(project.getPeople().stream().filter(p -> p.getName().equals("shaobin0604")).anyMatch(p -> ((PersonResponse) p).getExpertise() >= 34));


        assertEquals("ManbangGroup/Phantom", project.getName());

        assertTrue(greaterThan1(project.getNumberOfCommits()));
        assertTrue(greaterThan1(project.getNumberOfIssues()));


        List<Triplet<String, Date, List<String>>> dummyCommits = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("EE MMM dd HH:mm:ss zzzz yyyy", Locale.US);
        Date date = null;
        try {
            date = formatter.parse("Fri Jul 19 08:21:11 CEST 2019");
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        Triplet<String, Date, List<String>> tri = new Triplet<>("23c12ea9080dcd32668496fcb09495914ceb908e", date, new ArrayList<>());
        dummyCommits.add(tri);

        List<Quartet<Long, Long, String, String>> dummyIssues = new ArrayList<>();
        dummyIssues.add(new Quartet<>(552106113L, 26L, "androidx 兼容性问题", "open"));
        List<Pair<Long, Long>> dummyClosedIssues = new ArrayList<>();
        dummyClosedIssues.add(new Pair<>(535457397L, 25L));


        Coupling dummyCoupling = new Coupling();


        ProjectResponse dummy = new ProjectResponse("id", "ManbangGroup/Phantom", dummyCommits, dummyIssues, dummyClosedIssues, dummyCoupling, null, null, null, null, null, null);
        assertEquals(dummy.getName(), project.getName());
        assertEquals(dummy.getCommits().get(0).getFirst(), project.getCommits().get(0).getFirst());
        assertEquals(dummy.getCommits().get(0).getSecond(), project.getCommits().get(0).getSecond());
        assertEquals(dummy.getIssues().get(0).getFirst(), project.getIssues().get(0).getFirst());
        assertEquals(dummy.getIssues().get(0).getSecond(), project.getIssues().get(0).getSecond());
        assertEquals(dummy.getIssues().get(0).getThird(), project.getIssues().get(0).getThird());
        assertEquals(dummy.getIssues().get(0).getFourth(), project.getIssues().get(0).getFourth());
        assertEquals(dummy.getClosedIssues().get(0).getLeft(), project.getClosedIssues().get(0).getLeft());
        assertEquals(dummy.getClosedIssues().get(0).getRight(), project.getClosedIssues().get(0).getRight());

        assertTrue(greaterThan1(project.getCoupling().getPullRequestsNumbers().size()));
        assertTrue(greaterThan1(project.getCoupling().getIssuesNumbers().size()));


        //szz tests
        assertTrue(project.getCommits().stream().filter(c -> c.getFirst().equals("e073b52d895f6df0550b06525496b1e65c374c1c"))
                .anyMatch(s -> s.getThird().contains("inducingBug")));
        assertTrue(project.getCommits().stream().filter(c -> c.getFirst().equals("1ec44c7989e1101b2ffae744f72134ac3a3ba3be"))
                .anyMatch(s -> s.getThird().contains("resolvesBug")));


    }

    @Test
    @Order(2)
    void commitTypeTest() {
        Commit inducing = databaseService.getCommitByIdAndProjectName("e073b52d895f6df0550b06525496b1e65c374c1c", "ManbangGroup/Phantom");
        assertTrue(inducing instanceof CommitInducingBug);


        Commit resolving = databaseService.getCommitByIdAndProjectName("1ec44c7989e1101b2ffae744f72134ac3a3ba3be", "ManbangGroup/Phantom");
        assertTrue(resolving instanceof CommitResolvingBug);
        assertTrue(resolving.getMessage().contains("fix"));
    }

    @Test
    @Order(3)
    void checkReposInDBTest() {
        List<String> expected = new ArrayList<>();
        expected.add("ManbangGroup/Phantom");
        List<String> actual = databaseService.getAllRepositoryName();
        assertTrue(expected.size() <= actual.size());
        assertTrue(actual.contains(expected.get(0)));
    }

    @Test
    @Order(4)
    void updateRepoTest() {
        RepositoryAnalysis repositoryAnalysis = RepositoryAnalysis.getInstance(databaseService);
        ProjectResponse projectUpdated = repositoryAnalysis.updateRepository("https://github.com/ManbangGroup/Phantom.git");
        assertTrue(greaterThan1(projectUpdated.getNumberOfCommits()));
        assertTrue(greaterThan1(projectUpdated.getNumberOfIssues()));
    }

    public boolean greaterThan1(int size) {
        return size >= 1;
    }

    @Test
    void computeMetricTest() {
        RepositoryAnalysis repositoryAnalysis = RepositoryAnalysis.getInstance(databaseService);
        Commit commit = databaseService.getCommitByIdAndProjectName("1ec44c7989e1101b2ffae744f72134ac3a3ba3be", "ManbangGroup/Phantom");

        Pair<Metric, ComparisonMetric> result = repositoryAnalysis.computeMetric(commit, "ManbangGroup/Phantom");
        assertEquals(3, Math.round(result.getLeft().getCouplingBetweenObjects()));
        assertEquals(6, Math.round(result.getLeft().getLinesOfCode()));
        assertEquals(16, Math.round(result.getLeft().getWeightMethodClass()));
        assertEquals(29, Math.round(result.getLeft().getLackCohesionMethods()));
        assertEquals("ManbangGroup/Phantom", result.getLeft().getRepoName());

        assertEquals(0.0, result.getRight().getCouplingBetweenObjects());
        assertEquals(0.0, result.getRight().getLinesOfCode());
        assertEquals(0.0, result.getRight().getWeightMethodClass());
        assertEquals(-0.0, result.getRight().getLackCohesionMethods());
        assertEquals("ManbangGroup/Phantom", result.getRight().getRepoName());

    }

    @Test
    void parseIssueTest() {
        IssueParser issueParser = new IssueParser(databaseService);
        String stringToParse = "{\"assignees\":[],\"created_at\":\"2010-12-26T16:16:31Z\",\"title\":\"mavenize the project\",\"body\":\"Currently maven is the most popular project build system.\\nBy mavenizing the project, it'll be much easier to maintain, do continuous integration and more.\\n\",\"labels_url\":\"https:\\/\\/api.github.com\\/repos\\/stleary\\/JSON-java\\/issues\\/2\\/labels{\\/name}\",\"author_association\":\"NONE\",\"number\":2,\"updated_at\":\"2015-12-14T14:52:55Z\",\"performed_via_github_app\":null,\"comments_url\":\"https:\\/\\/api.github.com\\/repos\\/stleary\\/JSON-java\\/issues\\/2\\/comments\",\"active_lock_reason\":null,\"repository_url\":\"https:\\/\\/api.github.com\\/repos\\/stleary\\/JSON-java\",\"id\":492824,\"state\":\"closed\",\"locked\":false,\"comments\":4,\"closed_at\":\"2010-12-28T11:58:43Z\",\"url\":\"https:\\/\\/api.github.com\\/repos\\/stleary\\/JSON-java\\/issues\\/2\",\"labels\":[],\"milestone\":null,\"events_url\":\"https:\\/\\/api.github.com\\/repos\\/stleary\\/JSON-java\\/issues\\/2\\/events\",\"html_url\":\"https:\\/\\/github.com\\/stleary\\/JSON-java\\/issues\\/2\",\"assignee\":null,\"user\":{\"gists_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/gists{\\/gist_id}\",\"repos_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/repos\",\"following_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/following{\\/other_user}\",\"starred_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/starred{\\/owner}{\\/repo}\",\"login\":\"yusuke\",\"followers_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/followers\",\"type\":\"User\",\"url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\",\"subscriptions_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/subscriptions\",\"received_events_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/received_events\",\"avatar_url\":\"https:\\/\\/avatars2.githubusercontent.com\\/u\\/74894?v=4\",\"events_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/events{\\/privacy}\",\"html_url\":\"https:\\/\\/github.com\\/yusuke\",\"site_admin\":false,\"id\":74894,\"gravatar_id\":\"\",\"node_id\":\"MDQ6VXNlcjc0ODk0\",\"organizations_url\":\"https:\\/\\/api.github.com\\/users\\/yusuke\\/orgs\"},\"node_id\":\"MDU6SXNzdWU0OTI4MjQ=\"}";

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(stringToParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert json != null;
        Issue issue = (Issue) issueParser.parseIssueData(json, "ManbangGroup/Phantom");
        assertEquals("mavenize the project", issue.getTitle());
        assertEquals("ManbangGroup/Phantom", issue.getProjectName());
        assertEquals("closed", issue.getState());
        assertEquals(2, issue.getNumber());


    }

    @Test
    void checkRepoUrlTest() {
        assertEquals("https://github.com/ManbangGroup/Phantom.git", StringUtility.checkRepoUrl("ManbangGroup/Phantom"));
    }

    @Test
    void compareMetricsTest() {
        MetricCalculator metricCalculator = new MetricCalculator(databaseService);
        Metric previous = new Metric("id1", "p1", 4, 5, 6, 1.5);
        Metric current = new Metric("id2", "p1", 4.2, 6, 5.6, 3);
        Quartet<Double, Double, Double, Double> mc = null;
        try {
            mc = metricCalculator.compareMetrics(current, previous);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        assert mc != null;
        assertEquals(4.87804878048781, mc.getFirst());
        assertEquals(18.181818181818183, mc.getSecond());
        assertEquals(-6.896551724137938, mc.getThird());
        assertEquals(-66.66666666666666, mc.getFourth());

    }

    @Test
    void testModels() {
        Label label = new Label("name", "red", "fixed");
        assertEquals("name", label.getName());
        assertEquals("red", label.getColor());
        assertEquals("fixed", label.getDescription());
        Metric metric = new Metric("repoName", 1, 2, 3, 4);
        assertEquals("repoName", metric.getRepoName());
        Triplet<Integer, String, String> triplet = new Triplet<>(1, "commit", "link");
        assertEquals(Integer.valueOf(1), triplet.getFirst());

        CommitClosingIssue commitClosingIssue = new CommitClosingIssue();
        assertEquals("issue", commitClosingIssue.closingType());

        CommitClosingPullRequest commitClosingPullRequest = new CommitClosingPullRequest();
        assertEquals("pull request", commitClosingPullRequest.closingType());


    }


    @Test
    void testCommit() {
        PersonByEmail person = new PersonByEmail("a", "b");

        Date date = new Date();
        List<String> added = Arrays.asList("foo", "bar");
        List<String> modified = Arrays.asList("foo", "bar");
        List<String> removed = Arrays.asList("foo", "bar");

        List<DiffFileRename> renamed = new ArrayList<>();
        renamed.add(new DiffFileRename());

        Commit commit = new Commit("0", "projectName", person, date, "message",
                new CommitFilesDifference(added, modified, removed, renamed), null, null, 0);

        assertEquals(commit.getAuthor().getName(), person.getName());
        assertEquals(commit.getAuthor().getEmail(), person.getEmail());
        assertEquals(commit.getAuthor().getId(), person.getId());
        assertEquals(commit.getDate(), date);
        assertEquals(commit.getDifference().getAdded(), added);
        assertEquals(commit.getDifference().getModified(), modified);
        assertEquals(commit.getDifference().getDeleted(), removed);
        assertEquals(commit.getDifference().getRenamed(), renamed);
    }

    @Test
    void extractCommentLinesTest() {
        ArrayList<Integer> actual = (ArrayList<Integer>) FileUtility.extractCommentLines("./src/test/java/software_analytics/group2/backend/HelloWorld.java");
        ArrayList<Integer> expected = new ArrayList<>(Arrays.asList(4, 5, 10, 11, 12, 13, 14, 15, 18, 19, 20, 22));
        assertEquals(expected, actual);
    }
}

