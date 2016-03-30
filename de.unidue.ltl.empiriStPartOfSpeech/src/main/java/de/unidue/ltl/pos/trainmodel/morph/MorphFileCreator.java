package de.unidue.ltl.pos.trainmodel.morph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class MorphFileCreator {
	private static final String AMBIG = "ambig";
	private static final String NOT_AVAILABLE = "*";

	Set<Morph> info = new HashSet<Morph>();

	//the expected input file is downloadable from http://www.danielnaber.de/morphologie/
	public static void main(String[] args) throws Exception {
		MorphFileCreator parser = new MorphFileCreator();
		parser.parse(args);
	}

	private void parse(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(args[0]))));

		List<String> buffer = null;
		buffer = readBlock(br);

		while (!buffer.isEmpty()) {
			String item = getItem(buffer);

			FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
			getWkls(buffer, fd);

			if (fd.getB() == 0) {
				buffer = readBlock(br);
				continue;
			}
			String wkl = fd.getMostFrequentSamples(1).get(0);

			Morph make = makeMorphObject(item, wkl, buffer);
			if (make != null) {
				info.add(make);
			}

			buffer = readBlock(br);
		}
		br.close();

		writeMorph();
	}

	private void writeMorph() throws IOException {

		try (Writer writer = new FileWriter("target/morph.json")) {
			Gson gson = new GsonBuilder().create();
			gson.toJson(info, writer);
		}

	}

	private Morph makeMorphObject(String item, String wkl, List<String> buffer) {

		if (wkl.equals("VER")) {
			String form = getForm(wkl, buffer);
			String typ = getTyp(wkl, buffer);
			String num = getNum(wkl, buffer);
			Morph verb = new Morph();
			verb.makeVerb(item, wkl, form, typ, num);
			return verb;
		}
		if (wkl.equals("ADJ")) {
			String komp = getKomp(wkl, buffer);
			Morph adj = new Morph();
			adj.makeAdj(item, wkl, komp);
			return adj;
		}
		if (wkl.equals("ADV")) {
			String typ = getTyp(wkl, buffer);
			Morph adj = new Morph();
			adj.makeAdv(item, wkl, typ);
			return adj;
		}
		if (wkl.equals("ART")) {
			String typ = getTyp(wkl, buffer);
			Morph art = new Morph();
			art.makeArt(item, wkl, typ);
			return art;
		}
		if (wkl.equals("PA1")) {
			String der = getDer(wkl, buffer);
			String kmp = getKomp(wkl, buffer);
			String num = getNum(wkl, buffer);
			Morph pa = new Morph();
			pa.makeParticip("1", item, wkl, der, num, kmp);
			return pa;
		}
		if (wkl.equals("PA2")) {
			String der = getDer(wkl, buffer);
			String kmp = getKomp(wkl, buffer);
			String num = getNum(wkl, buffer);
			Morph pa = new Morph();
			pa.makeParticip("2", item, wkl, der, num, kmp);
			return pa;
		}

		if (wkl.equals("SUB")) {
			String der = getDer(wkl, buffer);
			String num = getNum(wkl, buffer);
			Morph sub = new Morph();
			sub.makeSubstantiv(item, wkl, der, num);
			return sub;
		}

		if (wkl.equals("PRO")) {
			String typ = getTyp(wkl, buffer);
			String num = getNum(wkl, buffer);
			Morph pro = new Morph();
			pro.makePronomen(item, wkl, typ, num);
			return pro;
		}

		if (wkl.equals("EIG")) {
			Morph eig = new Morph();
			eig.makeDefault(item, wkl);
			return eig;
		}

		if (wkl.equals("INJ")) {
			Morph inj = new Morph();
			inj.makeDefault(item, wkl);
			return inj;
		}

//		if (wkl.equals("PRP")) {
//			Morph prp = new Morph();
//			prp.makeDefault(item, wkl);
//			return prp;
//		}
//
//		if (wkl.equals("ABK")) {
//			Morph abk = new Morph();
//			abk.makeDefault(item, wkl);
//			return abk;
//		}
//
//		if (wkl.equals("ZUS")) {
//			Morph abk = new Morph();
//			abk.makeDefault(item, wkl);
//			return abk;
//		}
//
//		if (wkl.equals("KON")) {
//			Morph abk = new Morph();
//			abk.makeDefault(item, wkl);
//			return abk;
//		}

		if (wkl.equals("ZAL")) {
			Morph zal = new Morph();
			String numericVal = getNumericValue(wkl, buffer);
			zal.makeZahl(item, wkl, numericVal);
			return zal;
		}

		if (wkl.equals("NEG")) {
			Morph neg = new Morph();
			neg.makeDefault(item, wkl);
			return neg;
		}

//		if (wkl.equals("NIL")) {
//			return new Morph();
//		}

		return null;
//		throw new UnhandledException(new Throwable("WKL [" + wkl
//				+ "] unhandeled"));
	}

	private String getNumericValue(String wkl, List<String> buffer) {
		return getElement("zahl", buffer, wkl);
	}

	private String getDer(String wkl, List<String> buffer) {
		return getElement("der", buffer, wkl);
	}

	private String getElement(String element, List<String> buffer, String wkl) {
		FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
		for (int i = 1; i < buffer.size(); i++) {
			String l = buffer.get(i);

			if (!l.contains("wkl=\"" + wkl)) {
				continue;
			}

			Pattern p = Pattern.compile(element + "=\"([A-Z]+)\"");
			Matcher m = p.matcher(l);
			if (m.find()) {
				fd.addSample(m.group(1), 1);
			}

		}
		if (fd.getB() > 1) {
			return AMBIG;
		}
		if (fd.getB() == 0) {
			return NOT_AVAILABLE;
		}
		return fd.getMostFrequentSamples(1).get(0);
	}

	private String getKomp(String wkl, List<String> buffer) {
		return getElement("komp", buffer, wkl);
	}

	private String getNum(String wkl, List<String> buffer) {
		return getElement("num", buffer, wkl);
	}

	private String getTyp(String wkl, List<String> buffer) {
		return getElement("typ", buffer, wkl);
	}

	private String getForm(String wkl, List<String> buffer) {
		return getElement("form", buffer, wkl);
	}

	private Set<String> getWkls(List<String> buffer,
			FrequencyDistribution<String> fd) {
		Set<String> wkls = new HashSet<String>();

		for (int i = 1; i < buffer.size(); i++) {
			String l = buffer.get(i);
			Pattern p = Pattern.compile(" wkl=\"([A-Z0-9]+)\"");
			Matcher m = p.matcher(l);
			if (m.find()) {
				fd.addSample(m.group(1), 1);
			}
		}

		return wkls;
	}

	private String getItem(List<String> buffer) {
		String string = buffer.get(0);
		String replace = string.replaceAll("<form>", "");
		replace = replace.replaceAll("</form>", "");
		return replace;
	}

	private List<String> readBlock(BufferedReader br) throws Exception {
		List<String> buf = new ArrayList<String>();
		String l = null;
		while ((l = br.readLine()) != null) {
			if (l.startsWith("<inflections>")) {
				continue;
			}
			if (l.startsWith("<item>")) {
				continue;
			}
			if (l.startsWith("<?xml")) {
				continue;
			}
			if (l.startsWith("<!--")) {
				continue;
			}
			if (l.startsWith("#")) {
				continue;
			}
			if (l.startsWith("-->")) {
				continue;
			}

			if (l.startsWith("</item>")) {
				break;
			}

			buf.add(l);
		}

		return buf;
	}

}
