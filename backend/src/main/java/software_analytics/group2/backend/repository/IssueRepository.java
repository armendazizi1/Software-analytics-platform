package software_analytics.group2.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import software_analytics.group2.backend.model.repo.data.issue.Issue;

/**
 * Interface that allows you to access the objects saved within the issues collection.
 * It also provides methods to query the data contained in the collection itself.
 * It is the link between the database and our application.
 */
@Repository
public interface IssueRepository extends MongoRepository<Issue, Long> {

    /**
     * @param issueId     : id of the searched issue.
     * @param projectName : name of the project.
     * @return : issue object.
     */
    Issue findIssueByIssueIdAndProjectName(Long issueId, String projectName);
}
