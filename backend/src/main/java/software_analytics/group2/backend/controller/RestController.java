package software_analytics.group2.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import software_analytics.group2.backend.analysis.RepositoryAnalysis;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.commit.Commit;
import software_analytics.group2.backend.model.commit.CommitResponse;
import software_analytics.group2.backend.model.metric.ComparisonMetric;
import software_analytics.group2.backend.model.metric.Metric;
import software_analytics.group2.backend.model.project.ProjectResponse;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.service.DatabaseService;

import java.util.List;

/**
 * Rest controller handles all the users' requests and
 * call the right function to manage the requests.
 */

@Controller
public class RestController {

    @Autowired
    private DatabaseService databaseService;

    /**
     * Method that allows to analyse a new repository through the given url.
     *
     * @param repoUrl : project url to analyze
     * @return : project object
     */
    @ResponseBody
    @GetMapping("/repoAnalysis")
    public ProjectResponse getIndexPage(@RequestParam String repoUrl) {
        return RepositoryAnalysis.getInstance(databaseService).repositoryAnalysis(repoUrl);
    }

    /**
     * Method that allows to update an existing project within the database.
     *
     * @param repoUrl : project url to analyze
     * @return : project object
     */
    @ResponseBody
    @GetMapping("/repoUpdate")
    public ProjectResponse getRepoUpdated(@RequestParam String repoUrl) {
        return RepositoryAnalysis.getInstance(databaseService).updateRepository(repoUrl);
    }

    /**
     * Method that allows to get all the project name in the database.
     *
     * @return : the list of all project name in the database.
     */
    @ResponseBody
    @GetMapping("/repoNames")
    public List<String> getAllRepoName() {
        return databaseService.getAllRepositoryName();
    }

    /**
     * Method that allow to get all the specific data of an issue
     * through the given issueId and project name.
     *
     * @param issueId     : issue id to search
     * @param projectName : name of the project
     * @return : issue object
     */
    @ResponseBody
    @GetMapping("/issue/{issueId}")
    public Issue getIssueByIdAndProjectName(@PathVariable Long issueId, @RequestParam String projectName) {
        return databaseService.getIssueByIdAndProjectName(issueId, projectName);
    }

    /**
     * Method that allow to get all the specific data of a commit
     * through the given commitId and project name.
     *
     * @param commitId    : commit id to search
     * @param projectName : name of the project
     * @return : commit object
     */
    @ResponseBody
    @GetMapping("/commit/{commitId}")
    public Pair<CommitResponse, Pair<Metric, ComparisonMetric>> getCommitByIdAndProjectName(@PathVariable String commitId, @RequestParam String projectName) {
        RepositoryAnalysis instance = RepositoryAnalysis.getInstance(databaseService);
        Commit commit = databaseService.getCommitByIdAndProjectName(commitId, projectName);
        return new Pair<>(new CommitResponse(commit), instance.computeMetric(commit, projectName));
    }
}
