package software_analytics.group2.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import software_analytics.group2.backend.model.repo.data.pull_request.PullRequest;

/**
 * Interface that allows you to access the objects saved within the commits collection.
 * It also provides methods to query the data contained in the collection itself.
 * It is the link between the database and our application.
 */
@Repository
public interface PullRequestRepository extends MongoRepository<PullRequest, Long> {

    /**
     * @param pullRequestId : id of the searched pull request.
     * @param projectName   : name of the project.
     * @return : issue object.
     */
    PullRequest findPullRequestByPullIdAndProjectName(Long pullRequestId, String projectName);
}
