package de.unidue.ltl.tok.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class Dumper extends JCasAnnotator_ImplBase {

	public static final String PARAM_TARGET_FILE = "PARAM_TARGET_FILE";
	@ConfigurationParameter(name = PARAM_TARGET_FILE, mandatory = true)
	private String outputFile;

	private BufferedWriter bw;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		try {
			File file = new File(outputFile);
			file.getParentFile().mkdirs();
			bw = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		try {
			for (Sentence s : JCasUtil.select(aJCas, Sentence.class)) {

				for (Token t : JCasUtil.selectCovered(aJCas, Token.class,
						s.getBegin(), s.getEnd())) {
					bw.write(t.getCoveredText());
					bw.write("\n");
				}
				bw.write("\n");
			}
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {

		try {
			bw.close();
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

}
