package software_analytics.group2.backend.utility;

import software_analytics.group2.backend.model.Pair;

public class StringUtility {

    private StringUtility() {
    }

    /**
     * Method that checks that the repository url is built correctly.
     *
     * @param repoUrl : url of the repository to analyse.
     * @return : correct repo url.
     */
    public static String checkRepoUrl(String repoUrl) {
        String githubUrl = "https://github.com/";

        if (!repoUrl.contains(githubUrl))
            repoUrl = githubUrl + repoUrl;

        if (!repoUrl.contains("https://"))
            repoUrl = "https://" + repoUrl;

        if (!repoUrl.contains(".git"))
            repoUrl = repoUrl + ".git";

        return repoUrl;
    }

    /**
     * Method that allows you to extract the project name and
     * with which the folder that will contain it will be created.
     *
     * @param repoUrl : url of the repository to analyse.
     * @return : a pair composed by first and second project name.
     */
    public static Pair<String, String> projectName(String repoUrl, String delimiter) {
        String[] repoPaths = repoUrl.split(delimiter);
        String cloneDirectoryName = repoPaths[repoPaths.length - 1];
        String first = repoPaths[repoPaths.length - 2];
        String second = cloneDirectoryName.substring(0, cloneDirectoryName.length() - 4);
        return new Pair<>(first, second);
    }
}
