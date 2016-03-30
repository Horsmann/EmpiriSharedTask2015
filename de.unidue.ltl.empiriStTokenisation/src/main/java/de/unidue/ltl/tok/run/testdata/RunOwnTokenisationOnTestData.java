package de.unidue.ltl.tok.run.testdata;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.unidue.ltl.tok.EmpiriTokeniser;
import de.unidue.ltl.tok.misc.DataReader;
import de.unidue.ltl.tok.misc.ExDumper;

public class RunOwnTokenisationOnTestData
{

    public static void main(String[] args)
        throws Exception
    {
        doCmc();
        doWeb();
    }

    private static void doWeb()
        throws Exception
    {

        String WEB_INPUT_PREFIX = "src/main/resources/testDataRaw/web/";
        String WEB_OUTPUT_PREFIX = "target/testData/web_tok_LTL-UDE/";

        String[] files = new String[] { 
                "web_test_001.txt", 
                 "web_test_002.txt",
                "web_test_003.txt",
                "web_test_004.txt", "web_test_005.txt", "web_test_006.txt", "web_test_007.txt",
                "web_test_008.txt", "web_test_009.txt", "web_test_010.txt", "web_test_011.txt",
                "web_test_012.txt" 
                };

        for (String f : files) {

            CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                    DataReader.class, DataReader.PARAM_LANGUAGE, "de",
                    DataReader.PARAM_SOURCE_LOCATION, WEB_INPUT_PREFIX + "/" + f);

            AnalysisEngineDescription tagger = AnalysisEngineFactory
                    .createEngineDescription(
                            EmpiriTokeniser.class,
                            EmpiriTokeniser.PARAM_ABBREVIATION_LIST_LOCATION,
                            "src/main/resources/lists/abbreviations/list.txt",
                            EmpiriTokeniser.PARAM_APOSTHROP_JOINED_WORDS,
                            "src/main/resources/lists/apostroph_s_joinedWords/list.txt");

            AnalysisEngineDescription dumper = AnalysisEngineFactory.createEngineDescription(
                    ExDumper.class, ExDumper.PARAM_TARGET_FILE, WEB_OUTPUT_PREFIX + "/" + f);

            SimplePipeline.runPipeline(reader, tagger, dumper);
        }
    }

    private static void doCmc()
        throws Exception
    {

        String CMC_INPUT_PREFIX = "src/main/resources/testDataRaw/cmc/";
        String CMC_OUTPUT_PREFIX = "target/testData/cmc_tok_LTL-UDE/";

        String[] files = new String[] { "cmc_test_blog_comment.txt",
                "cmc_test_professional_chat.txt", "cmc_test_social_chat.txt",
                "cmc_test_twitter.txt", "cmc_test_whatsapp.txt", "cmc_test_wiki_discussion.txt" };

        for (String f : files) {

            CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                    DataReader.class, DataReader.PARAM_LANGUAGE, "de",
                    DataReader.PARAM_SOURCE_LOCATION, CMC_INPUT_PREFIX + "/" + f);

            AnalysisEngineDescription tagger = AnalysisEngineFactory
                    .createEngineDescription(
                            EmpiriTokeniser.class,
                            EmpiriTokeniser.PARAM_ABBREVIATION_LIST_LOCATION,
                            "/Users/toobee/Documents/Eclipse/lang-tech/projects/de.unidue.ltl.empiriStTok/src/main/resources/lists/abbreviations/list.txt",
                            EmpiriTokeniser.PARAM_APOSTHROP_JOINED_WORDS,
                            "/Users/toobee/Documents/Eclipse/lang-tech/projects/de.unidue.ltl.empiriStTok/src/main/resources/lists/apostroph_s_joinedWords/list.txt");

            AnalysisEngineDescription dumper = AnalysisEngineFactory.createEngineDescription(
                    ExDumper.class, ExDumper.PARAM_TARGET_FILE, CMC_OUTPUT_PREFIX + "/" + f);

            SimplePipeline.runPipeline(reader, tagger, dumper);
        }

    }

}
