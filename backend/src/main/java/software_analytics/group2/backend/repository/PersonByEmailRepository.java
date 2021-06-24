package software_analytics.group2.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import software_analytics.group2.backend.model.person.PersonByEmail;

import java.util.List;

/**
 * Interface that allows you to access the objects saved within the people collection.
 * It also provides methods to query the data contained in the collection itself.
 * It is the link between the database and our application.
 */
@Repository
public interface PersonByEmailRepository extends MongoRepository<PersonByEmail, String> {

    /**
     * @param email : email of the searched person.
     * @return : list of all person objects.
     */
    PersonByEmail findPersonByEmail(String email);

    /**
     * @param name  : name of the searched person.
     * @param email : email of the searched person.
     * @return : list of all person objects.
     */
    List<PersonByEmail> findPeopleByNameAndEmail(String name, String email);

    List<PersonByEmail> findAll();

    /**
     * @param login : login of the searched person.
     * @return : person with given login
     */
    PersonByEmail findPersonByLogin(String login);
}
