package de.unidue.ltl.tok;

public class SharedUtils {

	
	public static boolean checkPreviousNslotsForChar(String[] tokens, String string,
			int i, int window) {
		for (int idx = 1; i - idx >= 0 && idx <= window; idx++) {
			if (tokens[i - idx].equals(string)) {
				return true;
			}
		}

		return false;
	}
}
