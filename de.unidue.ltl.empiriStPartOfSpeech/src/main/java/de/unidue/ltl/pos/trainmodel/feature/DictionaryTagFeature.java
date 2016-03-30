package de.unidue.ltl.pos.trainmodel.feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

public class DictionaryTagFeature
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    public static final String PARAM_DICTIONARY_LOCATION = "dictLocation";
    @ConfigurationParameter(name = PARAM_DICTIONARY_LOCATION, mandatory = true)
    protected String dictionaryLocation;

    public static final String PARAM_LOAD_DICT_LOWER_CASE = "dictLowerCase";
    @ConfigurationParameter(name = PARAM_LOAD_DICT_LOWER_CASE, mandatory = true, defaultValue = "false")
    private boolean lowerCase;

    public static final String FEATURE_NAME = "tag_";

    private Map<String, String[]> map = new HashMap<>();

    private String UNKNOWN = "*";

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            loadDictionary();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    private void loadDictionary()
        throws IOException
    {
        List<String> readLines = FileUtils.readLines(new File(dictionaryLocation));

        for (String l : readLines) {
            String[] split = l.split("\t");
            String token = split[0];

            List<String> tags = new ArrayList<String>();
            for (int i = 1; i < split.length && i <= 3; i++) {
                tags.add(split[i]);
            }
            while (tags.size() < 3) {
                tags.add(UNKNOWN);
            }

            if (lowerCase) {
                token = token.toLowerCase();
            }

            map.put(token, tags.toArray(new String[0]));
        }
    }

    public Set<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        Set<Feature> features = new HashSet<Feature>();

        String tokenText = aClassificationUnit.getCoveredText();

        if (lowerCase) {
            tokenText = tokenText.toLowerCase();
        }

        String[] mft = getMostFrequentTag(tokenText);

        features.add(new Feature(FEATURE_NAME + "1", mft[0]));
        features.add(new Feature(FEATURE_NAME + "2", mft[1]));
        features.add(new Feature(FEATURE_NAME + "3", mft[2]));

        return features;
    }

    private String[] getMostFrequentTag(String tokenText)
    {
        String[] mft = map.get(tokenText);
        if (mft != null) {
            return mft;
        }
        mft = map.get(tokenText.toLowerCase());
        if (mft != null) {
            return mft;
        }

        return new String[] { UNKNOWN, UNKNOWN, UNKNOWN };
    }

}
