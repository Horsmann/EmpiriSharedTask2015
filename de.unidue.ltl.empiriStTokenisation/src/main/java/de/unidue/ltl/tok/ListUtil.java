package de.unidue.ltl.tok;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class ListUtil {
    
    public static String abbreviationFile = "";
    public static String apostrophJoinedWords="";

    public static boolean isAbbreviation(String string, Set<String> abbreviations) throws IOException {
        abbreviations = ListUtil.loadAbbreviations(abbreviations);
        return abbreviations.contains(string);
    }
    
    
    
	private static Set<String> loadAbbreviations(Set<String> abbreviations) throws IOException {
		if(abbreviations!=null){
			return abbreviations;
		}
		abbreviations = new HashSet<String>();
		List<String> readLines = FileUtils.readLines(new File(
		        abbreviationFile));
		for (String s : readLines) {
			if (s.startsWith("#")) {
				continue;
			}
			abbreviations.add(s.toLowerCase());
		}
		return abbreviations;
	}
	
	public static Set<String> loadSmilies(Set<String> smilies) throws IOException {
		if(smilies!=null){
			return smilies;
		}
		
		smilies = new HashSet<String>();
		List<String> readLines = FileUtils.readLines(new File(
				"src/main/resources/smilies/list.txt"));
		for (String s : readLines) {
			if (s.startsWith("#")) {
				continue;
			}
			smilies.add(s.toLowerCase());
		}
		return smilies;
	}

	public static Set<String> loadApostrophConnectedUnits(Set<String> apos) throws IOException {
		if(apos!=null){
			return apos;
		}
		apos = new HashSet<String>();
		List<String> readLines = FileUtils.readLines(new File(
				apostrophJoinedWords));
		for (String s : readLines) {
			if (s.startsWith("#")) {
				continue;
			}
			apos.add(s.toLowerCase());
		}
		return apos;
	}
}
