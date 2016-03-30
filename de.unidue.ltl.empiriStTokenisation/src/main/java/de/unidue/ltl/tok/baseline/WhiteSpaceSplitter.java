package de.unidue.ltl.tok.baseline;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class WhiteSpaceSplitter
    extends JCasAnnotator_ImplBase
{

    Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        String[] tokens = whiteSpaceTokenization(aJCas);
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

    private void warn(String msg)
    {
        log.log(Level.WARNING, msg);
    }

    private String[] whiteSpaceTokenization(JCas aJCas)
    {
        String singleWhiteSpaceSeparatedString = aJCas.getDocumentText().replaceAll(" +", " ");
        return singleWhiteSpaceSeparatedString.split(" ");
    }

    private void annotateToken(JCas aJCas, String[] split)
    {
        String documentText = aJCas.getDocumentText();
        int startIdx = 0;
        int endIdx = -1;
        for (String token : split) {

            // tough cases where a unit has a whitespace in the middle
            // somewhere. In this case the simple
            // string matching won't work because we assume that a unit has no
            // whitespaces --> indexOf will return -1 as this unit is not found
            // in the document text
            // Case for instance a smiley such as ": )", with removed whitespace
            // .indexOf won't match.

            if (documentText.indexOf(token, startIdx) == -1) {
                warn("Cannot find token [" + token
                        + "] in document text - I will try to match it somehow");

                int idx = startIdx;
                if (documentText.charAt(startIdx) == ' ') {
                    idx += 1;
                }

                while (documentText.charAt(idx) != ' ') {
                    idx++;
                }

                int len = idx - startIdx;
                if (documentText.charAt(startIdx) == ' ') {
                    len--;
                }

                startIdx = documentText.indexOf(token.substring(0, len), startIdx);
                endIdx = documentText.indexOf(token.substring(len), startIdx) + 1;

                sanityCheck(startIdx, endIdx);

                warn("Matched [" + token + "] to [" + documentText.substring(startIdx, endIdx)
                        + "] in this right?");

            }
            // easy cases
            else {
                startIdx = documentText.indexOf(token, startIdx);
                endIdx = startIdx + token.length();
            }

            Token t = new Token(aJCas, startIdx, endIdx);
            t.addToIndexes();

            startIdx = endIdx;
        }
    }

    private void sanityCheck(int startIdx, int endIdx)
    {
        if (startIdx < 0 || endIdx < 0) {
            throw new IllegalArgumentException("Matching failed index became negative startIdx: ["
                    + startIdx + "] + endIdx: [" + endIdx + "]");
        }

        if (startIdx > endIdx) {
            throw new IllegalArgumentException(
                    "Matching failed start index became larger than endId");
        }
    }

}
