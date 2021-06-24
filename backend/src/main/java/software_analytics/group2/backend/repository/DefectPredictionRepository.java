package software_analytics.group2.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import software_analytics.group2.backend.DefectPrediction;


@Repository
public interface DefectPredictionRepository extends MongoRepository<DefectPrediction, String> {

    /**
     * @param projectName : name of the project.
     * @return : DefectPrediction object.
     */
    DefectPrediction findDefectPredictionByProjectName(String projectName);

}
