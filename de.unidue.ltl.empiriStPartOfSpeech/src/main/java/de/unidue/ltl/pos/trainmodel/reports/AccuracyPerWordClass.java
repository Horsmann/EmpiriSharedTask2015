package de.unidue.ltl.pos.trainmodel.reports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

import de.unidue.ltl.pos.trainmodel.tc.PostPosUpdateTask;

public class AccuracyPerWordClass
    extends BatchReportBase
    implements Constants
{

    static Logger log = Logger.getLogger(AccuracyPerWordClass.class);

    public static List<File> writtenFiles_plain = new ArrayList<>();
    public static List<File> writtenFiles_post = new ArrayList<>();

    static String OUTPUT_FILE_PLAIN = "wordClassPerformance_plain.txt";
    static String OUTPUT_FILE_POST = "wordClassPerformance_post.txt";

    public void execute()
        throws Exception
    {

        for (TaskContextMetadata subcontext : getSubtasks()) {
            String type = subcontext.getType();
            if (type.contains(CRFSuiteTestTask.class.getName())
                    || type.contains(PostPosUpdateTask.class.getName())) {
                StorageService storageService = getContext().getStorageService();
                File locateKey = storageService.locateKey(subcontext.getId(),
                        Constants.ID_OUTCOME_KEY);
                
                if(!locateKey.exists()){
                    log.warn("File ["+locateKey+"] does not exist - skip");
                    continue;
                }

                String report = generateWordClassReport(locateKey);

                if (type.contains(CRFSuiteTestTask.class.getName())) {
                    File targetFile = storageService.locateKey(subcontext.getId(),
                            OUTPUT_FILE_PLAIN);
                    FileUtils.writeStringToFile(targetFile, report);
                    writtenFiles_plain.add(targetFile);
                }
                else if (type.contains(PostPosUpdateTask.class.getName())) {
                    File targetFile = storageService
                            .locateKey(subcontext.getId(), OUTPUT_FILE_POST);
                    FileUtils.writeStringToFile(targetFile, report);
                    writtenFiles_post.add(targetFile);
                }
            }
        }
    }

    private String generateWordClassReport(File locateKey)
        throws IOException
    {
        
        Map<String, WordClass> wcp = getWcPerformances(locateKey);

        StringBuilder sb = new StringBuilder();
        List<String> keySet = new ArrayList<String>(wcp.keySet());
        Collections.sort(keySet);

        sb.append(String.format("#%10s\t%5s\t%5s\n", "Class", "Occr", "Accr"));
        for (String k : keySet) {
            WordClass wc = wcp.get(k);
            double accuracy = wc.getCorrect() / wc.getN() * 100;

            sb.append(String.format("%10s", k) + "\t" + String.format("%5d", wc.getN().intValue())
                    + "\t" + String.format("%5.1f", accuracy));
            sb.append("\n");
        }

        return sb.toString();

    }

    private Map<String, WordClass> getWcPerformances(File locateKey)
        throws IOException
    {
        Map<String, WordClass> wcp = new HashMap<>();

        List<String> lines = FileUtils.readLines(locateKey);
        Map<String, String> labels = getLabels(lines);

        for (String l : lines) {
            if (l.startsWith("#")) {
                continue;
            }
            String[] entry = splitAtFirstEqualSignRightHandSide(l);

            String pg = entry[1];
            String[] split = pg.split(";");

            if (split.length < 2) {
                System.out.println("ERROR\t" + l);
                continue;
            }

            String prediction = labels.get(split[0]);
            String gold = labels.get(split[1]);

            WordClass wordClass = wcp.get(gold);
            if (wordClass == null) {
                wordClass = new WordClass();
            }
            if (gold.equals(prediction)) {
                wordClass.correct();
            }
            else {
                wordClass.incorrect();
            }
            wcp.put(gold, wordClass);
        }
        return wcp;
    }

    private String[] splitAtFirstEqualSignRightHandSide(String l)
    {
        int equal = l.lastIndexOf("=");
        String lhs = l.substring(0, equal);
        String rhs = l.substring(equal + 1);

        return new String[] { lhs, rhs };
    }

    private Map<String, String> getLabels(List<String> lines)
    {
        for (String s : lines) {
            if (s.startsWith("#labels")) {
                return extractIdLabelMap(s);
            }
        }

        return null;
    }

    private Map<String, String> extractIdLabelMap(String s)
    {
        String wc = s.replaceAll("#labels ", "");

        String[] units = wc.split(" ");

        if (units.length < 2) {
            log.warn("Splitting the line [" + s + "] did not work as expected");
        }

        Map<String, String> id2label = new HashMap<>();

        for (String u : units) {
            String[] split = u.split("=");
            id2label.put(split[0], split[1]);
        }

        return id2label;
    }

    class WordClass
    {
        double correct = 0;
        double incorrect = 0;

        public Double getN()
        {
            return correct + incorrect;
        }

        public Double getCorrect()
        {
            return correct;
        }

        public void correct()
        {
            correct++;
        }

        public void incorrect()
        {
            incorrect++;
        }
    }

    public static void generateSummaryReport(String output, List<File> files)
        throws IOException
    {
        Map<String, Double> accPerformance = new HashMap<>();
        Map<String, Double> freqOccur = new HashMap<>();

        for (File f : files) {
            log.info("Processing: " + f.getAbsolutePath());
            List<String> lines = FileUtils.readLines(f);
            for (String l : lines) {
                if (l.startsWith("#")) {
                    continue;
                }

                String[] split = l.split("\t");
                String label = split[0].trim();
                Double freq = Double.valueOf(split[1]);
                Double acc = Double.valueOf(split[2].replaceAll(",", "."));

                accPerformance = add(label, acc, accPerformance);
                freqOccur = add(label, freq, freqOccur);
            }
        }

        List<String> list = new ArrayList<String>(accPerformance.keySet());
        Collections.sort(list);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("#%10s\t%5s\t%5s\n", "Class", "Occr", "Acc"));
        for (String k : list) {
            Double val = accPerformance.get(k);
            val = val / files.size();

            Double freq = freqOccur.get(k);
            freq = freq / files.size();

            sb.append(String.format("%10s", k) + "\t" + String.format("%5d", freq.intValue())
                    + "\t" + String.format("%5.1f", val));
            sb.append("\n");
        }
        FileUtils.writeStringToFile(new File(output), sb.toString());
    }

    private static Map<String, Double> add(String label, Double val, Map<String, Double> map)
    {
        Double history = map.get(label);
        if (history == null) {
            history = val;
        }
        else {
            history += val;
        }
        map.put(label, history);
        return map;
    }
}
