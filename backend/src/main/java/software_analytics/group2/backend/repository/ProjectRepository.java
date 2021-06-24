package software_analytics.group2.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import software_analytics.group2.backend.model.project.Project;

/**
 * Interface that allows you to access the objects saved within the projects collection.
 * It also provides methods to query the data contained in the collection itself.
 * It is the link between the database and our application.
 */
@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    /**
     * Method that allows us to get a project from the database using its name.
     * In this case it is the first occurrence found in the database
     *
     * @param projectName : name of the searched project.
     * @return : project object.
     */
    Project findByName(String projectName);
}
