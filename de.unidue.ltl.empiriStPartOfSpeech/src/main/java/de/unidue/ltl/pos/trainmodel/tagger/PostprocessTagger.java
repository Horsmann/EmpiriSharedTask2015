package de.unidue.ltl.pos.trainmodel.tagger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.jfree.util.Log;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PostprocessTagger
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_NAMELIST_FOLDER = "namelistFolder";
    @ConfigurationParameter(name = PARAM_NAMELIST_FOLDER, mandatory = true)
    private File folder;
    
    public static final String PARAM_NAMELIST_LOWER_CASE = "PARAM_LOWER_CASE";
    @ConfigurationParameter(name = PARAM_NAMELIST_LOWER_CASE, mandatory = true, defaultValue = "false")
    private boolean lowerCase;

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;

    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    private String posMappingLocation;

    private Set<String> names = null;

    private MappingProvider p;
    
    private Logger logger;
    

    @Override
    public void initialize(final UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        initNames();

        p = new MappingProvider();
        p.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/"
                        + "${language}-${pos.tagset}-pos.map");
        p.setDefault(MappingProvider.BASE_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS");
        p.setDefault("pos.tagset", "default");
        p.setOverride(MappingProvider.LOCATION, posMappingLocation);
        p.setOverride(MappingProvider.LANGUAGE, language);
        
        logger = Logger.getLogger(PostprocessTagger.class);

    }

    private void initNames()
        throws ResourceInitializationException
    {
        if (names != null) {
            return;
        }
        names = new HashSet<String>();

        for (File file : folder.listFiles()) {
            if (file.isHidden()) {
                continue;
            }
            if (file.isDirectory()) {
                throw new ResourceInitializationException(new Throwable(
                        "Did not expect that namelists are stored in subfolders"));
            }

            List<String> readLines = null;
            try {
                readLines = FileUtils.readLines(file, "utf-8");
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
            for (String l : readLines) {
                if (l.startsWith("#")) {
                    continue;
                }
                if (lowerCase) {
                    l = l.toLowerCase();
                }

                names.add(l);
            }
        }
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
        for (int i = 0; i < tokens.size(); i++) {
            
            if (updateIfHashTag(aJCas, tokens, i)) {
                continue;
            }
            if (updateIfAtMention(aJCas, tokens, i)) {
                continue;
            }
            if (updateIfUrl(aJCas, tokens, i)) {
                continue;
            }
            if (updateIfEmail(aJCas, tokens, i)) {
                continue;
            }
            if (updateIfTruncation(aJCas, tokens, i)) {
                continue;
            }
            if (udpateIfNamedEntity(aJCas, tokens, i)) {
              logger.info("Recognized token ["+tokens.get(i).getCoveredText()+"] as NE");
                continue;
            }
            if(updateAnonymizedStudentNames(aJCas, tokens,i)){
                continue;
            }
            if (updateIfWord(aJCas, tokens, i, "sehr")) {
                continue;
            }
            if(updateIfWhatsAppEmoji(aJCas, tokens,i)){
                continue;
            }
        }
    }

    private boolean updateIfWhatsAppEmoji(JCas aJCas, List<Token> tokens, int i)
    {
        String text = tokens.get(i).getCoveredText();
        if(text.startsWith("emojiQ")){
            updatePos(aJCas, tokens.get(i), "EMOIMG");
        }
        
        return false;
    }

    private boolean updateAnonymizedStudentNames(JCas aJCas, List<Token> tokens, int i)
    {
        String text = tokens.get(i).getCoveredText();
        if(Pattern.matches("Student[0-9]+", text)){
            updatePos(aJCas, tokens.get(i), "NE");
            Log.info("Fixed Student Updateing");
            return true;
        }
        
        return false;
    }

//    private boolean updateForeignWord(JCas aJCas, List<Token> tokens, int i)
//    {
//        Token t = tokens.get(i);
//        String text = tokens.get(i).getCoveredText().toLowerCase();
//        
////        logger.info("FM check - ["+text+"] Is a NE");
//        
//        POS pos = t.getPos();
//        if(pos != null && pos.getPosValue().equals("NE")){
//            return false;
//        }
//        
//        if(text.length() < 3){
//            //short words as "b." etc. a problem
//            return false;
//        }
//        
//        
//        if(text.matches("[^a-zäöüß]+")){
//            //only pure words
//            return false;
//        }
//        
//        if(enclosingAposthrope(tokens,i)){
//            if(germanVocab.contains(text)){
//                return false;
//            }
//            updatePos(aJCas, t, "FM");
//            return true;
//        }
//        
//        if(englishVocab.contains(text) && !germanVocab.contains(text)){
//            updatePos(aJCas, t, "FM");
//            return true;
//        }
//        
//        return false;
//    }
//
//    private boolean enclosingAposthrope(List<Token> tokens, int i)
//    {
//        if(i -1 < 0 || i+1>=tokens.size()){
//            return false;
//        }
//        
//        String before = tokens.get(i-1).getCoveredText();
//        String after = tokens.get(i+1).getCoveredText();
//        
//        return before.equals("„") && after.equals("“");
//    }

    private boolean updateIfWord(JCas aJCas, List<Token> tokens, int idx, String string)
    {
        Token t = tokens.get(idx);
        String text = t.getCoveredText();
        if (text.equals(string)) {
            updatePos(aJCas, t, "PTKIFG");
            return true;
        }
        return false;
    }

    private boolean updateIfEmail(JCas aJCas, List<Token> tokens, int idx)
    {
        Token t = tokens.get(idx);
        String text = t.getCoveredText();
        if (Pattern.matches("[0-9a-z\\.\\-_]+@[0-9a-z\\.\\-_]+", text)) {
            updatePos(aJCas, t, "EML");
            return true;
        }
        return false;
    }

    private boolean updateIfTruncation(JCas aJCas, List<Token> tokens, int idx)
    {
        Token t = tokens.get(idx);
        String text = t.getCoveredText();
        if (Pattern.matches("[A-Za-zÄäÖöÜüß]+\\-", text)) {
            updatePos(aJCas, t, "TRUNC");
            return true;
        }
        return false;
    }

    private boolean udpateIfNamedEntity(JCas aJCas, List<Token> tokens, int idx)
    {
//    	Token t = tokens.get(idx);
//    	List<NamedEntity> nes = JCasUtil.selectCovered(aJCas, NamedEntity.class,t.getBegin(), t.getEnd() );
//    	if(nes.isEmpty()){
//    		return false;
//    	}
//    	
//    	updatePos(aJCas, t, "NE");
//    	return true;
    	
        String neCandidate = null;

        // 3 word window
        if (idx + 2 < tokens.size()) {
            String c = tokens.get(idx).getCoveredText();
            String n1 = tokens.get(idx + 1).getCoveredText();
            String n2 = tokens.get(idx + 2).getCoveredText();

            neCandidate = c + " " + n1 + " " + n2;
            if (isNamedEntity(neCandidate)) {
                updatePos(aJCas, tokens.get(idx), "NE");
                updatePos(aJCas, tokens.get(idx + 1), "NE");
                updatePos(aJCas, tokens.get(idx + 2), "NE");
                return true;
            }
        }

        // 2 word window
        if (idx + 1 < tokens.size()) {
            String c = tokens.get(idx).getCoveredText();
            String n1 = tokens.get(idx + 1).getCoveredText();

            neCandidate = c + " " + n1;
            if (isNamedEntity(neCandidate)) {
                updatePos(aJCas, tokens.get(idx), "NE");
                updatePos(aJCas, tokens.get(idx + 1), "NE");
                return true;
            }
        }

        // 1 word window
        if (idx < tokens.size()) {
            String c = tokens.get(idx).getCoveredText();

            neCandidate = c;
            if (isNamedEntity(neCandidate)) {
                updatePos(aJCas, tokens.get(idx), "NE");
                return true;
            }
        }

        return false;
    }

    private boolean isNamedEntity(String text)
    {
        if (lowerCase) {
            text = text.toLowerCase();
        }
        return names.contains(text);
    }

    private void updatePos(JCas aJCas, Token t, String val)
    {
        POS pos = t.getPos();
        if (pos == null) {

            Type posTag = p.getTagType(val);
            pos = (POS) aJCas.getCas().createAnnotation(posTag, t.getBegin(), t.getEnd());
            pos.addToIndexes();
        }
        pos.setPosValue(val);
    }

    private boolean updateIfUrl(JCas aJCas, List<Token> tokens, int idx)
    {
        Token t = tokens.get(idx);
        String text = t.getCoveredText();
        if (text.startsWith("http") || text.startsWith("www.")) {
            updatePos(aJCas, t, "URL");
            return true;
        }
        return false;
    }

    private boolean updateIfHashTag(JCas aJCas, List<Token> tokens, int idx)
    {
        Token t = tokens.get(idx);
        String text = t.getCoveredText();
        if (text.startsWith("#")) {
            updatePos(aJCas, t, "HST");
            return true;
        }
        return false;
    }

    private boolean updateIfAtMention(JCas aJCas, List<Token> tokens, int idx)
    {
        Token t = tokens.get(idx);
        String text = t.getCoveredText();
        if (text.startsWith("@")) {
            updatePos(aJCas, t, "ADR");
            return true;
        }
        return false;
    }

    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {

    }

}
