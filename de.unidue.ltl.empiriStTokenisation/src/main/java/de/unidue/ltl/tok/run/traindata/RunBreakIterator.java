package de.unidue.ltl.tok.run.traindata;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.tok.misc.DataReader;
import de.unidue.ltl.tok.misc.Dumper;

public class RunBreakIterator
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
        String INPUT_FOLDER = "src/main/resources/trainDataRaw/";
        String OUTPUT_FOLDER = "target/trainData/dkproBreakIterator/web/";

        String[] files = new String[] { INPUT_FOLDER + "web/web_train_001.txt",
                OUTPUT_FOLDER + "/web_train_001.txt",

                INPUT_FOLDER + "web/web_train_002.txt",

                INPUT_FOLDER + "web/web_train_003.txt",

                INPUT_FOLDER + "web/web_train_004.txt",

                INPUT_FOLDER + "web/web_train_005.txt",

                INPUT_FOLDER + "web/web_train_006.txt",

                INPUT_FOLDER + "web/web_train_007.txt",

                INPUT_FOLDER + "web/web_train_008.txt",

                INPUT_FOLDER + "web/web_train_009.txt",

                INPUT_FOLDER + "web/web_train_010.txt",

                INPUT_FOLDER + "web/web_train_011.txt", };

        for (String f : files) {
            RunBreakIterator r = new RunBreakIterator();
            r.run(f, OUTPUT_FOLDER + "/" + getName(f));
        }
    }

    private static void doCmc()
        throws Exception
    {
        String INPUT_FOLDER = "src/main/resources/trainDataRaw/";
        String OUTPUT_FOLDER = "target/trainData/dkproBreakIterator/cmc/";

        String[] files = new String[] {

        INPUT_FOLDER + "cmc/cmc_train_blog_comment.txt",

        INPUT_FOLDER + "cmc/cmc_train_professional_chat.txt",

        INPUT_FOLDER + "cmc/cmc_train_social_chat.txt",

        INPUT_FOLDER + "cmc/cmc_train_twitter_1.txt",

        INPUT_FOLDER + "cmc/cmc_train_twitter_2.txt",

        INPUT_FOLDER + "cmc/cmc_train_whats_app.txt",

        INPUT_FOLDER + "cmc/cmc_train_wiki_discussion_1.txt",

        INPUT_FOLDER + "cmc/cmc_train_wiki_discussion_2.txt", };

        for (String f : files) {
            RunBreakIterator r = new RunBreakIterator();
            r.run(f, OUTPUT_FOLDER + "/" + getName(f));
        }

    }

    private static String getName(String e)
    {
        int lastIndexOf = e.lastIndexOf("/");
        return e.substring(lastIndexOf + 1);
    }

    public void run(String raw, String target)
        throws Exception
    {

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                DataReader.class, DataReader.PARAM_LANGUAGE, "de",
                DataReader.PARAM_SOURCE_LOCATION, raw);

        AnalysisEngineDescription tok = AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription dumper = AnalysisEngineFactory.createEngineDescription(
                Dumper.class, Dumper.PARAM_TARGET_FILE, target);

        SimplePipeline.runPipeline(reader, tok, dumper);
    }
}
