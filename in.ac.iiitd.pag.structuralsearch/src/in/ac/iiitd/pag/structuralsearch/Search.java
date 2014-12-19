package in.ac.iiitd.pag.structuralsearch;

import in.ac.iiitd.pag.util.ASTUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.LuceneUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Search {
	static String operatorsFile = "";
	//static String projectData = "";
	static String soIndexFilePath = "";
	static String stopFilePath = "";
	static String topic = "factorial";
	
	static {
		Properties props = FileUtil.loadProps();
		if (props == null)
			System.out.println("ERROR: Cannot search. Cannot read properties.");
		operatorsFile = props.getProperty("OPERATORS_FILE");
		//projectData = props.getProperty("TESTDATA_INDEX_FILE_PATH");
		soIndexFilePath = props.getProperty("INDEX_FILE_PATH");
		stopFilePath = props.getProperty("STOP_FILE_PATH");

	}
	

	public static void main(String[] args) {
		
		//LuceneUtil.checkIndex(projectData);
		//LuceneUtil.checkIndex(soIndexFilePath);

		searchTopic(topic, 500, true);
	}

	public static List<String> searchTopic(String queryString, int maxResults,
			boolean showCodeResults) {

		int right = 0;
		int total = 0;

		List<CodeSnippet> results = SearchUtil.searchIndex(queryString,
				"title", 0, soIndexFilePath, maxResults, showCodeResults,
				operatorsFile);

		System.out.println("Found " + results.size() + " results.");

		List<CodeSnippet> filteredResults = new ArrayList<CodeSnippet>();

		for (CodeSnippet result : results) {
			int complexity = ASTUtil.getStructuralComplexity(result.methodDef);
			String title = result.title;
			if ((complexity > 0) && (!title.contains("C#"))) {
				result.complexity = complexity;
				filteredResults.add(result);
			}
		}

		System.out.println("Found " + filteredResults.size()
				+ " results with more than 0 complexity.");

		filteredResults = Ranker.applyStructureCountBoost(filteredResults); // sort
																			// by
																			// structural
																			// similarity

		System.out.println("Found " + filteredResults.size()
				+ " after duplicate removal.");

		String output = "";
		int[] complexities = new int[filteredResults.size()];
		int i = 0;
		int resultCount = 0;
		for (CodeSnippet result : filteredResults) {
			complexities[i++] = result.complexity;
			System.out.print(result.complexity + " ");
		}
		System.out.println();

		List<String> filteredResultsFinal = new ArrayList<String>();
		int limit = findLimit(complexities);
		for (CodeSnippet result : filteredResults) {
			if (showCodeResults && result.complexity >= limit) {
				resultCount++;
				output = output + result.id + " " + result.title + "\n"
						+ result.methodDef + "\n" + result.algoString + "\n"
						+ result.complexity + "\n\n\n";
				filteredResultsFinal.add(result.algoString);
			}
		}
		output = output + Arrays.toString(complexities);
		System.out.println("Found " + resultCount
				+ " after complexity filtering.");

		List<String> stops = FileUtil.readFromFileAsList(stopFilePath);

		String outputLowerCase = output.toLowerCase();

		outputLowerCase = outputLowerCase.trim().replaceAll(" +", " ");
		outputLowerCase = outputLowerCase.replaceAll("\n", " ");
		for (String stop : stops) {
			outputLowerCase = outputLowerCase.replaceAll("\\b" + stop + "\\b",
					"");
		}

		List<String> trigrams = Ngram.ngrams(3, outputLowerCase);
		List<String> bigrams = Ngram.ngrams(2, topic);
		Map<String, Integer> trigramsMap = new HashMap<String, Integer>();
		int count = 1;
		for (String trigram : trigrams) {
			trigram = trigram.trim();
			for (String bigram : bigrams) {
				if (trigram.contains(bigram)) {
					if (trigram.equalsIgnoreCase(topic))
						continue;
					// check validity
					String temp = trigram.replaceAll(" ", "");

					if (outputLowerCase.contains(temp)) {
						if (trigramsMap.get(trigram) != null) {
							count = trigramsMap.get(trigram) + 1;
						}
						;
						trigramsMap.put(trigram, count);
					}
				}
			}
		}

		bigrams = Ngram.ngrams(2, outputLowerCase);
		List<String> unigrams = Ngram.ngrams(1, topic);
		Map<String, Integer> bigramsMap = new HashMap<String, Integer>();
		count = 1;
		for (String bigram : bigrams) {
			bigram = bigram.trim();
			for (String unigram : unigrams) {
				unigram = unigram.trim();
				if (bigram.contains(unigram)) {
					// check validity
					if (bigram.equalsIgnoreCase(topic))
						continue;

					if (!bigram.trim().contains(" "))
						continue;
					String temp = bigram.replaceAll(" ", "");

					if (outputLowerCase.contains(temp)) {
						boolean isValid = true;
						for (String key : trigramsMap.keySet()) {
							if (key.contains(bigram)) {
								isValid = false;
								break;

							}
						}
						if (isValid) {
							if (trigramsMap.get(bigram) != null) {
								count = trigramsMap.get(bigram) + 1;
							}
							;
							trigramsMap.put(bigram, count);
						}
					}
				}
			}
		}

		for (String key : trigramsMap.keySet()) {
			System.out.println(key + trigramsMap.get(key));
		}

		/*
		 * i=0; resultCount = 0; String trigram =
		 * "binary search tree".toLowerCase(); output = ""; for(CodeSnippet
		 * result: filteredResults) { String code =
		 * result.methodDef.toLowerCase(); String title =
		 * result.title.toLowerCase();
		 * 
		 * if (title.contains(trigram) || title.contains(trigram.replaceAll(" ",
		 * ""))) { continue; }
		 * 
		 * if (code.contains(trigram) || code.contains(trigram.replaceAll(" ",
		 * ""))) { continue; }
		 * 
		 * complexities[i++] = result.complexity; if (showCodeResults &&
		 * result.complexity <= 150) { resultCount++; output = output +
		 * result.id + " " + result.title + "\n" + result.methodDef + "\n" +
		 * result.algoString + "\n" + result.complexity + "\n\n\n"; } } output =
		 * output + Arrays.toString(complexities); System.out.println("Found " +
		 * resultCount + " after ngram filtering.");
		 */

		try {
			FileUtil.saveFile(new File("C:\\temp\\"), "rankerResult.txt",
					output, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * for(CodeSnippet result: results) { List<CodeSnippet> testDataResults
		 * = SearchUtil.searchIndex(result.algoString, "algo", 1, projectData,
		 * maxResults, showCodeResults,operatorsFile); for(CodeSnippet
		 * testDataResult: testDataResults) { if (showCodeResults) {
		 * System.out.println(result.id + " " + result.title + "\n" +
		 * result.methodDef + "\n\n\n"); //System.out.println(result.id + " " +
		 * result.title + testDataResult.methodName + "**" +
		 * testDataResult.methodDef); } total++; if
		 * (testDataResult.methodName.equalsIgnoreCase
		 * (queryString.replaceAll(" ", ""))) {
		 * 
		 * right++; } } } if (total > 0) { int rightResultCount =
		 * getRightResultCount(queryString, projectData, maxResults,
		 * operatorsFile); if (rightResultCount > 0) { float recall = right *
		 * 1.0f/rightResultCount; System.out.println(queryString);
		 * System.out.println("Precision =" + right*1.0f/total);
		 * System.out.println("Recall = " + recall); System.out.println(""); } }
		 */
		return filteredResultsFinal;
	}

	private static int findLimit(int[] complexities) {
		int limit = 1;
		Arrays.sort(complexities);
		for (int i = 0; i <= complexities.length; i++) {

		}
		return limit;
	}

	public static int getRightResultCount(String queryString,
			String projectData, int maxResults, String operatorsFile) {
		int result = 0;
		List<CodeSnippet> testDataResults = SearchUtil.searchIndex(queryString,
				"methodname", 1, projectData, maxResults, true, operatorsFile);
		result = testDataResults.size();
		return result;
	}
}
