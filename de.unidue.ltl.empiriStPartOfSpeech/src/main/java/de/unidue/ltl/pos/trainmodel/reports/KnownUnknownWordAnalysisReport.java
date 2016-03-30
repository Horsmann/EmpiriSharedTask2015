package de.unidue.ltl.pos.trainmodel.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

import de.unidue.ltl.pos.trainmodel.tc.PostPosUpdateTask;

public class KnownUnknownWordAnalysisReport
    extends BatchReportBase
    implements Constants
{

    static String OOV_STAT_FILE = "oovPerformance.txt";
    static String IV_STAT_FILE = "ivPerformance.txt";

    static String ACCURACY_TEXT = "Accuracy: ";

    public static List<File> plain_oov_all = new ArrayList<>();
    public static List<File> plain_iv_all = new ArrayList<>();
    
    public static List<File> post_oov_all = new ArrayList<>();
    public static List<File> post_iv_all = new ArrayList<>();

    Set<String> training;

    List<String> testToken = new ArrayList<>();
    List<String> testPred = new ArrayList<>();
    List<String> testGold = new ArrayList<>();

    Log log;
    {
    	log = LogFactory.getLog(getClass());
    }
    
    static String featureFile = null;
    {
        TCMachineLearningAdapter adapter = CRFSuiteAdapter.getInstance();
        featureFile = adapter.getFrameworkFilename(AdapterNameEntries.featureVectorsFile);
    }

    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        File train = null, id2o = null;
        String outputFolderId = null;
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains("ExtractFeaturesTask-Train")) {
                train = store.locateKey(subcontext.getId(), TEST_TASK_OUTPUT_KEY + "/"
                        + featureFile);
                extractTrainingVocab(train);
            }
        }
        
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains(CRFSuiteTestTask.class.getName())) {
                outputFolderId = subcontext.getId();
                id2o = store.locateKey(subcontext.getId(), Constants.ID_OUTCOME_KEY);
                loadTestResults(id2o);
                evaluate(store, outputFolderId + "/", plain_iv_all, plain_oov_all);
            }    
        }
        
        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains(PostPosUpdateTask.class.getName())) {
                outputFolderId = subcontext.getId();
                id2o = store.locateKey(subcontext.getId(), Constants.ID_OUTCOME_KEY);
                loadTestResults(id2o);
                evaluate(store, outputFolderId + "/", post_iv_all, post_oov_all);
            }    
        }

    }

    private void evaluate(StorageService store, String outputContext, List<File> iv_all, List<File> oov_all)
        throws Exception
    {
        double known_correct = 0;
        double known_incorrect = 0;
        ConditionalFrequencyDistribution<String, String> known_cfd = new ConditionalFrequencyDistribution<String, String>();

        double unknown_correct = 0;
        double unknown_incorrect = 0;

        ConditionalFrequencyDistribution<String, String> unknown_cfd = new ConditionalFrequencyDistribution<String, String>();
        for (int i = 0; i < testToken.size(); i++) {
            String key = testToken.get(i);
            String pred = testPred.get(i);
            String gold = testGold.get(i);
            boolean b = pred.equals(gold);

            if (training.contains(key)) {
                if (b) {
                    known_correct++;
                }
                else {
                    known_incorrect++;
                }
                known_cfd.addSample(gold, pred, 1);
            }
            else {
                if (b) {
                    unknown_correct++;
                }
                else {
                    unknown_incorrect++;
                }
                unknown_cfd.addSample(gold, pred, 1);
            }
        }

        String known = buildOutput(known_correct, known_incorrect, known_cfd);
        String unknown = buildOutput(unknown_correct, unknown_incorrect, unknown_cfd);

        String ivLocation = store.locateKey(outputContext, "").getAbsolutePath() + "/"
                + IV_STAT_FILE;
        String oovLocation = store.locateKey(outputContext, "").getAbsolutePath() + "/"
                + OOV_STAT_FILE;

        File invo = new File(ivLocation);
        File oov = new File(oovLocation);
        FileUtils.writeStringToFile(invo, known);
        FileUtils.writeStringToFile(oov, unknown);

        iv_all.add(invo);
        oov_all.add(oov);
    }

    private String buildOutput(double correct, double incorrect,
            ConditionalFrequencyDistribution<String, String> cfd)
    {
        StringBuilder sb = new StringBuilder();
        double accuracy = (correct / (correct + incorrect)) * 100;
        sb.append(ACCURACY_TEXT + String.format("%.1f\n", accuracy));
        sb.append("Total: " + String.format("%.0f", correct + incorrect) + " Correct: "
                + String.format("%.0f", correct) + " Incorrect: "
                + String.format("%.0f", incorrect) + "\n\n");

        sb.append(String.format("%10s\t%5s\t%-15s\n", "Label", "Correct", "Worst-Conf"));
        List<String> conditions = new ArrayList<String>(cfd.getConditions());
        Collections.sort(conditions);
        for (String key : conditions) {
            FrequencyDistribution<String> fd = cfd.getFrequencyDistribution(key);
            double accPerLabel = (double) fd.getCount(key) / fd.getN() * 100;

            String worstConfusion = getClassThatWasMostOftenChosenWronglyForLabel(fd, key);

            sb.append(String.format("%10s\t%5.2f\t%-15s", key, accPerLabel, worstConfusion) + "\n");
        }

        return sb.toString();
    }

    private void loadTestResults(File id2o)
        throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(id2o),
                "UTF-8"));
        String line = null;
        Map<String, String> id2label = new HashMap<>();
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#labels")) {
                id2label = getId2LabelMapping(line);
                continue;
            }
            if (line.startsWith("#")) {
                continue;
            }
            if (line.isEmpty()) {
                continue;
            }

            int equal = line.lastIndexOf("=");
            int sem = line.lastIndexOf(";");
            int underscore = line.lastIndexOf("_", equal);

            String token = line.substring(underscore + 1, equal);
            String pred = id2label.get(line.substring(equal + 1, sem));
            String gold = id2label.get(line.substring(sem + 1));

            testToken.add(token);
            testPred.add(pred);
            testGold.add(gold);
        }
        br.close();
        log.info("Loaded test data from ["+id2o.getAbsolutePath()+"]; test set composes of ["+testToken.size()+"] token with ["+testPred.size()+"] predictions and ["+testGold.size()+"]");
    }

    private Map<String, String> getId2LabelMapping(String line)
    {
        line = line.replaceAll("#labels ", "");

        String[] split = line.split(" ");

        Map<String, String> results = new HashMap<String, String>();
        for (String s : split) {
            String[] e = s.split("=");
            results.put(e[0], e[1]);
        }

        return results;
    }

    public static void generateSummaryReport(String output, List<File> files, String prefix)
        throws IOException
    {
        Map<String,Double> avgWc = new HashMap<>();
        
        Double avg_acc=0.0;
        for (File o : files) {
            List<String> readLines = FileUtils.readLines(o, "utf-8");
            String acc_line = readLines.get(0);
            acc_line = acc_line.replaceAll(ACCURACY_TEXT, "");
            avg_acc += Double.valueOf(acc_line.replaceAll(",", "."));
            
            for(int i=4; i < readLines.size(); i++){
                String line = readLines.get(i);
                String[] split = line.split("\t");
                String tag = split[0].trim();
                String val = split[1].replaceAll(",", ".");
                Double acc = Double.valueOf(val);
                
                Double curVal = avgWc.get(tag);
                if(curVal==null){
                    curVal = acc;
                }else{
                    curVal += acc;
                }
                avgWc.put(tag, curVal);
            }
            
        }
        avg_acc /= files.size();
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + " Acc: " + String.format("%.1f\n", avg_acc)+"%");
        sb.append("\n");
        
        List<String> keySet = new ArrayList<String>(avgWc.keySet());
        Collections.sort(keySet);
        for(String key : keySet){
            Double avg = avgWc.get(key) / files.size();
            sb.append(String.format("%15s\t\t%.1f\n", key, avg));
        }
        
        FileUtils.writeStringToFile(new File(output), sb.toString());
    }

    private String getClassThatWasMostOftenChosenWronglyForLabel(FrequencyDistribution<String> fd,
            String gold)
    {
        String mostOftenWronglyChosenLabel = null;
        long maxCount = 0;
        for (String key : fd.getKeys()) {
            if (key.equals(gold)) {
                continue;
            }
            long count = fd.getCount(key);
            if (maxCount < count) {
                mostOftenWronglyChosenLabel = key;
                maxCount = count;
            }
        }

        double worstConf = ((double) maxCount / fd.getN())*100;
        String output = String.format("(%.2f%5s)", worstConf, mostOftenWronglyChosenLabel);

        return output;
    }

    private void extractTrainingVocab(File train)
        throws Exception
    {
        training = new HashSet<String>();
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(train), "UTF-8");
        BufferedReader br = new BufferedReader(streamReader);

        String next = null;
        while ((next = br.readLine()) != null) {

            if (next.isEmpty()) {
                continue;
            }

            String word = extractUnit(next);
            training.add(word);
        }

        br.close();

    }

    private String extractUnit(String next)
    {
        int start = next.indexOf(ID_FEATURE_NAME);
        int end = next.indexOf("\t", start);
        start = next.lastIndexOf("_", end);

        String word = next.substring(start + 1, end);

        return word;
    }
}
