package de.unidue.ltl.tok;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.unidue.ltl.tok.type.ExToken;

public class EmpiriTokeniser extends JCasAnnotator_ImplBase {
    
    public static final String PARAM_ABBREVIATION_LIST_LOCATION = "PARAM_ABBREVIATION_LIST_LOCATION";
    @ConfigurationParameter(name = "PARAM_ABBREVIATION_LIST_LOCATION", mandatory = true)
    private String abbreviationFile;
    
    public static final String PARAM_APOSTHROP_JOINED_WORDS = "PARAM_APOSTHROP_JOINED_WORDS";
    @ConfigurationParameter(name = "PARAM_APOSTHROP_JOINED_WORDS", mandatory = true)
    private String aposothropJoinedWords;
    
	Logger log = Logger.getLogger(getClass().getName());
	
	Splitter splitter = new Splitter();
	Merger merger = new Merger();
	
	   @Override
	    public void initialize(final UimaContext context)
	        throws ResourceInitializationException
	    {
	        super.initialize(context);
	        ListUtil.abbreviationFile = abbreviationFile;
	        ListUtil.apostrophJoinedWords = aposothropJoinedWords;
	    }

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String[] tokens = whiteSpaceTokenization(aJCas);
		try {
			tokens = splitter.doSplitting(tokens);
			tokens = merger.doMerging(tokens);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		annotateToken(aJCas, tokens);
		annotateSequence(aJCas);
	}
	
	private void annotateSequence(JCas aJCas)
    {
	    int start = 0;
	    int end = aJCas.getDocumentText().length();
	    Sentence s = new Sentence(aJCas, start, end);
	    s.addToIndexes();
    }

	private String[] whiteSpaceTokenization(JCas aJCas) {
		String singleWhiteSpaceSeparatedString = aJCas.getDocumentText()
				.replaceAll(" +", " ");
		return singleWhiteSpaceSeparatedString.split(" ");
	}

	private void annotateToken(JCas aJCas, String[] split) {
	    

	     int start=0;
      int end=0;
      for(int i=0; i < split.length; i++){
          String s = split[i];
          end=start+s.length();
          ExToken t = new ExToken(aJCas, start, end);
          t.setTokenValue(s);
          t.addToIndexes();

          start = end+1;
      
      }
	}
}
