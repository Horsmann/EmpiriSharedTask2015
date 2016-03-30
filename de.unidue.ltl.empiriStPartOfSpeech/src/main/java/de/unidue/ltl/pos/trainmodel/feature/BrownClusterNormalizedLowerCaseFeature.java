package de.unidue.ltl.pos.trainmodel.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

public class BrownClusterNormalizedLowerCaseFeature
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    public static final String PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES = "brownClassProbLocation";
    @ConfigurationParameter(name = PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES, mandatory = true)
    private File inputFile;

    private HashMap<String, String> map = null;
    
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            init();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    public Set<Feature> extract(JCas aJcas, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        String unit = aClassificationUnit.getCoveredText().toLowerCase();

        String workingCopy = normalizeUrls(unit, "<URL>");
        workingCopy = normalizeEmails(workingCopy, "<EMAIL>");
        workingCopy = normalizeAtMentions(workingCopy, "<ATMENTION>");
        workingCopy = normalizeHashTags(workingCopy, "<HASHTAG>");

        Set<Feature> features = createFeatures(unit);

        return features;
    }

    private Set<Feature> createFeatures(String unit)
    {
        Set<Feature> features = new HashSet<Feature>();

        String bitCode = map.get(unit);

        features.add(new Feature("brown16_", bitCode != null && bitCode.length() >= 16 ? bitCode
                .substring(0, 16) : "*"));
        features.add(new Feature("brown14_", bitCode != null && bitCode.length() >= 14 ? bitCode
                .substring(0, 14) : "*"));
        features.add(new Feature("brown12_", bitCode != null && bitCode.length() >= 12 ? bitCode
                .substring(0, 12) : "*"));
        features.add(new Feature("brown10_", bitCode != null && bitCode.length() >= 10 ? bitCode
                .substring(0, 10) : "*"));
        features.add(new Feature("brown8_", bitCode != null && bitCode.length() >= 8 ? bitCode
                .substring(0, 8) : "*"));
        features.add(new Feature("brown6_", bitCode != null && bitCode.length() >= 6 ? bitCode
                .substring(0, 6) : "*"));
        features.add(new Feature("brown4_", bitCode != null && bitCode.length() >= 4 ? bitCode
                .substring(0, 4) : "*"));
        features.add(new Feature("brown2_", bitCode != null && bitCode.length() >= 2 ? bitCode
                .substring(0, 2) : "*"));

        return features;
    }

    private void init()
        throws TextClassificationException
    {

        if (map != null) {
            return;
        }
        map = new HashMap<String, String>();

        try {

            BufferedReader bf = openFile();
            String line = null;
            while ((line = bf.readLine()) != null) {
                String[] split = line.split("\t");
                map.put(split[1], split[0]);
            }

        }
        catch (Exception e) {
            throw new TextClassificationException(e);
        }
    }

    private BufferedReader openFile()
        throws Exception
    {
        InputStreamReader isr = null;
        if (inputFile.getAbsolutePath().endsWith(".gz")) {

            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFile)),
                    "UTF-8");
        }
        else {
            isr = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
        }
        return new BufferedReader(isr);
    }
    
   
        public static String replaceTwitterPhenomenons(String input, String replacement)
        {
            /* Email and atmention are sensitive to order of execution */
            String workingCopy = input;
            workingCopy = normalizeUrls(workingCopy, replacement);
            workingCopy = normalizeEmails(workingCopy, replacement);
            workingCopy = normalizeAtMentions(workingCopy, replacement);
            workingCopy = normalizeHashTags(workingCopy, replacement);
            return workingCopy;
        }

        public static String normalizeHashTags(String input, String replacement)
        {
            String HASHTAG = "#[a-zA-Z0-9-_]+";
            String normalized = input.replaceAll(HASHTAG, replacement);
            return normalized;
        }

        public static String normalizeEmails(String input, String replacement)
        {
            String PREFIX = "[a-zA-Z0-9-_\\.]+";
            String SUFFIX = "[a-zA-Z0-9-_]+";

            String EMAIL_REGEX = PREFIX + "@" + SUFFIX + "\\." + "[a-zA-Z]+";
            String normalize = input.replaceAll(EMAIL_REGEX, replacement);
            return normalize;
        }

        public static String normalizeAtMentions(String input, String replacement)
        {
            String AT_MENTION_REGEX = "@[a-zA-Z0-9_-]+";
            String normalize = input.replaceAll(AT_MENTION_REGEX, replacement);
            return normalize;
        }

        public static String normalizeUrls(String input, String replacement)
        {
            String URL_CORE_REGEX = "[\\/\\\\.a-zA-Z0-9-_]+";

            String normalized = input.replaceAll("http:" + URL_CORE_REGEX, replacement);
            normalized = normalized.replaceAll("https:" + URL_CORE_REGEX, replacement);
            normalized = normalized.replaceAll("www\\." + URL_CORE_REGEX, replacement);

            return normalized;
        }

}
