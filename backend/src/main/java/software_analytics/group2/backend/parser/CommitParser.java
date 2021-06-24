package software_analytics.group2.backend.parser;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import software_analytics.group2.backend.model.CommitFilesDifference;
import software_analytics.group2.backend.model.Coupling;
import software_analytics.group2.backend.model.matrix.MatrixHandler;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.commit.Commit;
import software_analytics.group2.backend.model.commit.CommitBuilder;
import software_analytics.group2.backend.model.file.DiffFileRename;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.project.Project;
import software_analytics.group2.backend.model.project.ProjectBuilder;
import software_analytics.group2.backend.model.repo.data.issue.Issue;
import software_analytics.group2.backend.service.DatabaseService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class used to Parse a Git Log to Java Objects and store them in the database
 */
public class CommitParser {

    private static final Logger LOGGER = Logger.getLogger(CommitParser.class.getName());

    private final DatabaseService databaseService;
    private final CommitInducingBugParser commitInducingBugParser;
    private static final String DELIMITER = "##_._##\n";

    /**
     * @param databaseService: to communicate with the database
     */
    public CommitParser(DatabaseService databaseService) {
        this.databaseService = databaseService;
        this.commitInducingBugParser = CommitInducingBugParser.getInstance();
    }


    /**
     * Method get all the commits from the analysed project.
     *
     * @param projectName:    name of the project currently being analyzed
     * @param directoryPath:  path of the project directory
     * @param projectBuilder: builder of the current project object
     */
    public void parseCommits(String projectName, String directoryPath, ProjectBuilder projectBuilder) {
        projectBuilder.coupling(new Coupling(projectName, new HashMap<>(), new HashMap<>()));
        projectBuilder.bugs(new HashMap<>());
        Map<String, PersonByEmail> peopleMap = new HashMap<>();
        Map<String, String> commitIdMap = new HashMap<>();
        List<CommitBuilder> commitBuilders = new ArrayList<>();
        List<CommitBuilder> commitsToFix = new ArrayList<>();

        try (Repository repository = new FileRepository(directoryPath + "/.git");
             Git git = new Git(repository);
             Scanner log = new Scanner(new File(directoryPath + "/log.txt")).useDelimiter(DELIMITER);
             Scanner stats = new Scanner(new File(directoryPath + "/stats.txt")).useDelimiter(DELIMITER)) {

            RevWalk revWalk = new RevWalk(repository);
            revWalk.sort(RevSort.TOPO);
            Ref branch = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().get(0);
            revWalk.markStart(revWalk.parseCommit(branch.getObjectId()));
            LinkedHashMap<String, CommitFilesDifference> differenceList = computeFileDifferencesMap(log);
            addStatsToMap(stats, differenceList);
            Map<String, RevCommit> revCommitMap = createRevCommitMap(repository, git);

            for (Map.Entry<String, CommitFilesDifference> entry : differenceList.entrySet()) {
                String id = entry.getKey();
                RevCommit revCommit = revCommitMap.get(id);
                CommitBuilder commitBuilder = createBasicCommitBuilder(projectName, revCommit, peopleMap);
                commitBuilder.difference(entry.getValue());
                checkCoupling(commitBuilder, projectBuilder.getCoupling(), projectBuilder.getClosedIssues(),
                        projectName);
                checkForBugs(commitBuilder, commitsToFix, projectBuilder);
                addCommit(commitBuilder, commitBuilders, commitIdMap, null);
            }
        } catch (IOException | GitAPIException e) {
            LOGGER.log(Level.WARNING, "Error while trying to parse the commits", e);
        }
        fixCommitIdsOfResolving(commitsToFix, commitIdMap);
        projectBuilder.commits(buildCommits(commitBuilders));
        projectBuilder.matrixHandler(createMatrix(projectBuilder.getCommits(), projectName));
        projectBuilder.people(new HashSet<>(peopleMap.values()));
        saveProjectInDB(databaseService, projectBuilder);
    }

    /**
     * Method to update the commit list int he analysed project.
     *
     * @param directoryPath : path of the local project directory
     * @param project       : Project object created at the last parse/update
     */
    public void updateCommits(String directoryPath, Project project) {
        String projectName = project.getName();
        List<CommitBuilder> commitBuilders = new ArrayList<>();
        List<CommitBuilder> commitsThatResolve = new ArrayList<>();
        List<String> newCommitIds = new ArrayList<>();
        Map<String, String> commitIdMap = project.getCommits().stream().map(Commit::getId)
                .collect(Collectors.toMap(id -> id.substring(0, 8), id -> id));
        Map<String, PersonByEmail> peopleMap = project.getPeople().stream()
                .collect(Collectors.toMap(PersonByEmail::getEmail, person -> person));
        Map<String, Set<String>> commitsThatInduce = new HashMap<>();

        try (Repository repository = new FileRepository(directoryPath + "/.git");
             Git git = new Git(repository);
             Scanner log = new Scanner(new File(directoryPath + "/log.txt")).useDelimiter(DELIMITER);
             Scanner stats = new Scanner(new File(directoryPath + "/stats.txt")).useDelimiter(DELIMITER)) {

            String lastOldCommit = project.getCommits().get(0).getId();
            LinkedHashMap<String, CommitFilesDifference> differenceList = computeFileDifferencesMap(log, lastOldCommit);
            addStatsToMap(stats, lastOldCommit, differenceList);
            Map<String, RevCommit> revCommitMap = createRevCommitMap(repository, git, lastOldCommit);


            for (Map.Entry<String, CommitFilesDifference> entry : differenceList.entrySet()) {
                String id = entry.getKey();
                RevCommit revCommit = revCommitMap.get(id);
                if (revCommit.getName().equals(lastOldCommit))
                    break;

                CommitBuilder commitBuilder = createBasicCommitBuilder(projectName, revCommit, peopleMap);
                commitBuilder.difference(entry.getValue());
                checkCoupling(commitBuilder, project.getCoupling(), project.getClosedIssues(),
                        projectName);
                checkForBugs(commitBuilder, commitsThatResolve, project, commitsThatInduce, newCommitIds);
                addCommit(commitBuilder, commitBuilders, commitIdMap, newCommitIds);
            }
        } catch (IOException | GitAPIException e) {
            LOGGER.log(Level.WARNING, "Error while trying to parse the commits", e);
        }
        fixCommitIdsOfResolving(commitsThatResolve, commitIdMap);
        updateOldCommitsThatInduce(project.getCommits(), commitsThatInduce);
        List<Commit> newCommits = buildCommits(commitBuilders);
        project.setCommits(Stream.of(newCommits, project.getCommits()).flatMap(Collection::stream)
                .collect(Collectors.toList()));
        updateMatrix(project.getMatrixHandler(), newCommits);
        project.setPeople(new HashSet<>(peopleMap.values()));
        project.saveProjectFieldsInDB(databaseService);
        newCommits.forEach(databaseService::saveCommit);
    }

    /**
     * Method to create a map commit id to objects with file changes of that commit
     *
     * @param scanner:  scanner opened on log file of the project
     * @return          map commit id to object with the files impacted in that commit
     */
    private LinkedHashMap<String, CommitFilesDifference> computeFileDifferencesMap(Scanner scanner) {
        LinkedHashMap<String, CommitFilesDifference> map = new LinkedHashMap<>();
        while (scanner.hasNext()) {
            String[] block = scanner.next().split("\n");
            String id = computeId(block[0]);
            CommitFilesDifference diff = getChanges(block);
            map.put(id, diff);
        }
        return map;
    }

    /**
     * Finds the commit id in a line
     *
     * @param line:     first line of the commit text in the log
     * @return          the id of the commit
     */
    private String computeId(String line) {
        return line.substring(7);
    }

    /**
     * Method to create a map commit id to objects with file changes of that commit
     *
     * @param scanner:  scanner opened on log file of the project
     * @param last:     id of the last parsed commit before the update
     * @return          map commit id to object with the files impacted in that commit
     */
    private LinkedHashMap<String, CommitFilesDifference> computeFileDifferencesMap(Scanner scanner, String last) {
        LinkedHashMap<String, CommitFilesDifference> map = new LinkedHashMap<>();
        while (scanner.hasNext()) {
            String[] block = scanner.next().split("\n");
            String id = computeId(block[0]);
            if (id.equals(last))
                break;
            CommitFilesDifference diff = getChanges(block);
            map.put(id, diff);
        }
        return map;
    }

    /**
     * Method to extract the changed files by a given commit.
     *
     * @param lines:        array of text of that represent each line of the commit in the log
     * @return CommitFilesDifference:    object with the edits of the given commit
     */
    private CommitFilesDifference getChanges(String[] lines) {
        CommitFilesDifference fileDiff = new CommitFilesDifference();

        for (int i = 1, linesLength = lines.length; i < linesLength; i++) {
            String line = lines[i];
            if (line.startsWith("A\t"))
                fileDiff.getAdded().add(line.substring(2));
            else if (line.startsWith("M\t"))
                fileDiff.getModified().add(line.substring(2));
            else if (line.startsWith("D\t")) {
                fileDiff.getDeleted().add(line.substring(2));
            } else if (line.startsWith("R")) {
                String[] files = line.substring(5).split("\t");
                fileDiff.getRenamed().add(new DiffFileRename(files[0], files[1], Integer.parseInt(line.substring(1, 4))));
            }
        }
        databaseService.saveFileDifference(fileDiff);
        return fileDiff;
    }

    /**
     * Method to add the number of changed lines in the correct CommitFilesDifference object
     *
     * @param scanner:  scanner opened on stats file of the project
     *
     */
    private void addStatsToMap(Scanner scanner, LinkedHashMap<String, CommitFilesDifference> map) {
        while (scanner.hasNext()) {
            String[] block = scanner.next().split("\n");
            String id = computeId(block[0]);
            if (block.length >= 2)
                addValueOfChangedLines(block[1], map.get(id));
        }
    }

    /**
     * Method to add the number of changed lines in the correct CommitFilesDifference object
     *
     * @param scanner:      scanner opened on stats file of the project
     * @param lastCommit:   id of the last parsed commit before the update
     *
     */
    private void addStatsToMap(Scanner scanner, String lastCommit, LinkedHashMap<String, CommitFilesDifference> map) {
        while (scanner.hasNext()) {
            String[] block = scanner.next().split("\n");
            String id = computeId(block[0]);
            if (id.equals(lastCommit))
                break;
            if (block.length >= 2)
                addValueOfChangedLines(block[1], map.get(id));
        }
    }

    /**
     * Method that computes the number of changed line (added and deleted) of a commit
     *
     * @param line :                    text of the stats file with the short stat information
     * @param commitFilesDifference:    object representing the changes of the commit
     */
    private void addValueOfChangedLines(String line, CommitFilesDifference commitFilesDifference) {
        String[] parts = line.split(" ");
        commitFilesDifference.setAddedLines(Integer.parseInt(parts[4]));
        commitFilesDifference.setAddedLines(parts.length >= 7 ? Integer.parseInt(parts[6]) : 0);
        databaseService.saveFileDifference(commitFilesDifference);
    }

    /**
     *
     * @param repository:           Jgit repository object
     * @param git:                  Jgit git object
     * @return map                  commit id to revcommit object
     * @throws IOException:         if log file is does not exist
     * @throws GitAPIException      jGit
     */
    private Map<String, RevCommit> createRevCommitMap(Repository repository, Git git) throws IOException, GitAPIException {
        Map<String, RevCommit> map = new HashMap<>();
        for (RevCommit revCommit : git.log().add(repository.resolve("master")).call())
            map.put(revCommit.getName(), revCommit);
        return map;
    }

    /**
     *
     * @param repository:           Jgit repository object
     * @param git:                  Jgit git object
     * @param lastId:               id of the last parsed commit before update
     * @return map                  commit id to revcommit object
     * @throws IOException:         if log file is does not exist
     * @throws GitAPIException      jGit
     */
    private Map<String, RevCommit> createRevCommitMap(Repository repository, Git git, String lastId)
        throws IOException, GitAPIException {
        Map<String, RevCommit> map = new HashMap<>();
        for (RevCommit revCommit : git.log().add(repository.resolve("master")).call()) {
            String id = revCommit.getName();
            if (id.equals(lastId))
                break;
            map.put(id, revCommit);
        }
        return map;
    }


    /**
     * Creates a commit with the basic information
     *
     * @param projectName:  name of the project
     * @param revCommit:    JGit commit object
     * @param peopleMap:    map email to person object
     * @return              CommitBuilder object
     */
    private CommitBuilder createBasicCommitBuilder(String projectName, RevCommit revCommit,
                                                   Map<String, PersonByEmail> peopleMap) {
        CommitBuilder commitBuilder = new CommitBuilder(projectName);
        String id = revCommit.getName();
        commitBuilder.id(id);
        parseBasicCommit(commitBuilder, revCommit, peopleMap);
        return commitBuilder;
    }

    /**
     * * Method to parse a commit.
     *
     * @param commitBuilder :   builder for the commit currently parsed
     * @param revCommit     :   JGit commit object
     * @param people        :   Map of email to person object
     */
    private void parseBasicCommit(CommitBuilder commitBuilder, RevCommit revCommit, Map<String, PersonByEmail> people) {
        PersonIdent authorIdent = revCommit.getAuthorIdent();
        commitBuilder.author(getPerson(authorIdent, people));
        commitBuilder.date(authorIdent.getWhen());
        commitBuilder.message(revCommit.getFullMessage());
    }

    /**
     * Method to extract the information of the person.
     *
     * @param personIdent: object that holds the information of the person
     * @param people:      map email to person object
     * @return Person:      object that represents the person
     */
    private PersonByEmail getPerson(PersonIdent personIdent, Map<String, PersonByEmail> people) {
        String email = personIdent.getEmailAddress();
        PersonByEmail person;
        if (people.containsKey(email))
            person = people.get(email);
        else {
            person = databaseService.getPersonByEmail(email);
            if (person == null)
                person = new PersonByEmail(personIdent.getName(), email);
            people.put(email, person);
        }
        return person;
    }


    /**
     * Checks the coupling of the project for the given commit
     *
     * @param commitBuilder: builder for the commit currently parsed
     * @param coupling:      coupling object of the current project
     * @param projectName:   name of the project currently analyzed
     */
    private void checkCoupling(CommitBuilder commitBuilder, Coupling coupling, List<Pair<Long, Long>> closedIssues,
                               String projectName) {
        String message = commitBuilder.getMessage().toLowerCase();
        Matcher matcher = Pattern.compile(" [#]?[0-9]+ ").matcher(message);

        if (matcher.find()) {
            long number = Long.parseLong(matcher.group(0).replace('#', ' ').strip());

            if (message.contains("pull request"))
                addEntryToMap(coupling.getCommitToPullRequest(), number, commitBuilder.getId());
            else if ((message.contains("issue") || message.contains("fix"))) {
                int i = indexOfIssueClosed(closedIssues, number);
                if (i != -1) {
                    addEntryToMap(coupling.getCommitToIssues(), number, commitBuilder.getId());
                    Issue issueClosing = databaseService.getIssueByIdAndProjectName(closedIssues.get(i).getLeft(),
                            projectName);
                    commitBuilder.issue(issueClosing);
                }
            }
        }
    }

    /**
     * Method to add a new entry to the given map.
     *
     * @param map:    from issue or pull request number to list of commits
     * @param number: of the issue or pull request
     * @param id:     of the commit
     */
    private void addEntryToMap(Map<Long, List<String>> map, Long number, String id) {
        if (map.containsKey(number))
            map.get(number).add(id);
        else
            map.put(number, new ArrayList<>(Collections.singletonList(id)));
    }

    /**
     * Method to get the index of closed issue in the given list.
     *
     * @param closedIssues: list of pairs of closed issue numbers
     * @param number:       of the issue to find
     * @return id:           of the given issue
     */
    private int indexOfIssueClosed(List<Pair<Long, Long>> closedIssues, long number) {
        for (int i = 0; i < closedIssues.size(); i++) {
            Pair<Long, Long> issue = closedIssues.get(i);
            if (issue.getRight().equals(number))
                return i;
        }
        return -1;
    }

    /**
     * Check if the current commit introduces, resolves a bug or both
     *
     * @param commitBuilder:    commit currently being built
     * @param commitsToFix:     list of commits that will need to be fixed
     * @param projectBuilder:   project currently being built
     * @throws IOException:     caused if a file parsed is missing
     */
    private void checkForBugs(CommitBuilder commitBuilder, List<CommitBuilder> commitsToFix,
                              ProjectBuilder projectBuilder) throws IOException {
        Map<String, Set<String>> bugs = projectBuilder.getBugs();
        checkIfCommitInducedBug(commitBuilder, bugs);
        if (isResolvingBug(commitBuilder, bugs, projectBuilder.getName()))
            commitsToFix.add(commitBuilder);
    }

    /**
     * Checks if the commit resolves a bug
     *
     * @param commitBuilder :   commit that is currently analyzed
     * @param bugs          :   map of commits that solved bugs to commits that induced it
     * @param projectName   :   name of the project
     * @return :   if the commit resolves a bug
     * @throws IOException :   caused if a file parsed is missing
     */
    private boolean isResolvingBug(CommitBuilder commitBuilder, Map<String, Set<String>> bugs, String projectName)
            throws IOException {
        Set<String> newInducers = commitInducingBugParser.getCommitInducingBugs(projectName, commitBuilder);
        if (!newInducers.isEmpty()) {
            bugs.put(commitBuilder.getId(), newInducers);
            commitBuilder.resolvingBugIntroducedBy(newInducers);
            return true;
        }
        return false;
    }


    /**
     * Check if the current commit introduces, resolves a bug or both
     *
     * @param commitBuilder:        commit currently being built
     * @param commitsToFix:         list of commits that will need to be fixed
     * @param project:              project currently being built
     * @param commitsThatInduce: list of past commits that induce
     * @param newCommitIds:         list of ids of the new commits
     * @throws IOException:     caused if a file parsed is missing
     */
    private void checkForBugs(CommitBuilder commitBuilder, List<CommitBuilder> commitsToFix, Project project,
                              Map<String, Set<String>> commitsThatInduce, List<String> newCommitIds) throws IOException {
        Map<String, Set<String>> bugs = project.getBugs();
        checkIfCommitInducedBug(commitBuilder, bugs);
        if (isResolvingBug(commitBuilder, bugs, project.getName(), commitsThatInduce, newCommitIds))
            commitsToFix.add(commitBuilder);
    }

    /**
     * Checks if the commit resolves a bug
     *
     * @param commitBuilder :       commit that is currently analyzed
     * @param bugs          :       map of commits that solved bugs to commits that induced it
     * @param projectName   :       name of the project
     * @param commitsThatIntroduce: list of past commits that induce
     * @param newCommitIds:         list of ids of the new commits
     * @return :                    if the commit resolves a bug
     * @throws IOException :        caused if a file parsed is missing
     */
    private boolean isResolvingBug(CommitBuilder commitBuilder, Map<String, Set<String>> bugs, String projectName,
                                   Map<String, Set<String>> commitsThatIntroduce, List<String> newCommitIds)
            throws IOException {
        Set<String> newInducers = commitInducingBugParser.getCommitInducingBugs(projectName, commitBuilder);
        if (!newInducers.isEmpty()) {
            bugs.put(commitBuilder.getId(), newInducers);
            commitBuilder.resolvingBugIntroducedBy(newInducers);
            for (String shortId : newInducers) {
                if (!newCommitIds.contains(shortId)) {
                    if (commitsThatIntroduce.containsKey(shortId))
                        commitsThatIntroduce.get(shortId).add(commitBuilder.getId());
                    else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(commitBuilder.getId());
                        commitsThatIntroduce.put(shortId, new HashSet<>(list));
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Method that verifies whether a given commit has induced bugs.
     *
     * @param commitBuilder: the commit being analyzed
     * @param bugs:          map<commitId, Set<CommitId> that induced bugs
     */
    private void checkIfCommitInducedBug(CommitBuilder commitBuilder, Map<String, Set<String>> bugs) {
        String shortId = commitBuilder.getId().substring(0, 8);
        Set<String> inducing = bugs.entrySet().stream().filter(entry -> entry.getValue().contains(shortId))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        if (!inducing.isEmpty())
            commitBuilder.inducingBugFixedBy(inducing);
    }

    /**
     *  Add commit builder to the list of commits being built, to the id map and returns its id
     *
     * @param commitBuilder:    commit currently parsed
     * @param commitBuilders:   list of all previously parsed commits of this project
     * @param commitIdMap:      map short id to long id of each commit
     */
    private void addCommit(CommitBuilder commitBuilder, List<CommitBuilder> commitBuilders,
                             Map<String, String> commitIdMap, List<String> newCommitIds) {
        String id = commitBuilder.getId();
        commitBuilders.add(commitBuilder);
        commitIdMap.put(id.substring(0, 8), id);
        if (newCommitIds != null)
            newCommitIds.add(id.substring(0, 8));
    }

    /**
     * Method that verifies whether a given commit has resolved bugs.
     *
     * @param commitsToFix: commits that need the to change the shortId with the longId
     * @param idMap:        map of id short to id long
     */
    private void fixCommitIdsOfResolving(List<CommitBuilder> commitsToFix, Map<String, String> idMap) {
        commitsToFix.forEach(commit -> commit.resolvingBugIntroducedBy(commit.getResolvingBugIntroducedBy()
                .stream().map(idMap::get).collect(Collectors.toSet())));
    }

    /**
     * Update the commits parsed in the past to add the ids of the new commits that fixed bugs introduced previously
     *
     * @param commits:           list of commits previously parsed
     * @param commitsThatInduce: map of the id of the commit that introduced bugs to ids of the commits that solved them
     */
    private void updateOldCommitsThatInduce(List<Commit> commits, Map<String, Set<String>> commitsThatInduce) {
        for (int i = 0, commitsSize = commits.size(); i < commitsSize; i++) {
            Commit commit = commits.get(i);
            String id = commit.getId();
            if (commitsThatInduce.containsKey(id.substring(0, 8))) {
                Set<String> ids = commitsThatInduce.get(id);
                CommitBuilder builder = new CommitBuilder(commit);
                if (builder.getInducingBugFixedBy() == null)
                    builder.inducingBugFixedBy(ids);
                else
                    builder.getInducingBugFixedBy().addAll(ids);
                commit = builder.build();
                databaseService.saveCommit(commit);
                commits.set(i, commit);
            }
        }
    }

    /**
     * Method that builds the commits and saves them into the databases
     *
     * @param commitBuilders :  list of commit builders
     * @return commits:         list of built commits
     */
    private List<Commit> buildCommits(List<CommitBuilder> commitBuilders) {
        List<Commit> commits = new ArrayList<>();
        for (CommitBuilder commitBuilder : commitBuilders) {
            Commit commit = commitBuilder.build();
            commits.add(commit);
        }
        return commits;
    }

    /**
     * Create a matrix handler for the project
     *
     * @param commits:      commits of the project
     * @param projectName:  name of the project
     */
    private MatrixHandler createMatrix(List<Commit> commits, String projectName) {
        MatrixHandler handler = new MatrixHandler(projectName);
        updateMatrix(handler, commits);
        return handler;
    }

    /**
     * Update the matrixHandler of a project with the new commits
     *
     * @param handler:  matrixHandler of the project
     * @param commits:  commits to add to the handler to update it
     */
    private void updateMatrix(MatrixHandler handler, List<Commit> commits) {
        ListIterator<Commit> iterator = commits.listIterator(commits.size());
        String projectName = handler.getProjectName();

        while(iterator.hasPrevious()) {
            Commit commit = iterator.previous();
            PersonByEmail developer = commit.getAuthor();
            developer.addCommit(commit, projectName);

            CommitFilesDifference difference = commit.getDifference();
            int indexOfDev = handler.getDeveloperIndexOrAdd(commit.getAuthor());

            for (String file_name : difference.getAdded())
                handler.addFileAndIncrementCell(file_name, commit, indexOfDev);

            for (String file_name : difference.getModified())
                handler.incrementCell(file_name, commit, indexOfDev);

            for (String file_name : difference.getDeleted())
                handler.deleteFile(file_name);

            for (DiffFileRename file : difference.getRenamed())
                handler.renameFile(file, commit, indexOfDev);

            commit.setAuthorExperience(computeAuthorExperience(developer.getEmail(), difference, handler));
        }
        handler.updateDB(databaseService);
        LOGGER.log(Level.INFO, () -> handler.getProjectName() + " matrix\n" + handler.matrixToString());
    }

    /**
     * Compute the experience of a developer in a commit
     *
     * @param email:            email of the developer
     * @param difference:       object with the files touched in a commit
     * @param handler:          matrix object
     * @return                  score of the experience of the developer
     */
    public int computeAuthorExperience(String email, CommitFilesDifference difference, MatrixHandler handler) {
        List<String> files = difference.getAllFilesAfterRename();
        int score = 0;
        int counter = 0;
        for (String file : files) {
            int current = handler.computeScoreOfDevelorerOnFile(email, file);
            if (current != -1) {
                score += current;
                ++counter;
            }
        }
        return (counter != 0) ? Math.round((float) score/counter) : 0;
    }

    /**
     *  Save the objects of the project builder in the database
     *
     * @param databaseService:  service connected to the database
     * @param projectBuilder:   project currently being built
     */
    private void saveProjectInDB(DatabaseService databaseService, ProjectBuilder projectBuilder) {
        databaseService.saveCoupling(projectBuilder.getCoupling());
        projectBuilder.getPeople().forEach(databaseService::savePerson);
        projectBuilder.getCommits().forEach(databaseService::saveCommit);
    }
}