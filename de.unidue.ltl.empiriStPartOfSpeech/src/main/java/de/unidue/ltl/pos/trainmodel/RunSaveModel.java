package de.unidue.ltl.pos.trainmodel;

import static java.util.Arrays.asList;

import java.io.File;
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
import org.dkpro.tc.ml.ExperimentSaveModel;

import de.unidue.ltl.pos.trainmodel.misc.PreProcessing;
import de.unidue.ltl.pos.trainmodel.misc.SharedTaskReader;

public class RunSaveModel
    implements Constants
{
    public static String trainFolder = null;
    public static String homeFolder = null;

    public static Integer charMinNgram = null;
    public static Integer charMaxNgram = null;
    public static Integer charTopNgram = null;

    public static String brownCluster = null;
    public static String w2vClassCluster = null;
    public static String posDictionary = null;
    private static String morphDictionary = null;

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

        trainFolder = prop.getProperty("allData");

        homeFolder = prop.getProperty("homeFolder");

        charMinNgram = Integer.valueOf(prop.getProperty("charMinNgram"));
        charMaxNgram = Integer.valueOf(prop.getProperty("charMaxNgram"));
        charTopNgram = Integer.valueOf(prop.getProperty("charTopNgram"));

        brownCluster = prop.getProperty("brownCluster");
        posDictionary = prop.getProperty("posDictionary");
        morphDictionary = prop.getProperty("morphDict");

        posMapping = prop.getProperty("posMapping");

        System.setProperty("DKPRO_HOME", homeFolder);

        ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE, Constants.LM_SINGLE_LABEL);

        RunSaveModel experiment = new RunSaveModel();
        experiment.validation(pSpace);
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

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, learningMode), Dimension.create(
                        DIM_FEATURE_MODE, featureMode), dimPipelineParameters, dimFeatureSets,
                dimClassificationArgs);

        return pSpace;
    }

    protected void validation(ParameterSpace pSpace)
        throws Exception
    {
        ExperimentSaveModel batch = new ExperimentSaveModel(experimentName, CRFSuiteAdapter.class,
                new File(System.getProperty("user.home") + "/Desktop/shrdTaskModel"));
        batch.setPreprocessing(PreProcessing.getPreprocessing());
        batch.setParameterSpace(pSpace);

        // Run
        Lab.getInstance().run(batch);
    }

}
