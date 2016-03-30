package de.unidue.ltl.pos.trainmodel.feature;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

public class IsVerbPronounFusion extends FeatureExtractorResource_ImplBase
		implements ClassificationUnitFeatureExtractor {
	private final String FEATURE_NAME = "isVerbPronounFusion";

	@Override
	public Set<Feature> extract(JCas view,
			TextClassificationUnit classificationUnit)
			throws TextClassificationException {

		String text = classificationUnit.getCoveredText();
		boolean b = hasTypicalVerbPronounFusionSuffix(text);

		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FEATURE_NAME, b ? 1 : 0));

		return features;
	}

	static boolean hasTypicalVerbPronounFusionSuffix(String text) {
		// kannste, haste, biste, willste, kommste, säufste, etc...
		return Pattern.matches("[a-zäöüß]+ste", text);
	}

}
