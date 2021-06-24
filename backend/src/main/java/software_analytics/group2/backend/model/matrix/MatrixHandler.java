package software_analytics.group2.backend.model.matrix;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import software_analytics.group2.backend.model.commit.Commit;
import software_analytics.group2.backend.model.file.DiffFileRename;
import software_analytics.group2.backend.model.file.ProjectFile;
import software_analytics.group2.backend.model.person.PersonByEmail;
import software_analytics.group2.backend.model.person.PersonResponse;
import software_analytics.group2.backend.service.DatabaseService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "matrixHandlers")
public class MatrixHandler {

	@Id
	private String id;
	String projectName;
	private List<List<Integer>> matrix;
	@DBRef
	private List<PersonByEmail> developers;
	private List<String> developerEmails;
	@DBRef
	private List<ProjectFile> files;
	private List<String> fileNames;


	public MatrixHandler(String projectName) {
		this.projectName = projectName;
		matrix = new ArrayList<>();
		developers = new ArrayList<>();
		developerEmails = new ArrayList<>();
		files = new ArrayList<>();
		fileNames = new ArrayList<>();
	}


	/**
	 * Method to compute the index of a developer, if it not in the matrix it returns -1
	 *
	 * @param file: name of a file
	 * @return      index of the file in the matrix
	 */
	public int getFileIndex(String file) {
		if (fileNames.contains(file))
			return fileNames.indexOf(file);
		return -1;
	}


	/**
	 * Method to compute if the file is already in the matrix
	 *
	 * @param file: name of a file
	 * @return      true if the file exists, false otherwise
	 */
	public boolean isFileInMatrix(String file) {
		return (getFileIndex(file) != -1);
	}


	/**
	 * Method to compute the index of a developer, if it not in the matrix it returns -1
	 *
	 * @param email:	email of the developer
	 * @return          index of the developer in the matrix
	 */
	public int getDeveloperIndex(String email) {
		if (developerEmails.contains(email))
			return developerEmails.indexOf(email);
		return -1;
	}


	/**
	 * Method to compute the index of a developer, if it not in the matrix it adds it and returns the new index
	 *
	 * @param developer:    object of the developer
	 * @return              index of the developer in the list
	 */
	public int getDeveloperIndexOrAdd(PersonByEmail developer) {
		int index = getDeveloperIndex(developer.getEmail());
		if (index != -1)
			return index;
		developers.add(developer);
		developerEmails.add(developer.getEmail());
		if (!matrix.isEmpty()) {
			matrix.forEach(list -> list.add(0));
			return matrix.get(0).size()-1;
		}
		return 0;
	}

	/**
	 * Add a file in the matrix
	 *
	 * @param fileName: name of the file
	 * @param commit:   commit that introduced the file
	 */
	public void addFile(String fileName, Commit commit) {
		files.add(new ProjectFile(projectName, fileName, new ArrayList<>()));
		fileNames.add(fileName);
		getFile(fileName).addCommit(commit);
		matrix.add(IntStream.range(0, developers.size()).mapToObj(i -> 0).collect(Collectors.toList()));
	}

	/**
	 *  Adds a file to the matrix and increments the counter of the given developer on that file
	 *
	 * @param fileName:   name of the file
	 * @param commit:     commit that introduced the file
	 * @param indexOfDev: index of the developer in the matrix
	 */
	public void addFileAndIncrementCell(String fileName, Commit commit, int indexOfDev) {
		if (!isFileInMatrix(fileName))
			addFile(fileName, commit);
		getFile(fileName).addCommit(commit);
		incrementCell(fileName, commit, indexOfDev);
	}

	public ProjectFile getFile(String fileName) {
		for (ProjectFile file : files) {
			if (file.getPath().equals(fileName))
				return file;
		}
		return null;
	}

	/**
	 *  Renames a file in this class
	 *
	 * @param fileRenamed:  file that has been renamed
	 * @param commit:       commit object that renamed the file
	 * @param indexOfDev:   index of developer in the matrix

	 */
	public void renameFile(DiffFileRename fileRenamed, Commit commit, int indexOfDev) {
		String from = fileRenamed.getFrom();
		String to = fileRenamed.getTo();
		if (fileNames.contains(to))
			mergeColumns(from, to);
		int indexOfFile = fileNames.indexOf(from);
		if (indexOfFile != -1) {
			fileNames.set(indexOfFile, to);
			ProjectFile file = files.get(indexOfFile);
			file.setPath(to);
			file.addCommit(commit);
			incrementCell(indexOfFile, indexOfDev);
		}
	}

	private void mergeColumns(String file0, String file1) {
		int index0 = fileNames.indexOf(file0);
		int index1 = fileNames.indexOf(file1);

		if (index0 != -1 && index1 != -1) {
			List<Integer> list0 = matrix.get(index0);
			List<Integer> list1 = matrix.get(index1);
			for (int i=0, s=developerEmails.size(); i<s; ++i)
				list0.set(i, list0.get(index0) + list1.get(index1));
			files.remove(index1);
			fileNames.remove(index1);
			matrix.remove(index1);
		}


	}


	/**
	 *  Deletes a file in this class
	 *
	 * @param file:   file name to remove from the matrix
	 */
	public void deleteFile(String file) {
		int indexOfFile = getFileIndex(file);
		if (indexOfFile != -1) {
			files.remove(indexOfFile);
			fileNames.remove(indexOfFile);
			matrix.remove(indexOfFile);
		}
	}


	/**
	 * Increments the counter of a developer on a given file
	 *
	 * @param file:       index of a file
	 * @param developer:  index of the developer
	 */
	public void incrementCell(int file, int developer) {
		if (file != -1 && developer != -1) {
			int oldValue = matrix.get(file).get(developer);
			matrix.get(file).set(developer, ++oldValue);
		}
	}


	/**
	 * Increments the counter of a developer on a given file
	 *
	 * @param nameOfFile:        name of the file object
	 * @param commit:   commit that introduced the file
	 * @param indexOfDev:  index of the developer in the matrix
	 */
	public void incrementCell(String nameOfFile, Commit commit, int indexOfDev) {
		getFile(nameOfFile).addCommit(commit);
		incrementCell(getFileIndex(nameOfFile), indexOfDev);
	}

	/**
	 * Updated the references of the data structures of this class
	 *
	 * @param databaseService:  service to talk to the database
	 */
	public void updateDB(DatabaseService databaseService) {
		files.forEach(databaseService::saveFile);
		developers.forEach(databaseService::savePerson);
		databaseService.saveMatrixHandler(this);
	}

	/**
	 * Returns a string representing the matrix on the console
	 */
	public String matrixToString() {
		StringBuilder matrixToString = new StringBuilder();
		matrixToString.append("Matrix").append("##### DEV #####\n");
		for (PersonByEmail dev : developers)
			matrixToString.append(dev.getName()).append(" ").append(dev.getEmail()).append('\n');
		matrixToString.append("##### FILE #####\n");
		for (String file : fileNames)
			matrixToString.append(file).append('\n');
		for (List<Integer> row : matrix)
			matrixToString.append(row.toString()).append("\n");
		return matrixToString.toString();
	}

	/**
	 * Returns the response of the best score of the matrix
	 */
	public MatrixResponse computeBestDeveloperForEachFile() {
		MatrixResponse response = new MatrixResponse();
		for (int j = 0, matrixSize = matrix.size(); j < matrixSize; j++) {
			List<Integer> list = matrix.get(j);
			int maxScore = 0;
			int total = 0;
			int index = 0;
			for (int i = 0, listSize = list.size(); i < listSize; ++i) {
				int score = list.get(i);
				total += score;
				if (score > maxScore) {
					maxScore = score;
					index = i;
				}
			}
			response.add(fileNames.get(j), new PersonResponse(developers.get(index), projectName),
					(total != 0) ? 100 * maxScore / total : 0);
		}
		return response;
	}


	public int computeScoreOfDevelorerOnFile(String developer, String file) {
		int developerIndex = getDeveloperIndex(developer);
		int fileIndex = getFileIndex(file);
		if (developerIndex == -1 || fileIndex == -1)
			return -1;
		return matrix.get(fileIndex).get(developerIndex);
	}

}
