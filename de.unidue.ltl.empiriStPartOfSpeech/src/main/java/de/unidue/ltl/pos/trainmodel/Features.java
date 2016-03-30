package de.unidue.ltl.pos.trainmodel;

import java.util.Arrays;
import java.util.List;

import org.dkpro.lab.task.Dimension;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.features.length.NrOfCharsUFE;
import org.dkpro.tc.features.ngram.LuceneCharacterNGramUFE;
import org.dkpro.tc.features.tcu.CurrentUnit;
import org.dkpro.tc.features.tcu.NextNextUnit;
import org.dkpro.tc.features.tcu.NextUnit;
import org.dkpro.tc.features.tcu.PrevPrevUnit;
import org.dkpro.tc.features.tcu.PrevUnit;

import de.unidue.ltl.pos.trainmodel.feature.BrownClusterNormalizedLowerCaseFeature;
import de.unidue.ltl.pos.trainmodel.feature.ContainsCapitalLetter;
import de.unidue.ltl.pos.trainmodel.feature.ContainsComma;
import de.unidue.ltl.pos.trainmodel.feature.ContainsHyphen;
import de.unidue.ltl.pos.trainmodel.feature.ContainsPeriod;
import de.unidue.ltl.pos.trainmodel.feature.ContainsUnderScore;
import de.unidue.ltl.pos.trainmodel.feature.DictionaryTagFeature;
import de.unidue.ltl.pos.trainmodel.feature.IsAllCapitalized;
import de.unidue.ltl.pos.trainmodel.feature.IsCamelCase;
import de.unidue.ltl.pos.trainmodel.feature.IsColon;
import de.unidue.ltl.pos.trainmodel.feature.IsDot;
import de.unidue.ltl.pos.trainmodel.feature.IsExclamationMark;
import de.unidue.ltl.pos.trainmodel.feature.IsFirstLetterCapitalized;
import de.unidue.ltl.pos.trainmodel.feature.IsFocusPartikel;
import de.unidue.ltl.pos.trainmodel.feature.IsInfinitiveModalVerb;
import de.unidue.ltl.pos.trainmodel.feature.IsIntensitaetsPartikel;
import de.unidue.ltl.pos.trainmodel.feature.IsNumber;
import de.unidue.ltl.pos.trainmodel.feature.IsQuestionMark;
import de.unidue.ltl.pos.trainmodel.feature.IsTripleDot;
import de.unidue.ltl.pos.trainmodel.feature.IsVerbPronounFusion;
import de.unidue.ltl.pos.trainmodel.feature.MorphFeat;

public class Features
    implements Constants
{

    @SuppressWarnings("unchecked")
    public static Dimension<List<String>> getFeatures()
    {
        return Dimension.create(
                DIM_FEATURE_SET,
                Arrays.asList(new String[] {
                        // Context
                        PrevPrevUnit.class.getName(),
                        PrevUnit.class.getName(),
                        CurrentUnit.class.getName(),
                        NextUnit.class.getName(),
                        NextNextUnit.class.getName(),

                        // Contains
                        ContainsCapitalLetter.class.getName(),
                        ContainsComma.class.getName(),
                        ContainsHyphen.class.getName(),
                        ContainsPeriod.class.getName(),
                        ContainsUnderScore.class.getName(),

                        // Punctuation
                        IsColon.class.getName(),
                        IsDot.class.getName(),
                        IsExclamationMark.class.getName(),
                        IsQuestionMark.class.getName(),
                        IsTripleDot.class.getName(),

                        // All
                        IsAllCapitalized.class.getName(),

                        // Is
                        IsFirstLetterCapitalized.class.getName(), IsNumber.class.getName(),
                        IsCamelCase.class.getName(),

                        LuceneCharacterNGramUFE.class.getName(),
                        NrOfCharsUFE.class.getName(),

//                        // Resources
                        DictionaryTagFeature.class.getName(),
                        MorphFeat.class.getName(),
                        BrownClusterNormalizedLowerCaseFeature.class.getName(),
                        
                
//                        // German particles
                        IsFocusPartikel.class.getName(), 
                        IsIntensitaetsPartikel.class.getName(),
                        IsVerbPronounFusion.class.getName(),

                        IsInfinitiveModalVerb.class.getName(),
                }));
    }

    @SuppressWarnings("unchecked")
    public static Dimension<List<Object>> getFeatureParams(Integer charMinNgram,
            Integer charMaxNgram, Integer charTopNgram, String brownCluster,
            String posDictionary, String morphDictionary)
    {
        return Dimension.create(DIM_PIPELINE_PARAMS, Arrays.asList(new Object[] {
                MorphFeat.PARAM_JSON, morphDictionary,
                DictionaryTagFeature.PARAM_DICTIONARY_LOCATION, posDictionary,
                DictionaryTagFeature.PARAM_LOAD_DICT_LOWER_CASE, false,
                BrownClusterNormalizedLowerCaseFeature.PARAM_BROWN_CLUSTER_CLASS_PROPABILITIES,
                brownCluster, LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MIN_N, charMinNgram,
                LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_MAX_N, charMaxNgram,
                LuceneCharacterNGramUFE.PARAM_CHAR_NGRAM_USE_TOP_K, charTopNgram }));
    }

}
