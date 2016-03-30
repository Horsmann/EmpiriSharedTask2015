package de.unidue.ltl.tok.run.testdata;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTokenizer;
import de.unidue.ltl.tok.misc.DataReader;
import de.unidue.ltl.tok.misc.Dumper;

public class RunArkTokenizerOnTestData
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
        String INPUT_FOLDER = "src/main/resources/testDataRaw/";
        String OUTPUT_FOLDER = "target/testData/arktoolsTokenisation/";

        String[] pairs = new String[] { INPUT_FOLDER + "web/web_test_001.txt",

        INPUT_FOLDER + "web/web_test_002.txt",

        INPUT_FOLDER + "web/web_test_003.txt",

        INPUT_FOLDER + "web/web_test_004.txt",

        INPUT_FOLDER + "web/web_test_005.txt",

        INPUT_FOLDER + "web/web_test_006.txt",

        INPUT_FOLDER + "web/web_test_007.txt",

        INPUT_FOLDER + "web/web_test_008.txt",

        INPUT_FOLDER + "web/web_test_009.txt",

        INPUT_FOLDER + "web/web_test_010.txt",

        INPUT_FOLDER + "web/web_test_011.txt",
        
        INPUT_FOLDER + "web/web_test_012.txt",
        };

        for (String f : pairs) {
            RunArkTokenizerOnTestData r = new RunArkTokenizerOnTestData();
            r.run(f, OUTPUT_FOLDER + "/" + getName(f));
        }
    }

    private static void doCmc()
        throws Exception
    {
        String INPUT_FOLDER = "src/main/resources/testDataRaw/";
        String OUTPUT_FOLDER = "target/testData/arktoolsTokenisation/";

        String[] files = new String[] {

        INPUT_FOLDER + "cmc/cmc_test_blog_comment.txt",

        INPUT_FOLDER + "cmc/cmc_test_professional_chat.txt",

        INPUT_FOLDER + "cmc/cmc_test_social_chat.txt",

        INPUT_FOLDER + "cmc/cmc_test_twitter.txt",

        INPUT_FOLDER + "cmc/cmc_test_whatsapp.txt",

        INPUT_FOLDER + "cmc/cmc_test_wiki_discussion.txt",
  };

        for (String f : files) {
            RunArkTokenizerOnTestData r = new RunArkTokenizerOnTestData();
            r.run(f, OUTPUT_FOLDER + "/" + getName(f));
        }

    }

    private static String getName(String e)
    {
        int lastIndexOf = e.lastIndexOf("/");
        return e.substring(lastIndexOf + 1);
    }

    public void run(String raw,  String target)
        throws Exception
    {

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                DataReader.class, DataReader.PARAM_LANGUAGE, "de",
                DataReader.PARAM_SOURCE_LOCATION, raw);

        AnalysisEngineDescription tok = AnalysisEngineFactory
                .createEngineDescription(ArktweetTokenizer.class);

        AnalysisEngineDescription dumper = AnalysisEngineFactory.createEngineDescription(
                Dumper.class, Dumper.PARAM_TARGET_FILE, target);

        SimplePipeline.runPipeline(reader, tok, dumper);
    }
}
