package de.unidue.ltl.pos.trainmodel.tagger.eval.run;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.performance.Stopwatch;
import de.unidue.ltl.pos.trainmodel.tagger.eval.CSVPrinter;
import de.unidue.ltl.pos.trainmodel.tagger.eval.ConfusionMatrixReport;
import de.unidue.ltl.pos.trainmodel.tagger.eval.GermanReader;
import de.unidue.ltl.pos.trainmodel.tagger.eval.GermanTaggers;
import de.unidue.ltl.pos.trainmodel.tagger.eval.GoldPOSAnnotator;
import de.unidue.ltl.pos.trainmodel.tagger.eval.ReaderConfiguration;
import de.unidue.ltl.pos.trainmodel.tagger.eval.ResultWriter;
import de.unidue.ltl.pos.trainmodel.tagger.eval.SequenceReportHTML;
import de.unidue.ltl.pos.trainmodel.tagger.eval.SequenceReportText;
import de.unidue.ltl.pos.trainmodel.tagger.eval.TaggerConfiguration;
import de.unidue.ltl.pos.trainmodel.tagger.eval.TopConfusedLexemeReport;

public class TaggerEvaluation
{
    public static String OUTPUT_FOLDER = "target/evaluationOut/";

    public static void main(String[] args)
        throws Exception
    {

        new File(OUTPUT_FOLDER).mkdirs();

//        String testFolder = "src/main/resources/testGoldData/**/";
        String testFolder = "src/main/resources/modeltrain/data/empiri";
        
        String mapping = "src/main/resources/de-stts-pos.map";

        List<ReaderConfiguration> corporaDe = new ArrayList<ReaderConfiguration>();
        corporaDe.add(GermanReader.getShrdTaskTestData(testFolder, mapping));

        List<TaggerConfiguration> taggersDe = new ArrayList<TaggerConfiguration>();
        taggersDe.addAll(GermanTaggers.getTreeTaggerModel(mapping));
         taggersDe.addAll(GermanTaggers.getTreeTaggerWithPostProcessingModel(mapping));
        taggersDe.addAll(GermanTaggers.getMajorityTagger(mapping));
//        taggersDe.addAll(GermanTaggers.getEmpiriTrainedModel(mapping));
//        taggersDe.addAll(GermanTaggers.getEmpiriTrainedModelWithPostProcessing(mapping));

        evaluate(corporaDe, taggersDe, "deAll");
    }

    private static void evaluate(List<ReaderConfiguration> readers,
            List<TaggerConfiguration> taggers, String experimentName)
        throws Exception
    {
        HashMap<String, List<String[]>> map = new HashMap<String, List<String[]>>();
        for (ReaderConfiguration corpus : readers) {

            String uuid = new Timestamp(new Date().getTime()).toString();

            CollectionReaderDescription reader = corpus.makeReader();
            for (TaggerConfiguration tagger : taggers) {
                AnalysisEngineDescription taggerEngineDescription = tagger.makeEngine();

                String humanReadable = tagger.humanRedableName + "-on-" + corpus.humanReadableName;
                System.out.println("################################");
                System.out.println("START: " + humanReadable);
                String detailFile = OUTPUT_FOLDER + uuid + "_" + humanReadable + "_zDetail.txt";
                String timerFile = OUTPUT_FOLDER + uuid + "_" + humanReadable + "_timer.txt";
                String accFile = OUTPUT_FOLDER + uuid + "_" + humanReadable + "_acc.txt";

                SimplePipeline.runPipeline(
                        reader,
                        createEngineDescription(GoldPOSAnnotator.class),
                        createEngineDescription(Stopwatch.class, Stopwatch.PARAM_TIMER_NAME, "t1"),
                        createEngineDescription(taggerEngineDescription),
                        createEngineDescription(Stopwatch.class, Stopwatch.PARAM_TIMER_NAME, "t1",
                                Stopwatch.PARAM_OUTPUT_FILE, timerFile),
                        createEngineDescription(SequenceReportHTML.class,
                                SequenceReportHTML.PARAM_FILE_GROUP_KEY, uuid,
                                SequenceReportHTML.PARAM_FILE_USER_KEY, humanReadable,
                                SequenceReportHTML.PARAM_OUTPUT_FOLDER, OUTPUT_FOLDER),
                        createEngineDescription(SequenceReportText.class,
                                SequenceReportText.PARAM_FILE_GROUP_KEY, uuid,
                                SequenceReportText.PARAM_FILE_USER_KEY, humanReadable,
                                SequenceReportText.PARAM_OUTPUT_FOLDER, OUTPUT_FOLDER),
                        createEngineDescription(ConfusionMatrixReport.class,
                                ConfusionMatrixReport.PARAM_FILE_GROUP_KEY, uuid,
                                ConfusionMatrixReport.PARAM_FILE_USER_KEY, humanReadable,
                                ConfusionMatrixReport.PARAM_OUTPUT_FOLDER, OUTPUT_FOLDER),
                        createEngineDescription(TopConfusedLexemeReport.class,
                                TopConfusedLexemeReport.PARAM_FILE_GROUP_KEY, uuid,
                                TopConfusedLexemeReport.PARAM_FILE_USER_KEY, humanReadable,
                                TopConfusedLexemeReport.PARAM_OUTPUT_FOLDER, OUTPUT_FOLDER),
                        createEngineDescription(ResultWriter.class,
                                ResultWriter.PARAM_OUTPUT_FILE_NAME, detailFile,
                                ResultWriter.PARAM_PROP_FILE, accFile,
                                ResultWriter.PARAM_TIMER_FILE, timerFile,
                                ResultWriter.PARAM_CORPUS_NAME, corpus.humanReadableName,
                                ResultWriter.PARAM_TOOL_NAME, tagger.humanRedableName));

                outputTimes(timerFile);
            }

            List<String[]> resultOverview = getResultOverview(uuid);
            Collections.sort(resultOverview, new Comparator<String[]>()
            {

                public int compare(String[] o1, String[] o2)
                {
                    return o1[0].compareTo(o2[0]);
                }
            });
            map.put(corpus.humanReadableName, resultOverview);
        }

        CSVPrinter.createCSV(OUTPUT_FOLDER, experimentName, map);
    }

    private static void outputTimes(String timerFile)
        throws IOException
    {
        FileInputStream fileInput = new FileInputStream(new File(timerFile));
        Properties properties = new Properties();
        properties.load(fileInput);
        fileInput.close();

        Map<String, Double> results = new HashMap<String, Double>();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            results.put(key, Double.valueOf(value));
        }

        DecimalFormat df = new DecimalFormat("0.00");

        System.out.println();
        System.out.println("time:    " + df.format(results.get(Stopwatch.KEY_SUM)) + " s");
        System.out.println("avg:     " + df.format(results.get(Stopwatch.KEY_MEAN)) + " s");
        System.out.println("std-dev: " + df.format(results.get(Stopwatch.KEY_STDDEV)) + " s");
    }

    private static List<String[]> getResultOverview(final String uuid)
        throws IOException
    {
        File target = new File(OUTPUT_FOLDER + "/");
        File[] files = target.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith(uuid) && name.endsWith("acc.txt");
            }
        });

        List<String[]> perTaggerResults = new ArrayList<String[]>();
        for (File resultFile : files) {

            FileInputStream fileInput = new FileInputStream(resultFile);
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();

            String tool = properties.getProperty(ResultWriter.TOOL);
            String accCoarse = properties.getProperty(ResultWriter.ACC_COARSE);
            String time = properties.getProperty(ResultWriter.TIME);

            perTaggerResults.add(new String[] { tool, accCoarse.replace(".", ","),
                    time.replace(".", ",") });

        }
        return perTaggerResults;

    }
}
