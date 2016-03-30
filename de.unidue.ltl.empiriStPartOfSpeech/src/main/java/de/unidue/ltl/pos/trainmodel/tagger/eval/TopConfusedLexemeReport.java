package de.unidue.ltl.pos.trainmodel.tagger.eval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.type.GoldPOS;

public class TopConfusedLexemeReport
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_OUTPUT_FOLDER = "outputFolder";
    @ConfigurationParameter(name = PARAM_OUTPUT_FOLDER, mandatory = true)
    private String outputFolder;

    public static final String PARAM_FILE_GROUP_KEY = "groupKey";
    @ConfigurationParameter(name = PARAM_FILE_GROUP_KEY, mandatory = true)
    private String groupKey;

    public static final String PARAM_FILE_USER_KEY = "userKey";
    @ConfigurationParameter(name = PARAM_FILE_USER_KEY, mandatory = false)
    private String userKey;

    LexemeConfusionTracker fine = new LexemeConfusionTracker();
    LexemeConfusionTracker coarse = new LexemeConfusionTracker();

    @Override
    public void initialize(final UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

    }

    @Override
    public void process(JCas arg0)
        throws AnalysisEngineProcessException
    {

        for (Sentence s : JCasUtil.select(arg0, Sentence.class)) {

            List<Token> tokens = JCasUtil
                    .selectCovered(arg0, Token.class, s.getBegin(), s.getEnd());

            List<GoldPOS> gold = JCasUtil.selectCovered(arg0, GoldPOS.class, s.getBegin(),
                    s.getEnd());
            List<POS> pred = JCasUtil.selectCovered(arg0, POS.class, s.getBegin(), s.getEnd());

            for (int i = 0; i < pred.size(); i++) {
                String token = tokens.get(i).getCoveredText();

                String predictionFine = pred.get(i).getPosValue();
                String goldFine = gold.get(i).getPosTag().getPosValue();
                if (!predictionFine.equals(goldFine)) {
                    fine.addSample(goldFine, token + "-" + predictionFine);
                }

                String predictionCoarse = pred.get(i).getClass().getSimpleName();
                String goldCoarse = gold.get(i).getCPosTag();
                if (!predictionCoarse.equals(goldCoarse)) {
                    coarse.addSample(goldCoarse, token + "-" + predictionCoarse);
                }
            }

        }

    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        String fileFine = outputFolder + "/" + groupKey + "_" + userKey + "_fineLexConfusion.txt";
        String fileCoarse = outputFolder + "/" + groupKey + "_" + userKey
                + "_coarseLexConfusion.txt";

        String fineFormattedMatrix = fine.getFormattedOutput();
        String coarseFormattedMatrix = coarse.getFormattedOutput();
        try {
            FileUtils.writeStringToFile(new File(fileFine), fineFormattedMatrix);
            FileUtils.writeStringToFile(new File(fileCoarse), coarseFormattedMatrix);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    class LexemeConfusionTracker
    {
        ConditionalFrequencyDistribution<String, String> cfd = new ConditionalFrequencyDistribution<String, String>();

        public void addSample(String gold, String wrongPred)
        {
            cfd.addSample(gold, wrongPred, 1);
        }

        public String getFormattedOutput()
        {
            List<String> conditions = new ArrayList<String>(cfd.getConditions());
            Collections.sort(conditions);

            StringBuilder sb = new StringBuilder();
            for (String con : conditions) {
                FrequencyDistribution<String> fd = cfd.getFrequencyDistribution(con);
                long n = fd.getN();

                sb.append(String.format("%10s", con)
                        + String.format(" %20s ", "(" + ConfusionMatrix.formatValue(n) + ") errors"));
                sb.append(String.format("\t"));

                for (String top : fd.getMostFrequentSamples(10)) {
                    double erroFreq = fd.getCount(top);
                    sb.append(String.format("%20s %5s", top, String.format("%3.1f%%",erroFreq/n*100)));
                }
                sb.append("\n");
            }

            return sb.toString();
        }
    }

}
