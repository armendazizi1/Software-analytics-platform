package software_analytics.group2.backend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import software_analytics.group2.backend.model.Pair;
import software_analytics.group2.backend.model.Triplet;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "predictions")
public class DefectPrediction {

    @Id
    private String id;
    private String projectName;
    private String toPredictName;
    private double precision;
    private double recall;
    private List<Pair<String, Double>> predictedCommits;
    private static final Logger LOGGER = Logger.getLogger(DefectPrediction.class.getName());

    public DefectPrediction(String projectName) {
        this.projectName = projectName;
        this.toPredictName = "buggy";
    }


    public void evaluateClassifierAndPredict(String trainingFilePath, String testFilePath) {
        String balancedTrainingFilePath = buildTrainingSet(trainingFilePath);
        RandomForest classifier = evaluateClassifier(balancedTrainingFilePath);
        predictCommits(classifier, testFilePath);
    }

    /**
     * Method to predict commits being buggy given test file and classifier
     *
     * @param classifier   :   classifier used to predict
     * @param testFilePath : path to the test file which contains commits to predict
     * @return list of commits with their buggy percentage,buggy/clean
     */
    private List<Pair<String, Double>> predictCommits(RandomForest classifier, String testFilePath) {
        List<Pair<String, Double>> res = new ArrayList<>();
        ArrayList<Prediction> predictions = new ArrayList<>();
        try {
            Evaluation results = runOnTestSet(classifier, testFilePath);
            predictions = results.predictions();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while trying to evaluate the test set", e);
        }



        for (Prediction prediction : predictions) {
            String string = prediction.toString();
            String[] bits = string.split(" ");
            double buggyPercent = Double.parseDouble(bits[bits.length - 2]);

            if (prediction.predicted() == 0.0 && buggyPercent>=0.7)
                res.add(new Pair<>("buggy", buggyPercent));
            else
                res.add(new Pair<>("clean", buggyPercent));

        }

        this.predictedCommits = res;
        return res;
    }

    /**
     * Method which train the classifier
     *
     * @param balancedTrainingFilePath :  path to training file
     * @return classifier which is trained by data
     */
    private RandomForest evaluateClassifier(String balancedTrainingFilePath) {
        Instances instances = getInstanceOfTrainingSet(balancedTrainingFilePath);


        double prec = 0;
        double rec = 0;

        assert instances != null;
        int size = instances.numInstances() / 10;
        int begin = 0;
        int end = size - 1;
        RandomForest classifier = new RandomForest();

        for (int i = 1; i <= 10; i++) {

            Instances training = new Instances(instances);
            Instances testing = new Instances(instances, begin, (end - begin));

            for (int j = 0; j < (end - begin); j++) {
                training.delete(begin);
            }
            training.setClass(training.attribute(toPredictName));


            try {
                classifier.buildClassifier(training);
                Evaluation evaluation = new Evaluation(testing);
                evaluation.evaluateModel(classifier, testing);



                Triplet<Double,Double,Double> triplets=getFieldsForPrecRec(evaluation.predictions());
                double predictedbuggyOnes = triplets.getFirst();
                double actualBuggy =  triplets.getSecond();
                double correctlyClassified = triplets.getThird();
                rec += computeScore(actualBuggy, correctlyClassified);
                prec += computeScore(predictedbuggyOnes, correctlyClassified);
                begin = end + 1;
                end += size;

                if (i == 9) {
                    end = instances.numInstances();
                }

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while runinng the classifier", e);
            }
        }
        this.precision = prec * 100 / 10.0;
        this.recall = rec * 100 / 10.0;
        return classifier;

    }
    Triplet<Double,Double,Double> getFieldsForPrecRec(ArrayList<Prediction> predictions){
        double predictedbuggyOnes = 0;
        double actualBuggy = 0;
        double correctlyClassified = 0;
        for (Prediction prediction : predictions) {

            String string = prediction.toString();
            String[] bits = string.split(" ");
            double buggyPercent = Double.parseDouble(bits[bits.length - 2]);

            if (prediction.predicted() == 0.0 && buggyPercent >= 0.7) {
                predictedbuggyOnes++;
                if (prediction.predicted() == prediction.actual()) {
                    correctlyClassified++;
                }
            }
            if (prediction.actual() == 0.0) {
                actualBuggy++;
            }
        }
        return new Triplet<>(predictedbuggyOnes,actualBuggy,correctlyClassified);
    }


    /**
     * Method to compute the score of precision or recall
     *
     * @param buggy:                number of buggy commits
     * @param correctlyClassified:  number of correctly classified commits
     * @return                      the score
     */
    private double computeScore(double buggy, double correctlyClassified) {
        double value = 0;
        if (buggy == 0) {
            if (correctlyClassified == 0.0)
                value = 1;
        }
        else
            value = correctlyClassified / buggy;

        return value;
    }

    /**
     * Method to get the instances of the given training set path
     *
     * @param balancedTrainingFilePath: path of the training set file
     * @return  instance of the training set
     */
    private Instances getInstanceOfTrainingSet(String balancedTrainingFilePath) {
        FileReader frTraining = null;
        Instances instances = null;
        try {
            frTraining = new FileReader(balancedTrainingFilePath);
            instances = new Instances(frTraining);

            if (instances.classIndex() == -1)
                instances.setClassIndex(instances.numAttributes() - 1);


        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "File of balanced training set not found", e);
        }
        return instances;
    }


    /**
     * Method to build the training set
     *
     * @param trainingFilePath :  path to training data file
     * @return balanced training set
     */
    private String buildTrainingSet(String trainingFilePath) {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("true");
        categories.add("false");


        String balancedTrainingFilePath = null;
        try {
            balancedTrainingFilePath = balanceTrainingSetWithSMOTE(trainingFilePath, categories);
            if (balancedTrainingFilePath == null)
                balancedTrainingFilePath = trainingFilePath;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while trying to build the training set", e);
        }

        return balancedTrainingFilePath;

    }

    private String balanceTrainingSetWithSMOTE(String trainingFilePath, ArrayList<String> categories) throws Exception {

        FileReader frTraining = new FileReader(trainingFilePath);
        Instances instances = new Instances(frTraining);
        instances.setClass(instances.attribute(toPredictName));

        ArrayList<Integer> percentages = getPercentageOfArtificialInstancesNeeded(instances, categories);
        if (percentages.isEmpty())
            return null;

        for (int j = 0; j < percentages.size(); j++) {
            if (percentages.get(j) == 0)
                continue;

            SMOTE filter = new SMOTE();
            filter.setInputFormat(instances);
            String options = ("-C " + (j + 1) + " -K 5 -P " + percentages.get(j) + " -S 1");
            String[] optionsArray = options.split(" ");
            filter.setOptions(optionsArray);
            instances = Filter.useFilter(instances, filter);

        }

        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        File outputFile = new File(trainingFilePath.replace(".arff", "_balanced.arff"));
        saver.setFile(outputFile);
        saver.writeBatch();

        return outputFile.getAbsolutePath();

    }

    private ArrayList<Integer> getPercentageOfArtificialInstancesNeeded(Instances instances, ArrayList<String> categories) {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> numberOfInstancesPerCategory = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            numberOfInstancesPerCategory.add(0);
        }

        Iterator<Instance> iterator = instances.iterator();
        while (iterator.hasNext()) {
            Instance instance = iterator.next();
            int index = ((Double) instance.classValue()).intValue();
            numberOfInstancesPerCategory.set(index, numberOfInstancesPerCategory.get(index) + 1);
        }

        int max = 0;
        int maxIndex = -1;

        for (int i = 0; i < numberOfInstancesPerCategory.size(); i++) {
            if (numberOfInstancesPerCategory.get(i) < 6)
                return new ArrayList<>();
            if (numberOfInstancesPerCategory.get(i) > max) {
                max = numberOfInstancesPerCategory.get(i);
                maxIndex = i;
            }
        }

        for (int i = 0; i < numberOfInstancesPerCategory.size(); i++) {
            if (i == maxIndex) {
                result.add(0);
            } else {
                int percentage = ((max - numberOfInstancesPerCategory.get(i)) * 100) / numberOfInstancesPerCategory.get(i);
                result.add(percentage);
            }
        }

        return result;
    }

    /**
     * Method to evaluate test set based on trained classifier
     *
     * @param classifier   :   classifier used to predict
     * @param testFilePath : path to the test file which contains commits to predict
     * @return evaluation results
     */
    private Evaluation runOnTestSet(Classifier classifier, String testFilePath) throws Exception {

        FileReader frTest = new FileReader(testFilePath);
        Instances test = new Instances(frTest);
        test.setClass(test.attribute(toPredictName));

        StringBuffer predsBuffer = new StringBuffer();
        PlainText pt = new PlainText();
        pt.setBuffer(predsBuffer);
        pt.setHeader(test);
        Evaluation eval = new Evaluation(test);
        eval.evaluateModel(classifier, test, pt);

        return eval;
    }


}
