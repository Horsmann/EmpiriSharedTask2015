package de.unidue.ltl.pos.trainmodel.feature;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationUnit;

public class IsFocusPartikel extends FeatureExtractorResource_ImplBase
		implements ClassificationUnitFeatureExtractor {
	private final String FEATURE_NAME = "isFocusPartikel";

	@Override
	public Set<Feature> extract(JCas view,
			TextClassificationUnit classificationUnit)
			throws TextClassificationException {

		String text = classificationUnit.getCoveredText();

		boolean b = isFocusPartikel(text);

		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(FEATURE_NAME, b ? 1 : 0));
		return features;
	}

	static boolean isFocusPartikel(String text) {
		for (String s : new String[] { "allein", "allenfalls", "annähernd",
				"auch", "ausgerechnet", "bereits", "besonders", "bestenfalls",
				"bloß", "einzig", "erst", "etwa", "frühestens", "gar",
				"gerade", "lediglich", "mindestens", "noch", "nur", "schon",
				"selbst", "sogar", "spätestens", "vor allem ", "wenigstens",
				"zumindest" }) {
			if (text.equals(s)) {
				return true;
			}
		}
		return false;
	}
}