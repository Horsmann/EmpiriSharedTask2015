package de.unidue.ltl.pos.learningcurve;

import static java.util.Arrays.asList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dkpro.lab.Lab;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.util.ReportConstants;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.ml.ExperimentTrainTest;

import de.unidue.ltl.pos.trainmodel.Features;
import de.unidue.ltl.pos.trainmodel.misc.PreProcessing;
import de.unidue.ltl.pos.trainmodel.reports.OverallAccuracyCollector;
import de.unidue.ltl.pos.trainmodel.tc.ExperimentPostProcTrainTest;

/**
 * Trains a learning curve over both, training and test data
 */
public class RunLearningCurve
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

    private static String namelist = null;

    public static String experimentName = "sharedTask";
    public static String languageCode = "de";
    public static Boolean useCoarse = false;
    private static String posMapping = null;

    public static void main(String[] args)
        throws Exception
    {
        FileInputStream input = new FileInputStream("src/main/resources/traintest.properties");
        Properties prop = new Properties();
        prop.load(input);
        input.close();

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
        
        int NUM_FOLDS=10;

        List<String> files = SplitUtil.splitIntoNFilesIntoTemporaryFolder(
                "src/main/resources/learningcurve/", ".txt", true, NUM_FOLDS);
        List<List<String>> lc = SplitUtil.createLearningCurvesplits(files);

        for (int i = 1; i < lc.get(0).size(); i++) {
            for (int k = 0; k < lc.size(); k++) {
                String test = lc.get(k).get(0);
                String train = lc.get(k).get(i);
                
                ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE,
                        Constants.LM_SINGLE_LABEL, train, test);
                RunLearningCurve experiment = new RunLearningCurve();
                experiment.validation(pSpace);
            }
            System.out.println();
            if (!OverallAccuracyCollector.writtenFiles_plain.isEmpty()) {
                evaluateResults(OverallAccuracyCollector.writtenFiles_plain, new File(
                        "target/learningCurvePlain.txt"), i);
            }
            if (!OverallAccuracyCollector.writtenFiles_post.isEmpty()) {
                evaluateResults(OverallAccuracyCollector.writtenFiles_post, new File(
                        "target/learningCurvePost.txt"), i);
            }
            OverallAccuracyCollector.writtenFiles_plain = new ArrayList<File>();
            OverallAccuracyCollector.writtenFiles_post = new ArrayList<File>();
        }
    }

    private static void evaluateResults(List<File> result, File out, int counter)
        throws Exception
    {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out,
                true), "utf-8"));

        Double total = 0.0;
        for (File f : result) {
            FileInputStream input = new FileInputStream(f);
            Properties prop = new Properties();
            prop.load(input);
            input.close();
            Double val = Double.valueOf((String) prop.get(ReportConstants.PCT_CORRECT));
            total += val;
        }
        total = total / result.size();
        bw.write("Set" + counter + " = ~" + total);
        bw.write("\n");

        bw.close();
    }

    @SuppressWarnings("unchecked")
    public static ParameterSpace getParameterSpace(String featureMode, String learningMode,
            String train, String test)
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

        dimReaders.put(DIM_READER_TRAIN, LineTokenTagReader.class);
        dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(LineTokenTagReader.PARAM_LANGUAGE,
                languageCode, LineTokenTagReader.PARAM_SOURCE_LOCATION, train,
                LineTokenTagReader.PARAM_POS_MAPPING_LOCATION, posMapping,
                LineTokenTagReader.PARAM_PATTERNS, "*.data"));

        dimReaders.put(DIM_READER_TEST, LineTokenTagReader.class);
        dimReaders.put(DIM_READER_TEST_PARAMS, Arrays.asList(LineTokenTagReader.PARAM_LANGUAGE,
                languageCode, LineTokenTagReader.PARAM_SOURCE_LOCATION, test,
                LineTokenTagReader.PARAM_POS_MAPPING_LOCATION, posMapping,
                LineTokenTagReader.PARAM_PATTERNS, "*.data"));

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
//        ExperimentTrainTest batch = new ExperimentTrainTest(experimentName, CRFSuiteAdapter.class);
        batch.setPreprocessing(PreProcessing.getPreprocessing());
        batch.addReport(OverallAccuracyCollector.class);
        batch.setParameterSpace(pSpace);

        // Run
        Lab.getInstance().run(batch);
    }

}
