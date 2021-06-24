package software_analytics.group2.backend.reader;

import software_analytics.group2.backend.model.metric.Metric;

import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class the allows to read and parse csv file.
 */
public class CSVReader {

    private static final Logger LOGGER = Logger.getLogger(CSVReader.class.getName());

    /**
     * Method that read and compute the Metric for a given csv file.
     *
     * @param fileName    : name of the csv file to parse.
     * @param commitId    : commit id (project snapshot analysed).
     * @param projectName : name of the project in which we perform the metric operations.
     * @return : metric object
     */
    public Metric readMetricsFromCSV(String fileName, String commitId, String projectName) {
        double cob = 0;
        double wmc = 0;
        double lcom = 0;
        double loc = 0;
        int count = 0;

        try (com.opencsv.CSVReader reader = new com.opencsv.CSVReader(new FileReader(fileName))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (count > 0) {
                    cob += Integer.parseInt(line[3]);
                    wmc += Integer.parseInt(line[4]);
                    lcom += Integer.parseInt(line[5]);
                    loc += Integer.parseInt(line[6]);
                }
                count++;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurs while reading the csv metric file", e);
        }

        count = (count == 0) ? 1 : count;
        return new Metric(commitId, projectName, cob / count, wmc / count,
                lcom / count, loc / count);
    }
}
