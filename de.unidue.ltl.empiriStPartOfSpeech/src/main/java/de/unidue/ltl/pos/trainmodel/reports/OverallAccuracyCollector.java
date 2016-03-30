package de.unidue.ltl.pos.trainmodel.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.dkpro.lab.reporting.BatchReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.task.TaskContextMetadata;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.util.ReportConstants;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;
import org.dkpro.tc.crfsuite.task.CRFSuiteTestTask;

import de.unidue.ltl.pos.trainmodel.tc.PostPosUpdateTask;

public class OverallAccuracyCollector
    extends BatchReportBase
    implements Constants
{

    public static List<File> writtenFiles_plain = new ArrayList<>();
    public static List<File> writtenFiles_post = new ArrayList<>();

    public void execute()
        throws Exception
    {

        for (TaskContextMetadata subcontext : getSubtasks()) {
            if (subcontext.getType().contains(CRFSuiteTestTask.class.getName())) {
                StorageService storageService = getContext().getStorageService();

                File storageFolder = storageService.locateKey(subcontext.getId(), "");
                File evaluation = new File(storageFolder,
                        new CRFSuiteAdapter()
                                .getFrameworkFilename(AdapterNameEntries.evaluationFile));
                writtenFiles_plain.add(evaluation);
            }
            if (subcontext.getType().contains(PostPosUpdateTask.class.getName())) {
                StorageService storageService = getContext().getStorageService();

                File storageFolder = storageService.locateKey(subcontext.getId(), "");
                File evaluation = new File(storageFolder,
                        new CRFSuiteAdapter()
                                .getFrameworkFilename(AdapterNameEntries.evaluationFile));
                writtenFiles_post.add(evaluation);

            }
        }
    }

    public static void summarize(String output, List<File> files)
        throws IOException
    {

        List<Double> accuracies = new ArrayList<>();

        for (File f : files) {
            Properties p = new Properties();
            p.load(new FileInputStream(f));
            String property = p.getProperty(ReportConstants.PCT_CORRECT);
            String value = property.replaceAll(",", ".");
            Double accuracy = Double.valueOf(value);
            accuracies.add(accuracy * 100);
        }

        StringBuilder sb = new StringBuilder();
        Double sum = 0.0;
        for (Double d : accuracies) {
            sum += d;

            sb.append(String.format("%4.1f\n", d));
        }

        Double avg = sum / accuracies.size();
        sb.append("------\n");
        sb.append(String.format("%4.1f\n", avg));

        double varianz = getVarianz(avg, accuracies);

        sb.append("\n");
        // sb.append(String.format("Varianz: %.2f", varianz));
        sb.append(String.format("Std-Dev: %.2f\n", Math.sqrt(varianz)));

        FileUtils.writeStringToFile(new File(output), sb.toString());
    }

    private static double getVarianz(Double avg, List<Double> accuracies)
    {
        double val = 0.0;
        for (Double v : accuracies) {
            val += Math.pow(v - avg, 2);
        }

        return val / accuracies.size();
    }
}
