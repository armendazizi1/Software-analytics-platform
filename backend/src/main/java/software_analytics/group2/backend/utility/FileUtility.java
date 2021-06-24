package software_analytics.group2.backend.utility;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FileUtility {

    private static final Logger LOGGER = Logger.getLogger(FileUtility.class.getName());

    private FileUtility() {
    }

    /**
     * Method that allows to delete the directory of the cloned repository.
     *
     * @param dir : directory where we want to delete files.
     * @return : true if the directory is deleted, otherwise false.
     */
    public static boolean deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            assert files != null;
            for (File file : files)
                if (file.isDirectory())
                    deleteDirectory(file);
                else
                    deleteFile(file, "file");
        }
        return deleteFile(dir, "directory");
    }

    /**
     * Method to delete a given file.
     *
     * @param file       : file to delete
     * @param typeOfFile : type of the file.
     * @return : true if the file is deleted, otherwise false.
     */
    public static boolean deleteFile(File file, String typeOfFile) {
        if (!file.delete()) {
            LOGGER.log(Level.WARNING, () -> typeOfFile + file.getName() + " was not deleted");
            return false;
        }
        LOGGER.log(Level.INFO, () -> "Deleted " + typeOfFile + " : " + file.getName());
        return true;
    }

    /**
     * Method to copy the content of the src file into the dest file.
     *
     * @param source   : source file
     * @param dest     : destination file
     * @param position : where start copying.
     * @return true if the src file is deleted, otherwise false.
     */
    public static boolean copyFile(File source, File dest, int position) {
        String line;
        try (BufferedReader input = new BufferedReader(new FileReader(source));
             BufferedWriter output = new BufferedWriter(new FileWriter(dest));) {
            for (int i = 0; i < position; i++)
                line = input.readLine();
            while ((line = input.readLine()) != null) {
                output.write(line + "\n");
                output.flush();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurs while copying the file", e);
        }
        return deleteFile(source, "source");
    }

    /**
     * Method to extract a given line from a given file.
     *
     * @param lineNumber : number of the line we want to extract.
     * @param path       : path of the file in which we want to extract the line.
     * @return : extracted line.
     * @throws IOException : exception handled.
     */
    public static String getLine(int lineNumber, String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).get(lineNumber);
    }

    /**
     * Method to get all the file names in a directory.
     *
     * @param dir : path to the dir.
     * @return : name of the files in that directory.
     */
    public static Set<String> listFiles(String dir) {
        return Stream.of(Objects.requireNonNull(new File(dir).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName).sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Method to get all comment line index from the given file.
     *
     * @param filepath : path to the file to be parsed.
     * @return : list of comment line contained in the given file.
     */
    public static List<Integer> extractCommentLines(String filepath) {
        int index = 1;
        String line;
        String track = null;
        List<Integer> tmp = new ArrayList<>();
        List<Integer> commentLineNumbers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("//")) {
                    commentLineNumbers.add(index);
                }
                if (line.startsWith("/")) {
                    if (line.endsWith("/"))
                        commentLineNumbers.add(index);
                    else {
                        track = String.valueOf(index);
                        tmp.add(index);
                    }
                } else if (line.endsWith("*/") && track != null) {
                    IntStream.rangeClosed(Integer.parseInt(track), index).forEach(tmp::add);
                    commentLineNumbers.addAll(tmp);
                    tmp.clear();
                    track = null;
                }
                index++;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "File not found in the given path", e);
        }
        Set<Integer> set = new HashSet<>(commentLineNumbers);
        commentLineNumbers.clear();
        commentLineNumbers.addAll(set);
        Collections.sort(commentLineNumbers);
        return commentLineNumbers;
    }
}
