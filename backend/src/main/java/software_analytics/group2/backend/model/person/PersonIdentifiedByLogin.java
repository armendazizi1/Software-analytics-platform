package software_analytics.group2.backend.model.person;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonIdentifiedByLogin {

    private String login;
    private double openedPRPercent;
    private double reviewedPRPercent;

    public PersonIdentifiedByLogin(PersonByEmail person, String projectName) {
        this.login = person.getLogin();
        this.openedPRPercent = person.computeAcceptedPercentage(projectName);
        this.reviewedPRPercent = person.computeReviewedPercentage(projectName);
    }


}
