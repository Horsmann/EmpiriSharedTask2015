package de.unidue.ltl.pos.trainmodel.feature;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

public class IsInfinitiveModalVerb extends FeatureExtractorResource_ImplBase
		implements ClassificationUnitFeatureExtractor {
	private final String FEATURE_NAME = "isInfinitiveModalVerb";

	@Override
	public Set<Feature> extract(JCas view,
			TextClassificationUnit classificationUnit)
			throws TextClassificationException {

		String text = classificationUnit.getCoveredText().toLowerCase();

		boolean b = isFocusPartikel(text);

		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FEATURE_NAME, b ? 1 : 0));
		return features;
	}

	static boolean isFocusPartikel(String text) {
		for (String s : new String[] { "dürfen", "können", "mögen", "müssen", "sollen", "wollen"  }) {
			if (text.equals(s)) {
				return true;
			}
		}
		return false;
	}
}