package de.unidue.ltl.pos.trainmodel.feature;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import com.google.gson.Gson;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

import de.unidue.ltl.pos.trainmodel.morph.Morph;

public class MorphFeat extends FeatureExtractorResource_ImplBase implements
		ClassificationUnitFeatureExtractor {

	public static final String PARAM_JSON = "json";
	@ConfigurationParameter(name = PARAM_JSON, mandatory = true)
	private File inputFile;

	private final String FEAT_WKL = "morphWkl";
	private final String FEAT_FORM = "morphForm";
	private final String FEAT_TYP = "morphTyp";
	private final String FEAT_NUM = "morphNum";
	private final String FEAT_KOMP = "morphKomp";
	private final String FEAT_DER = "morphDer";
	private final String FEAT_PARTICIP = "morphParticip";
	private final String FEAT_NUMERICAL_VAL = "morphNumericVal";

	private HashMap<String, Morph> map = null;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier,
			Map<String, Object> aAdditionalParams)
			throws ResourceInitializationException {
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}

		try {
			init();
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		return true;
	}

	private void init() throws TextClassificationException {

		if (map != null) {
			return;
		}
		map = new HashMap<String, Morph>();

		try {

			Gson gson = new Gson();
			String verbs = FileUtils.readFileToString(inputFile);
			Morph[] data = gson.fromJson(verbs, Morph[].class);
			for (Morph v : data) {
				map.put(v.getItem(), v);
			}

		} catch (Exception e) {
			throw new TextClassificationException(e);
		}
	}

	@Override
	public Set<Feature> extract(JCas view,
			TextClassificationUnit classificationUnit)
			throws TextClassificationException {

		String text = classificationUnit.getCoveredText();

		Morph m = map.get(text);
		if (m == null) {
			return dummyFeatureSet();
		}
		Set<Feature> s = new HashSet<Feature>();
		s.add(new Feature(FEAT_WKL, m.getWkl()));
		s.add(new Feature(FEAT_FORM, m.getForm()));
		s.add(new Feature(FEAT_TYP, m.getTyp()));
		s.add(new Feature(FEAT_NUM, m.getNum()));
		s.add(new Feature(FEAT_KOMP, m.getKomp()));
		s.add(new Feature(FEAT_DER, m.getDer()));
		s.add(new Feature(FEAT_PARTICIP, m.getParticip()));
		s.add(new Feature(FEAT_NUMERICAL_VAL, m.getNumericVal()));
		return s;
	}

	private Set<Feature> dummyFeatureSet() {
		Set<Feature> s = new HashSet<Feature>();

		s.add(new Feature(FEAT_WKL, "*"));
		s.add(new Feature(FEAT_FORM, "*"));
		s.add(new Feature(FEAT_TYP, "*"));
		s.add(new Feature(FEAT_NUM, "*"));
		s.add(new Feature(FEAT_KOMP, "*"));
		s.add(new Feature(FEAT_DER, "*"));
		s.add(new Feature(FEAT_PARTICIP, "*"));
		s.add(new Feature(FEAT_NUMERICAL_VAL, "*"));
		
		return s;
	}

}
