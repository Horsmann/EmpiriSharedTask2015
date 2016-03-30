package de.unidue.ltl.pos.trainmodel.tagger;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.flextag.FlexTag;

public class ComboTagger extends JCasAnnotator_ImplBase {

	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	private String language;

	public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
	@ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
	private String variant;

	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	private String posMappingLocation;

	AnalysisEngine mlTagger = null;
	AnalysisEngine postTagger = null;

	@Override
	public void initialize(final UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		mlTagger = AnalysisEngineFactory
				.createEngine(FlexTag.class, FlexTag.PARAM_LANGUAGE, language,
						FlexTag.PARAM_VARIANT, variant,
						FlexTag.PARAM_POS_MAPPING_LOCATION, posMappingLocation);
		postTagger = AnalysisEngineFactory
				.createEngine(
						PostprocessTagger.class,
						PostprocessTagger.PARAM_NAMELIST_FOLDER,
						"src/main/resources/namelists",
						PostprocessTagger.PARAM_NAMELIST_LOWER_CASE, true,
						PostprocessTagger.PARAM_POS_MAPPING_LOCATION, posMappingLocation,
						PostprocessTagger.PARAM_LANGUAGE, language
						);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		mlTagger.process(aJCas);
		postTagger.process(aJCas);
	}

	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {

	}

}
