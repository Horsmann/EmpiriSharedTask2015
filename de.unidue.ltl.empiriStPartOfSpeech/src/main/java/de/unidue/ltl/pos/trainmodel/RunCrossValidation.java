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
import org.dkpro.tc.ml.report.BatchTrainTestReport;

import de.unidue.ltl.pos.trainmodel.misc.PreProcessing;
import de.unidue.ltl.pos.trainmodel.misc.SharedTaskReader;
import de.unidue.ltl.pos.trainmodel.misc.SplitUtil;
import de.unidue.ltl.pos.trainmodel.reports.AccuracyPerWordClass;
import de.unidue.ltl.pos.trainmodel.reports.KnownUnknownWordAnalysisReport;
import de.unidue.ltl.pos.trainmodel.reports.OverallAccuracyCollector;
import de.unidue.ltl.pos.trainmodel.tc.ExperimentPostProcTrainTest;

public class RunCrossValidation implements Constants {
	public static String corpus = null;
	public static String experimentName = "sharedTask";
	public static String languageCode = "de";
	public static Boolean useCoarse = false;

	private static String posMapping = null;
	private static String primaryData = null;;
	private static String secondaryData = null;;
	public static String homeFolder = null;

	public static Integer charMinNgram = null;
	public static Integer charMaxNgram = null;
	public static Integer charTopNgram = null;

	public static String brownCluster = null;
	public static String posDictionary = null;
	private static String morphDictionary = null;

	private static String shortDescription = null;

	static int NUM_FOLDS = -1;
	private static String nameLists;

	public static void main(String[] args) throws Exception {

		FileInputStream input = new FileInputStream(args[0]);
		Properties prop = new Properties();
		prop.load(input);

		if (args.length > 1) {
			shortDescription = args[1];
		} else {
			shortDescription = "";
		}

		primaryData = prop.getProperty("primaryData");
		secondaryData = prop.getProperty("secondaryData");

		homeFolder = prop.getProperty("homeFolder");
		System.setProperty("DKPRO_HOME", homeFolder);

		charMinNgram = Integer.valueOf(prop.getProperty("charMinNgram"));
		charMaxNgram = Integer.valueOf(prop.getProperty("charMaxNgram"));
		charTopNgram = Integer.valueOf(prop.getProperty("charTopNgram"));

		brownCluster = prop.getProperty("brownCluster");
		posDictionary = prop.getProperty("posDictionary");
		morphDictionary = prop.getProperty("morphDict");

		posMapping = prop.getProperty("posMapping");
		nameLists = prop.getProperty("namelistFolder");

		NUM_FOLDS = Integer.valueOf(prop.getProperty("numFold"));

		List<String> s = SplitUtil.splitIntoNFilesIntoTemporaryFolder(
				primaryData, ".txt", true, NUM_FOLDS);
		List<String[]> cvSplits = SplitUtil.createCVoversampleSplits(s,
				secondaryData, ".txt", 1, NUM_FOLDS);

		for (String[] run : cvSplits) {

			ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE,
					Constants.LM_SINGLE_LABEL, run[0], run[1]);

			RunCrossValidation experiment = new RunCrossValidation();
			experiment.validation(pSpace);
		}

		String timestamp = "" + System.currentTimeMillis();
		String folder = System.getProperty("user.home") + "/Desktop/";
		AccuracyPerWordClass.generateSummaryReport(folder + shortDescription
				+ "plain_avgPosPerformance" + timestamp + ".txt",
				AccuracyPerWordClass.writtenFiles_plain);
		AccuracyPerWordClass.generateSummaryReport(folder + shortDescription
				+ "post_avgPosPerformance" + timestamp + ".txt",
				AccuracyPerWordClass.writtenFiles_post);

		OverallAccuracyCollector.summarize(folder + shortDescription
				+ "plain_avgAccuracy" + timestamp + ".txt",
				OverallAccuracyCollector.writtenFiles_plain);
		OverallAccuracyCollector.summarize(folder + shortDescription
				+ "post_avgAccuracy" + timestamp + ".txt",
				OverallAccuracyCollector.writtenFiles_post);

		KnownUnknownWordAnalysisReport.generateSummaryReport(folder
				+ shortDescription + "plain_avgUnknownVocab" + timestamp
				+ ".txt", KnownUnknownWordAnalysisReport.plain_oov_all, "OOV");
		KnownUnknownWordAnalysisReport.generateSummaryReport(
				folder + shortDescription + "plain_avgKnownVocab" + timestamp
						+ ".txt", KnownUnknownWordAnalysisReport.plain_iv_all,
				"IV");
		KnownUnknownWordAnalysisReport.generateSummaryReport(folder
				+ shortDescription + "post_avgUnknownVocab" + timestamp
				+ ".txt", KnownUnknownWordAnalysisReport.post_oov_all, "OOV");
		KnownUnknownWordAnalysisReport.generateSummaryReport(folder
				+ shortDescription + "post_avgKnownVocab" + timestamp + ".txt",
				KnownUnknownWordAnalysisReport.post_iv_all, "IV");
	}

	@SuppressWarnings("unchecked")
	public static ParameterSpace getParameterSpace(String featureMode,
			String learningMode, String train, String test) throws Exception {

		// configure training and test data reader dimension
		Map<String, Object> dimReaders = new HashMap<String, Object>();

		Dimension<List<String>> dimFeatureSets = Features.getFeatures();

		Dimension<List<String>> dimClassificationArgs = Dimension
				.create(DIM_CLASSIFICATION_ARGS,
						asList(new String[] { CRFSuiteAdapter.ALGORITHM_ADAPTIVE_REGULARIZATION_OF_WEIGHT_VECTOR }));

		Dimension<List<Object>> dimPipelineParameters = Features
				.getFeatureParams(charMinNgram, charMaxNgram, charTopNgram,
						brownCluster, posDictionary,
						morphDictionary);

		dimReaders.put(DIM_READER_TRAIN, SharedTaskReader.class);
		dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(
				SharedTaskReader.PARAM_LANGUAGE, languageCode,
				SharedTaskReader.PARAM_SOURCE_LOCATION, train,
				SharedTaskReader.PARAM_POS_MAPPING_LOCATION, posMapping,
				SharedTaskReader.PARAM_PATTERNS, "*.txt"));

		dimReaders.put(DIM_READER_TEST, SharedTaskReader.class);
		dimReaders.put(DIM_READER_TEST_PARAMS, Arrays.asList(
				SharedTaskReader.PARAM_LANGUAGE, languageCode,
				SharedTaskReader.PARAM_SOURCE_LOCATION, test,
				SharedTaskReader.PARAM_POS_MAPPING_LOCATION, posMapping,
				SharedTaskReader.PARAM_PATTERNS, "*.txt"));

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle(
				"readers", dimReaders), Dimension.create(DIM_LEARNING_MODE,
				learningMode), Dimension.create(DIM_FEATURE_MODE, featureMode),
				dimPipelineParameters, dimFeatureSets, dimClassificationArgs);

		return pSpace;
	}

	protected void validation(ParameterSpace pSpace) throws Exception {

		ExperimentPostProcTrainTest batch = new ExperimentPostProcTrainTest(
				experimentName, CRFSuiteAdapter.class, nameLists, posMapping);
		// ExperimentTrainTest batch = new ExperimentTrainTest(experimentName,
		// CRFSuiteAdapter.class);
		batch.setParameterSpace(pSpace);
		batch.setPreprocessing(PreProcessing.getPreprocessing());
		batch.addReport(BatchTrainTestReport.class);
		batch.addReport(AccuracyPerWordClass.class);
		batch.addReport(OverallAccuracyCollector.class);
		batch.addReport(KnownUnknownWordAnalysisReport.class);

		// Run
		Lab.getInstance().run(batch);
	}

}
