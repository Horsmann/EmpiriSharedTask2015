package de.unidue.ltl.pos.trainmodel.morph;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

public class LoadTest {
	public static void main(String[] args) throws Exception {

		Gson gson = new Gson();
		String verbs = FileUtils
				.readFileToString(new File("target/morph.json"));
		Morph[] data = gson.fromJson(verbs, Morph[].class);
		for (Morph v : data) {
			System.out.println(v.getItem() + " " + v.getWkl());
		}

	}
}
