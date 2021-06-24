package software_analytics.group2.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import software_analytics.group2.backend.model.matrix.MatrixHandler;


/**
 * Interface that allows you to access the objects saved within the matrixHadlers collection.
 * It also provides methods to query the data contained in the collection itself.
 * It is the link between the database and our application.
 */
@Repository
public interface MatrixHandlerRepository extends MongoRepository<MatrixHandler, String> {

	/**
	 * @param projectName : name of the project.
	 * @return : MatrixHandler object.
	 */
	MatrixHandler findMatrixHandlerByProjectName(String projectName);

}


