package de.unidue.ltl.pos.trainmodel.tagtestdata;

import java.io.File;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.flextag.FlexTag;
import de.unidue.ltl.pos.trainmodel.tagger.ComboTagger;

public class RunPos {

	public static void main(String[] args) throws Exception {
		doCmc(1, FlexTag.class);
		doWeb(1, FlexTag.class);

		doCmc(2, ComboTagger.class);
		doWeb(2, ComboTagger.class);
	}

	private static void doWeb(int runId,
			Class<? extends AnalysisComponent> taggerClass) throws Exception {
		String INPUT_PREFIX = "src/main/resources/testRawData/web/";
		String OUTPUT_PREFIX = "target/"
				+ "web_pos_LTL-UDE_run" + runId;
		new File(OUTPUT_PREFIX).mkdirs();

		String[] files = new String[] { "web_test_001.txt", "web_test_002.txt",
				"web_test_003.txt", "web_test_004.txt", "web_test_005.txt",
				"web_test_006.txt", "web_test_007.txt", "web_test_008.txt",
				"web_test_009.txt", "web_test_010.txt", "web_test_011.txt",
				"web_test_012.txt" };

		for (String f : files) {

			CollectionReaderDescription reader = CollectionReaderFactory
					.createReaderDescription(RawTestDataReader.class,
							RawTestDataReader.PARAM_LANGUAGE, "de",
							RawTestDataReader.PARAM_SOURCE_LOCATION,
							INPUT_PREFIX + "/" + f);

			AnalysisEngineDescription tagger = AnalysisEngineFactory
					.createEngineDescription(taggerClass,
							ComponentParameters.PARAM_LANGUAGE, "de",
							ComponentParameters.PARAM_VARIANT, "empiri");

			AnalysisEngineDescription dumper = AnalysisEngineFactory
					.createEngineDescription(PredictionWriter.class,
							PredictionWriter.PARAM_TARGET_FILE, OUTPUT_PREFIX
									+ "/" + f);

			SimplePipeline.runPipeline(reader, tagger, dumper);
		}
	}

	private static void doCmc(int runId,
			Class<? extends AnalysisComponent> taggerClass) throws Exception {

		String INPUT_PREFIX = "src/main/resources/testRawData/cmc/";
		String OUTPUT_PREFIX = "target/"
				+ "cmc_pos_LTL-UDE_run" + runId;
		new File(OUTPUT_PREFIX).mkdirs();

		String[] files = new String[] { "cmc_test_blog_comment.txt",
				"cmc_test_professional_chat.txt", "cmc_test_social_chat.txt",
				"cmc_test_twitter.txt", "cmc_test_whatsapp.txt",
				"cmc_test_wiki_discussion.txt" };

		for (String f : files) {

			CollectionReaderDescription reader = CollectionReaderFactory
					.createReaderDescription(RawTestDataReader.class,
							RawTestDataReader.PARAM_LANGUAGE, "de",
							RawTestDataReader.PARAM_SOURCE_LOCATION,
							INPUT_PREFIX + "/" + f);

			AnalysisEngineDescription tagger = AnalysisEngineFactory
					.createEngineDescription(taggerClass,
							ComponentParameters.PARAM_LANGUAGE, "de",
							ComponentParameters.PARAM_VARIANT, "empiri");

			AnalysisEngineDescription dumper = AnalysisEngineFactory
					.createEngineDescription(PredictionWriter.class,
							PredictionWriter.PARAM_TARGET_FILE, OUTPUT_PREFIX
									+ "/" + f);

			SimplePipeline.runPipeline(reader, tagger, dumper);
		}
	}

}
