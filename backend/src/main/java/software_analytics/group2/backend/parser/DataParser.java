package software_analytics.group2.backend.parser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import software_analytics.group2.backend.model.Label;
import software_analytics.group2.backend.model.person.PersonByEmail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class DataParser {

    /**
     * Method that parses the labels in a JSONObject.
     *
     * @param object : a JSONObject representing an issue.
     * @return : list of label.
     */
    List<Label> parseLabels(JSONObject object) {
        JSONArray labels = (JSONArray) object.get("labels");
        if (labels == null)
            return new ArrayList<>();

        List<Label> labelList = new ArrayList<>();
        for (Object o : labels) {
            JSONObject label = (JSONObject) o;
            String name = (String) label.get("name");
            String color = (String) label.get("color");
            String description = (String) label.get("description");
            labelList.add(new Label(name, color, description));
        }
        return labelList;
    }

    /**
     * @param object : pull request object to parse
     * @return : person who create the given pull request.
     */
    PersonByEmail parseUser(JSONObject object) {
        JSONObject user = (JSONObject) object.get("user");
        String username = (String) user.get("login");
        return new PersonByEmail(username);
    }

    /**
     * Method to parse the assignee in a issue.
     *
     * @param object : a JSONObject representing an issue.
     * @return : person object.
     */
    PersonByEmail parseAssignee(JSONObject object) {

        if (object.get("assignee") == null)
            return null;

        JSONObject assignee = (JSONObject) object.get("assignee");
        return new PersonByEmail((String) assignee.get("login"));
    }

    /**
     * Method to parse all the assignees in a issue.
     *
     * @param object : a JSONObject representing an issue.
     * @return : list of person objects.
     */
    List<PersonByEmail> parseAssignees(JSONObject object) {

        JSONArray assignees = (JSONArray) object.get("assignees");
        if (assignees == null)
            return new ArrayList<>();

        List<PersonByEmail> assigneeList = new ArrayList<>();
        for (Object o : assignees) {
            JSONObject assignee = (JSONObject) o;
            assigneeList.add(new PersonByEmail((String) assignee.get("assignee.login")));
        }
        return assigneeList;
    }

    /**
     * Method to parse a date in JSONObject.
     *
     * @param object : a JSONObject representing an issue.
     * @param type   : name of the data.
     * @return : LocalDateTime object.
     */
    LocalDateTime parseDate(JSONObject object, String type) {
        String date = (String) object.get(type);
        if (date == null)
            return null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        return LocalDateTime.parse(date, formatter);
    }
}
