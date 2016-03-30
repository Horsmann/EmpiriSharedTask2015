package de.unidue.ltl.pos.trainmodel.tagger.eval;

import de.unidue.ltl.pos.trainmodel.misc.SharedTaskReader;

public class GermanReader {

	public static ReaderConfiguration getShrdTaskTestData(String testFolder,
			String mapping) {
		return new ReaderConfiguration(SharedTaskReader.class, "shrdTsk", "de",
				mapping, testFolder, new String[] { "*.txt" });
	}
}
