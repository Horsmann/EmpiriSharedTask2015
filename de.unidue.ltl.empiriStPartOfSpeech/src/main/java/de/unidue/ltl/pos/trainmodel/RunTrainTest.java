package de.unidue.ltl.pos.trainmodel;

import static java.util.Arrays.asList;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;

import de.unidue.ltl.pos.trainmodel.misc.PreProcessing;
import de.unidue.ltl.pos.trainmodel.misc.SharedTaskReader;
import de.unidue.ltl.pos.trainmodel.reports.AccuracyPerWordClass;
import de.unidue.ltl.pos.trainmodel.reports.KnownUnknownWordAnalysisReport;
import de.unidue.ltl.pos.trainmodel.reports.OverallAccuracyCollector;
import de.unidue.ltl.pos.trainmodel.tc.ExperimentPostProcTrainTest;

/**
 * Trains the model on the training data and evaluates it on the test data We train a single
 * classifier on both data sets (cmc and web) and also evaluate them on both data sets
 */
public class RunTrainTest
    implements Constants
{
    public static String trainFolder = null;
    public static String testFolder = null;
    public static String homeFolder = null;

    public static Integer charMinNgram = null;
    public static Integer charMaxNgram = null;
    public static Integer charTopNgram = null;

    public static String brownCluster = null;
    public static String w2vClassCluster = null;
    public static String posDictionary = null;
    private static String morphDictionary = null;
    
    private static String namelist= null;

    public static String experimentName = "sharedTask";
    public static String languageCode = "de";
    public static Boolean useCoarse = false;
    private static String posMapping = null;

    public static void main(String[] args)
        throws Exception
    {

        FileInputStream input = new FileInputStream(args[0]);
        Properties prop = new Properties();
        prop.load(input);

        trainFolder = prop.getProperty("train");
        testFolder = prop.getProperty("test");

        homeFolder = prop.getProperty("homeFolder");
        namelist = prop.getProperty("namelistFolder");

        charMinNgram = Integer.valueOf(prop.getProperty("charMinNgram"));
        charMaxNgram = Integer.valueOf(prop.getProperty("charMaxNgram"));
        charTopNgram = Integer.valueOf(prop.getProperty("charTopNgram"));

        brownCluster = prop.getProperty("brownCluster");
        posDictionary = prop.getProperty("posDictionary");
        morphDictionary = prop.getProperty("morphDict");

        posMapping = prop.getProperty("posMapping");

        System.setProperty("DKPRO_HOME", homeFolder);

        ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL);

        RunTrainTest experiment = new RunTrainTest();
        experiment.validation(pSpace);

        OverallAccuracyCollector.summarize(System.getProperty("user.home")
                + "/Desktop/plain_avgAcc.txt", OverallAccuracyCollector.writtenFiles_plain);
        OverallAccuracyCollector.summarize(System.getProperty("user.home")
                + "/Desktop/pos_avgAcc.txt", OverallAccuracyCollector.writtenFiles_post);

        AccuracyPerWordClass.generateSummaryReport(System.getProperty("user.home")
                + "/Desktop/plain_wordclass.txt", AccuracyPerWordClass.writtenFiles_plain);
        AccuracyPerWordClass.generateSummaryReport(System.getProperty("user.home")
                + "/Desktop/post_wordclass.txt", AccuracyPerWordClass.writtenFiles_post);
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(String featureMode, String learningMode)
        throws Exception
    {

        // configure training and test data reader dimension
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        Dimension<List<String>> dimFeatureSets = Features.getFeatures();

        Dimension<List<String>> dimClassificationArgs = Dimension
                .create(DIM_CLASSIFICATION_ARGS,
                        asList(new String[] { CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR }));

        Dimension<List<Object>> dimPipelineParameters = Features.getFeatureParams(charMinNgram,
                charMaxNgram, charTopNgram, brownCluster, posDictionary, morphDictionary);

        dimReaders.put(DIM_READER_TRAIN, SharedTaskReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(SharedTaskReader.PARAM_LANGUAGE,
                languageCode, SharedTaskReader.PARAM_SOURCE_LOCATION, trainFolder,
                SharedTaskReader.PARAM_POS_MAPPING_LOCATION, posMapping,
                SharedTaskReader.PARAM_PATTERNS, "*.txt"));

        dimReaders.put(DIM_READER_TEST, SharedTaskReader.class);
        dimReaders.put(DIM_READER_TEST_PARAMS, Arrays.asList(SharedTaskReader.PARAM_LANGUAGE,
                languageCode, SharedTaskReader.PARAM_SOURCE_LOCATION, testFolder,
                SharedTaskReader.PARAM_POS_MAPPING_LOCATION, posMapping,
                SharedTaskReader.PARAM_PATTERNS, "*.txt"));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, learningMode), Dimension.create(
                        DIM_FEATURE_MODE, featureMode), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    protected void validation(ParameterSpace pSpace)
        throws Exception
    {
        ExperimentPostProcTrainTest batch = new ExperimentPostProcTrainTest(experimentName,
                CRFSuiteAdapter.class,namelist,posMapping);
        batch.setPreprocessing(PreProcessing.getPreprocessing());
        batch.addReport(AccuracyPerWordClass.class);
        batch.addReport(KnownUnknownWordAnalysisReport.class);
        batch.addReport(OverallAccuracyCollector.class);
        batch.setParameterSpace(pSpace);

        // Run
        Lab.getInstance().run(batch);
    }

}
