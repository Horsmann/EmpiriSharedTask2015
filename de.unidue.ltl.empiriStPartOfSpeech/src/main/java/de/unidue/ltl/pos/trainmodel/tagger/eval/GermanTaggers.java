package de.unidue.ltl.pos.trainmodel.tagger.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.flextag.FlexTag;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosTagger;
import de.unidue.ltl.majoritytagger.MajorityTagTagger;
import de.unidue.ltl.pos.trainmodel.tagger.ComboTagger;
import de.unidue.ltl.pos.trainmodel.tagger.TreeWagger;

public class GermanTaggers
{

    public static Collection<? extends TaggerConfiguration> getEmpiriTrainedModel(String mapping)
    {
        List<TaggerConfiguration> taggers = new ArrayList<TaggerConfiguration>();
        taggers.add(new TaggerConfiguration(FlexTag.class,
                mapping, "de", "empiri"));
        return taggers;
    }
    
    public static Collection<? extends TaggerConfiguration> getEmpiriTrainedModelWithPostProcessing(String mapping)
    {
        List<TaggerConfiguration> taggers = new ArrayList<TaggerConfiguration>();
        taggers.add(new TaggerConfiguration(ComboTagger.class,
                mapping, "de", "empiri"));
        return taggers;
    }
    
    public static Collection<? extends TaggerConfiguration> getMajorityTagger(String mapping)
    {
        List<TaggerConfiguration> taggers = new ArrayList<TaggerConfiguration>();
        taggers.add(new TaggerConfiguration(MajorityTagTagger.class,
                mapping, "de", "empiri16train"));
        return taggers;
    }

    public static Collection<? extends TaggerConfiguration> getTreeTaggerModel(String mapping)
    {
        List<TaggerConfiguration> taggers = new ArrayList<TaggerConfiguration>();
        taggers.add(new TaggerConfiguration(TreeTaggerPosTagger.class,
                mapping, "de", "le"));
        return taggers;
    }

    public static Collection<? extends TaggerConfiguration> getTreeTaggerWithPostProcessingModel(
            String mapping)
    {
        TreeWagger.namelist = "src/main/resources/namelists";
        List<TaggerConfiguration> taggers = new ArrayList<TaggerConfiguration>();
        taggers.add(new TaggerConfiguration(TreeWagger.class,
                mapping, "de", "le"));
        return taggers;
    }
    
}
