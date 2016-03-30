/**
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.unidue.ltl.pos.trainmodel.tc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.lab.engine.TaskContext;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.lab.task.impl.ExecutableTaskBase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter.AdapterNameEntries;
import org.dkpro.tc.core.task.InitTask;
import org.dkpro.tc.core.util.ReportConstants;
import org.dkpro.tc.crfsuite.CRFSuiteAdapter;

import de.unidue.ltl.pos.trainmodel.tagger.PostprocessTagger;

public class PostPosUpdateTask extends ExecutableTaskBase implements Constants {

	String ID_UPDATED_CAS_FOLDER = "updatedCas";

	double correct = 0;
	double incorrect = 0;

	private String namedEntitiyFolder;

	private MappingProvider p;

	Log log;
	{
		log = LogFactory.getLog(getClass());
	}

	public PostPosUpdateTask(String namedEntitiyFolder,
			String posMappingLocation, String language) {
		this.namedEntitiyFolder = namedEntitiyFolder;

		p = new MappingProvider();
		p.setDefault(MappingProvider.LOCATION,
				"classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/"
						+ "${language}-${pos.tagset}-pos.map");
		p.setDefault(MappingProvider.BASE_TYPE,
				"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS");
		p.setDefault("pos.tagset", "default");
		p.setOverride(MappingProvider.LOCATION, posMappingLocation);
		p.setOverride(MappingProvider.LANGUAGE, language);
	}

	@Override
	public void execute(TaskContext aContext) throws Exception {

		File file = aContext.getFile(Constants.ID_OUTCOME_KEY,
				AccessMode.READONLY);
		List<String> readLines = FileUtils.readLines(file, "UTF-8");

		Map<String, String> id2label = getId2LabelMapping(readLines);
		Map<String, String> label2id = getLabel2IdMapping(readLines);

		Map<String, List<String>> groupByCas = sortByCas(readLines);
		Map<String, List<String>> normSeqGroupByCas = normalizeSequenceId(groupByCas);
		Map<String, List<String>> normTokGroupByCas = normalizeTokenId(normSeqGroupByCas);

		StringBuilder head = new StringBuilder();
		head.append(readLines.get(0));
		head.append("\n");
		head.append(readLines.get(1));
		
		StringBuilder body = new StringBuilder();

		AnalysisEngine postProcessingEngine = AnalysisEngineFactory
				.createEngine(PostprocessTagger.class,
						PostprocessTagger.PARAM_NAMELIST_FOLDER,
						namedEntitiyFolder,
						PostprocessTagger.PARAM_NAMELIST_LOWER_CASE, true
						 );
		for (String key : normTokGroupByCas.keySet()) {

			File testBinCasFolder = aContext.getFolder(
					InitTask.OUTPUT_KEY_TEST, AccessMode.READONLY);
			CollectionReader binCasReader = CollectionReaderFactory
					.createReader(BinaryCasReader.class,
							BinaryCasReader.PARAM_SOURCE_LOCATION,
							testBinCasFolder, BinaryCasReader.PARAM_PATTERNS,
							key + "_0.bin");

			JCas jcas = JCasFactory.createJCas();
			binCasReader.getNext(jcas.getCas());

			p.configure(jcas.getCas());

			List<TextClassificationSequence> sequences = JCasUtil
					.selectCovering(jcas, TextClassificationSequence.class, 0,
							jcas.getDocumentText().length());

			List<String> list = normTokGroupByCas.get(key);

			for (String e : list) {
				int[] seqTokId = getSeqTokId(e);
				int seqid = seqTokId[0];
				int tokid = seqTokId[1];

				Pattern pattern = Pattern.compile("=([0-9]+);([0-9]+)");
				Matcher m = pattern.matcher(e);
				m.find();

				String prediction = id2label.get(m.group(1));

				TextClassificationSequence sequence = sequences.get(seqid);
				List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
						sequence.getBegin(), sequence.getEnd());

				Token token = tokens.get(tokid);

				Type posTag = p.getTagType(prediction);
				POS pos = (POS) jcas.getCas().createAnnotation(posTag,
						token.getBegin(), token.getEnd());
				pos.setPosValue(prediction);
				pos.addToIndexes();
				token.setPos(pos);

				// prediction = updatePrediction(prediction, pos.getPosValue());
				// evaluate(prediction, gold);
				//
				// sb.append(key + "_" + seq + "_" + tok + "=" +
				// label2id.get(prediction) + ";"
				// + label2id.get(gold) + "\n");
			}

			postProcessingEngine.process(jcas);
			for (String e : list) {
				int[] seqTokId = getSeqTokId(e);
				int seqid = seqTokId[0];
				int tokid = seqTokId[1];

				Pattern pattern = Pattern.compile("=([0-9]+);([0-9]+)");
				Matcher m = pattern.matcher(e);
				m.find();

				String gold = id2label.get(m.group(2));

				TextClassificationSequence sequence = sequences.get(seqid);
				List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
						sequence.getBegin(), sequence.getEnd());

				POS pos = tokens.get(tokid).getPos();
				evaluate(pos.getPosValue(), gold);

				String predictedLabel = label2id.get(pos.getPosValue());
				String goldLabel = label2id.get(gold);

				if (predictedLabel == null) {
					
					List<String> keySet = new ArrayList<String>(id2label.keySet());
					int max=-1;
					for(String id : keySet){
						int c = Integer.valueOf(id);
						if(c > max){
							max = c;
						}
					}
					max++;
					label2id.put(pos.getPosValue(), ""+max);
					id2label.put(""+max, pos.getPosValue());
					head.append(" "+max+"=" + pos.getPosValue());
					predictedLabel = label2id.get(pos.getPosValue());
				}

				body.append(key + "_" + seqid + "_" + tokid + "="
						+ predictedLabel + ";" + goldLabel + "\n");
			}

			// writeUpdatedJcas(aContext, jcas);
		}

		writeAccuracyFile(aContext);
		
		String out = head.toString() + "\n" + body.toString();
		writeUpdatedId2OutcomeReport(aContext, out);

	}

	private int[] getSeqTokId(String s) {
		int first_underscore = s.indexOf("_");
		String seq = s.substring(0, first_underscore);
		int second_underscore = s.indexOf("_", first_underscore + 1);
		String tok = s.substring(first_underscore + 1, second_underscore);
		int seqid = Integer.valueOf(seq);
		int tokid = Integer.valueOf(tok);
		return new int[] { seqid, tokid };
	}

	private void evaluate(String prediction, String gold) {

		if (prediction.equals(gold)) {
			correct++;
		} else {
			incorrect++;
		}
	}

	private void writeAccuracyFile(TaskContext aContext) throws IOException {
		// file to hold prediction results
		File evalFile  = aContext.getFile(CRFSuiteAdapter.getInstance()
				.getFrameworkFilename(AdapterNameEntries.evaluationFile),AccessMode.READWRITE);
		
		Properties p = new Properties();
		p.setProperty(ReportConstants.CORRECT, correct+"");
		p.setProperty(ReportConstants.INCORRECT,incorrect+"");
		p.setProperty(ReportConstants.PCT_CORRECT , (correct / (correct + incorrect))+"");
		p.store(new FileOutputStream(evalFile), "results of post processing");
	}

	private void writeUpdatedId2OutcomeReport(TaskContext aContext,
			String report) throws IOException {
		File file = aContext.getFile(Constants.ID_OUTCOME_KEY,
				AccessMode.READWRITE);
		FileUtils.writeStringToFile(file, report);
	}

	private Map<String, String> getLabel2IdMapping(List<String> readLines) {
		String line = null;
		for (String s : readLines) {
			if (s.startsWith("#ID")) {
				continue;
			}
			line = s;
			break;
		}

		line = line.replaceAll("#labels ", "");

		String[] split = line.split(" ");

		Map<String, String> results = new HashMap<String, String>();
		for (String s : split) {
			String[] e = s.split("=");
			results.put(e[1], e[0]);
		}

		return results;

	}

	// private void writeUpdatedJcas(TaskContext aContext, JCas jcas) throws
	// ResourceInitializationException, AnalysisEngineProcessException
	// {
	// File jcasFolder = aContext.getFolder(ID_UPDATED_CAS_FOLDER,
	// AccessMode.READWRITE);
	//
	// AnalysisEngine engine =
	// AnalysisEngineFactory.createEngine(BinaryCasWriter.class,BinaryCasWriter.PARAM_TARGET_LOCATION,
	// jcasFolder);
	// engine.process(jcas);
	// }

	private Map<String, List<String>> normalizeTokenId(
			Map<String, List<String>> normSeqGroupByCas) {
		List<String> keySet = new ArrayList<String>(normSeqGroupByCas.keySet());
		Collections.sort(keySet);

		for (String key : keySet) {
			List<String> list = normSeqGroupByCas.get(key);

			List<String> normList = new ArrayList<>();
			for (String s : list) {
				int first_underscore = s.indexOf("_");
				String seq = s.substring(0, first_underscore);

				int second_underscore = s.indexOf("_", first_underscore + 1);
				String tok = s.substring(first_underscore + 1,
						second_underscore);

				String pad = "000000";
				int l = pad.length() - tok.length();
				if (l > 0) {
					pad = pad.substring(0, l);
				}

				pad += tok;
				String norm = seq + "_" + pad + "_"
						+ s.substring(second_underscore + 1);
				normList.add(norm);
			}

			Collections.sort(normList);
			normSeqGroupByCas.put(key, normList);
		}
		// for (String key : keySet) {
		// System.out.println(key);
		// List<String> list = normSeqGroupByCas.get(key);
		// for(String s : list){
		// System.out.println(s);
		// }
		// }

		return normSeqGroupByCas;
	}

	private Map<String, List<String>> normalizeSequenceId(
			Map<String, List<String>> groupByCas) {
		List<String> keySet = new ArrayList<String>(groupByCas.keySet());
		Collections.sort(keySet);

		for (String key : keySet) {

			Set<String> distinctSequenceIds = new HashSet<>();
			List<String> list = groupByCas.get(key);
			for (String e : list) {
				String[] split = e.split("_");
				distinctSequenceIds.add(split[0]);
			}

			List<String> updated = new ArrayList<>();
			for (int i = 0; i < distinctSequenceIds.size(); i++) {
				Collections.sort(list);
				for (String e : list) {
					String[] split = e.split("_");

					e = i + e.substring(split[0].length());
					updated.add(e);
				}
			}
			groupByCas.put(key, updated);

		}

		return groupByCas;
	}

	private Map<String, List<String>> sortByCas(List<String> readLines) {
		Map<String, List<String>> casContents = new HashMap<>();

		for (String s : readLines) {
			if (s.startsWith("#")) {
				continue;
			}

			String[] info = s.split("_");

			List<String> list = casContents.get(info[0]);
			if (list == null) {
				list = new ArrayList<String>();
			}

			s = s.substring(info[0].length() + "_".length());
			list.add(s);
			casContents.put(info[0], list);
		}
		return casContents;
	}

	private Map<String, String> getId2LabelMapping(List<String> readLines) {
		String line = null;
		for (String s : readLines) {
			if (s.startsWith("#ID")) {
				continue;
			}
			line = s;
			break;
		}

		line = line.replaceAll("#labels ", "");

		String[] split = line.split(" ");

		Map<String, String> results = new HashMap<String, String>();
		for (String s : split) {
			String[] e = s.split("=");
			results.put(e[0], e[1]);
		}

		return results;
	}

}
