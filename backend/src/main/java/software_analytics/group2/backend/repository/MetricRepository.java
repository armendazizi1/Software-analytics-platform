package software_analytics.group2.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import software_analytics.group2.backend.model.metric.Metric;

/**
 * Interface that allows you to access the objects saved within the metrics collection.
 * It also provides methods to query the data contained in the collection itself.
 * It is the link between the database and our application.
 */
@Repository
public interface MetricRepository extends MongoRepository<Metric, String> {

    /**
     * @param commit   : commit id.
     * @param repoName : name of the project.
     * @return : metric object.
     */
    Metric findMetricByCommitAndRepoName(String commit, String repoName);
}
