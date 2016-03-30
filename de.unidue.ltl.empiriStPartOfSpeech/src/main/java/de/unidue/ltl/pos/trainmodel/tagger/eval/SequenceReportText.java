package de.unidue.ltl.pos.trainmodel.tagger.eval;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;


public class SequenceReportText extends SequenceReportAbstract{


	@Override
	public void initialize(final UimaContext context)
			throws ResourceInitializationException {
		SUFFIX =".txt";
		super.initialize(context);
		 
	}

	@Override
	protected void process(BufferedWriter bw, List<String> tokens,
			List<String> gold, List<String> pred) throws IOException {

		double accuracy = calculateAccuracy(gold, pred);
		int unitLength = getLongestToken(tokens);
		unitLength = setMinimalLength(unitLength);

		bw.write(String.format("ID: %5d, Accuracy %7.3f", sentId, accuracy));
		bw.write("\n");

		writeList(bw, tokens, unitLength, "");
		writeList(bw, gold, unitLength, "Gold: ");
		writeList(bw, pred, unitLength, "Pred: ");

		bw.write("\n");
	}

	private void writeList(BufferedWriter bw, List<String> content,
			int unitLength, String linePrefix) throws IOException {
		bw.write(String.format("%10s ", linePrefix));
		for (String unit : content) {
			bw.write(String.format("%" + unitLength + "s ", unit));
		}
		bw.write("\n");
	}

	private int setMinimalLength(int maxTokLen) {
		if (maxTokLen < 5) {
			return 5;
		}
		return maxTokLen;
	}

	private int getLongestToken(List<String> tokens) {
		int maxLen = 0;
		for (String s : tokens) {
			if (s.length() > maxLen) {
				maxLen = s.length();
			}
		}
		return maxLen;
	}

	private double calculateAccuracy(List<String> gold, List<String> pred) {
		double match = 0.0;
		for (int i = 0; i < pred.size(); i++) {
			if (gold.get(i).equals(pred.get(i))) {
				match++;
			}
		}

		return match / gold.size();
	}

}
