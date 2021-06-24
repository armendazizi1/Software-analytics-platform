package software_analytics.group2.backend.metric;


import software_analytics.group2.backend.analysis.RepositoryAnalysis;
import software_analytics.group2.backend.git.GitCommand;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.Quartet;
import software_analytics.group2.backend.model.metric.ComparisonMetric;
import software_analytics.group2.backend.model.metric.Metric;
import software_analytics.group2.backend.reader.CSVReader;
import software_analytics.group2.backend.service.DatabaseService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that perform the metrics operation.
 */
public class MetricCalculator {

    private static final Logger LOGGER = Logger.getLogger(MetricCalculator.class.getName());

    private final DatabaseService databaseService;
    private final String csvPath;
    private final CSVReader csvReader;


    public MetricCalculator(DatabaseService databaseService) {
        this.csvReader = new CSVReader();
        this.databaseService = databaseService;
        this.csvPath = "./src/main/resources/libs";
    }

    /**
     * Method that allows to create the metric file (a csv file) for the given project.
     *
     * @param projectName : name of the project to analyse
     */
    private void createMetricFile(String projectName) {

        String command = "java -jar ck-0.6.3-SNAPSHOT-jar-with-dependencies.jar " +
                "../projects/" + projectName + " true 0 false";
        try {
            Process process = Runtime.getRuntime().exec(command, null, new File(csvPath));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;

            while ((s = stdInput.readLine()) != null) {
                LOGGER.log(Level.INFO, s);
            }

            int result = process.waitFor();
            LOGGER.log(Level.INFO, () -> "Metric process result: " + result);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "File not found in the given path", e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Method that compares percentage difference between metrics.
     *
     * @param m1 : metric one to compare
     * @param m2 : metric two to compare
     */
    public Quartet<Double, Double, Double, Double> compareMetrics(Metric m1, Metric m2) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        double abs;
        double avg;
        double[] metrics = new double[4];

        Field[] fields = m1.getClass().getDeclaredFields();
        for (int i = 3; i < 7; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            String fieldName = field.getName();
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase()
                    + fieldName.substring(1);
            Method getValue = m1.getClass().getDeclaredMethod(methodName);
            Object value1 = getValue.invoke(m1);
            Object value2 = getValue.invoke(m2);
            if (value1 != null && value2 != null) {
                abs = (double) value1 - (double) value2;
                avg = ((double) value1 + (double) value2) / 2;
                if (avg != 0)
                    metrics[i - 3] = (abs / avg) * 100;
            }
        }
        metrics[3] *= -1;
        return new Quartet<>(metrics[0], metrics[1], metrics[2], metrics[3]);
    }

    /**
     * Method that compute the metric on the given project and
     * return a new metric object as result.
     *
     * @param projectName : name of the project to analyse.
     * @param commit      : current commit on which to calculate the metric.
     * @param fileName    : csv file name to parse.
     * @return : metric object for that project.
     */
    private Metric extractMetric(String projectName, String commit, String fileName) {
        createMetricFile(projectName);
        return csvReader.readMetricsFromCSV(csvPath + "/" + fileName + ".csv", commit, projectName);
    }

    /**
     * Method that extract metrics of current commitId and compared metrics to previous version
     *
     * @param commitId    : current commit on which to calculate the comparison metric.
     * @param projectName : name of the project to analyse.
     */
    public Pair<Metric, ComparisonMetric> extractComparisonMetric(String commitId, String projectName) {
        String[] elements = projectName.split("/");
        String fileName = elements[elements.length - 1];

        GitCommand.checkoutCommit(commitId, projectName);
        Metric currentMetric = extractMetric(projectName, commitId, fileName);
        databaseService.saveMetric(currentMetric);

        String previousCommit = RepositoryAnalysis.getInstance(databaseService).getPreviousCommitId(commitId, projectName);
        GitCommand.checkoutCommit(previousCommit, projectName);
        Metric previousMetric = extractMetric(projectName, previousCommit, fileName);

        Quartet<Double, Double, Double, Double> metricComparison = null;
        try {
            metricComparison = compareMetrics(currentMetric, previousMetric);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Error occurs during the metrics comparison", e);
        }
        assert metricComparison != null;
        ComparisonMetric comparisonMetric = new ComparisonMetric(commitId, previousCommit, projectName,
                metricComparison.getFirst(), metricComparison.getSecond(), metricComparison.getThird(),
                metricComparison.getFourth());
        databaseService.saveComparisonMetric(comparisonMetric);
        GitCommand.checkoutCommit("HEAD", projectName);
        return new Pair<>(currentMetric, comparisonMetric);
    }
}
