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

import de.unidue.ltl.tok.type.ExToken;

public class ExDumper extends JCasAnnotator_ImplBase {

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

				for (ExToken t : JCasUtil.select(aJCas, ExToken.class)) {
					bw.write(t.getTokenValue());
					bw.write("\n");
				}
				bw.write("\n");
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
