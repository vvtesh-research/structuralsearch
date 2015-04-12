package in.ac.iiitd.pag.structuralsearch;

import in.ac.iiitd.pag.oracle.Oracle;
import in.ac.iiitd.pag.util.ASTUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.LuceneUtil;
import in.ac.iiitd.pag.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Structural Search takes a topic and gives out variant implementations.
 * 
 * @author Venkatesh
 *
 */
public class Search {
	static String operatorsFile = "";  //List of operators that we recognize.
	static String soIndexFilePath = ""; //Path where stackoverflow is indexed
	static String stopFilePath = ""; //Path for file containing stop words.
	static String topic = "reverse string"; //Topic to search.
	
	static {
		Properties props = FileUtil.loadProps();
		if (props == null)
			System.out.println("ERROR: Cannot search. Cannot read properties.");
		operatorsFile = props.getProperty("OPERATORS_FILE");
		//projectData = props.getProperty("TESTDATA_INDEX_FILE_PATH");
		soIndexFilePath = props.getProperty("INDEX_FILE_PATH156");
		stopFilePath = props.getProperty("STOP_FILE_PATH");
	}
	

	public static void main(String[] args) {
		
		//LuceneUtil.checkIndex(projectData);
		//LuceneUtil.checkIndex(soIndexFilePath);
		/*Map<String, Integer> map = LuceneUtil.printAll("C:\\data\\minedsnippets\\algorepoindex156", "topic");
		try {
			FileUtil.writeMapToFile(map, "c:\\temp\\topcs.txt", 0);
			Map<String, Set<String>>  topicVocabMap = LuceneUtil.getTopicVocabularyMap("C:\\data\\minedsnippets\\algorepoindex156");
			String output = "";
			for(String topic: topicVocabMap.keySet()) {
				output += topic + "," + StringUtil.getAsCSV(topicVocabMap.get(topic)) + "\n";
			}
			FileUtil.saveFile(new File("c:\\temp\\"), "topicVocab.txt", output, "");
			
			Map<String, Integer> wordTopicCountMap = new HashMap<String, Integer>();
			for(String topic: topicVocabMap.keySet()) {
				Set<String> vocab = topicVocabMap.get(topic);
				for(String vocabItem: vocab) {
					int count = 0;
					if (wordTopicCountMap.containsKey(vocabItem)) {
						count = wordTopicCountMap.get(vocabItem);
					}
					count++;
					wordTopicCountMap.put(vocabItem, count);
				}
			}
			FileUtil.writeMapToFile(wordTopicCountMap, "c:\\temp\\wordcountintopics.txt", 0);
		} catch (IOException e) {
			
			e.printStackTrace();
		}*/
		searchTopic(topic, 500, true);
	}
	
	public static List<CodeSnippet> searchTopic(String queryString, int maxResults,
			boolean showCodeResults) {

		//LuceneUtil.checkIndex(soIndexFilePath);
		
		List<CodeSnippet> filteredResults = SearchUtil.searchIndex(queryString,
				"body", 0, soIndexFilePath, maxResults, showCodeResults,
				operatorsFile);

		if (showCodeResults) System.out.println("Found " + filteredResults.size() + " results.");
		
		if (filteredResults.size() == 0) return filteredResults;
		
	   filteredResults = dropZeroComplexityResults(filteredResults);

		if (showCodeResults) System.out.println("Found " + filteredResults.size()
				+ " results with more than 0 complexity.");
		
		filteredResults = dropResultswithBadTitles(filteredResults);
		
		if (showCodeResults) System.out.println("Found " + filteredResults.size()
				+ " results without bad titles like c#.");

		validateHeterogeneity(filteredResults);
		
		filteredResults = Ranker.applyStructureCountBoost(filteredResults, 0.4f); //dup removal. sort by complexity.
			
		
		if (showCodeResults) System.out.println("Found " + filteredResults.size()
				+ " after duplicate removal.");

		
		filteredResults = dropResultsByComplexityCutoff(filteredResults, 10000);
		
		if (showCodeResults) System.out.println("Found " + filteredResults.size()
				+ " after complexity filtering.");
		
		Map<String, Integer> trigramsMap = getLatentTopics(filteredResults);

		if (showCodeResults) {
			for (String key : trigramsMap.keySet()) {
				System.out.println(key + " " +  trigramsMap.get(key));
			}
		}
		
		filteredResults = dropResultsOfLatentTopics(filteredResults, trigramsMap);
				
		if (showCodeResults) System.out.println("Found " + filteredResults.size() + " after latent topic drops.");

		saveResults(filteredResults, "rankerResult.txt");
				
 		return filteredResults;
	}

	private static void validateHeterogeneity(List<CodeSnippet> filteredResults) {
		Oracle.loadHeterogeneityBuckets();
		int noOfVariants = Oracle.getNoOfVariants(topic);
		int resultsCount = filteredResults.size();
		String output = "";
		for(float f=0f; f<1.1f; f+=0.1f) {
			List<CodeSnippet> results = Ranker.applyStructureCountBoost(filteredResults, f);
			float precision = 0f;
			float recall = 0f;
			DecimalFormat df = new DecimalFormat("#.##");
			int variantsInResults = getVariantCountInResults(results);
			int resultsSizeAfterDedup = results.size();
			precision = variantsInResults * 100.0f / resultsSizeAfterDedup;
			recall =  variantsInResults * 100.0f / noOfVariants;
			float f1score = 2 * precision * recall / (precision + recall);
			output = output + MessageFormat.format("{0},{1},{2},{3},{4},{5},{6},{7},{8}\n", f, topic, resultsCount, noOfVariants,results.size(),variantsInResults,df.format(precision),df.format(recall),df.format(f1score));
		}
		System.out.println(output);
	}

	public static String getAsString(List<CodeSnippet> filteredResults) {
		String output = "";
		for (CodeSnippet result : filteredResults) {				
			output = output + result.id + " " + result.title + "\n"
					+ result.methodDef + "\n" + result.algoString + "\n"
					+ result.complexity + "\n\n\n";
		}
		return output;
	}
	
	public static int getVariantCountInResults(List<CodeSnippet> filteredResults) {
		int vCount = 0;
		HashSet<String> variantsFound = new HashSet<String>();
		Oracle.loadHeterogeneityBuckets();
		for (CodeSnippet result : filteredResults) {	
			String variantName =  Oracle.getHeterogeneityBucket(topic, result.id);
			variantsFound.add(variantName);
		}
		vCount =  variantsFound.size();
		return vCount;
	}
	
	private static void saveResults(List<CodeSnippet> filteredResults, String fileName) {
		try {
			String output = "";
			HashSet<String> variantsFound = new HashSet<String>();
			Oracle.loadHeterogeneityBuckets();
			for (CodeSnippet result : filteredResults) {	
				String variantName =  Oracle.getHeterogeneityBucket(topic, result.id);
				variantsFound.add(variantName);
				output = output + Oracle.getHeterogeneityBucket(topic, result.id) + "\n";
				output = output + result.id + " " + result.title + "\n"
						+ result.methodDef + "\n" + result.algoString + "\n"
						+ result.complexity + "\n\n\n";				
			}
			System.out.println("Variants in results = " + variantsFound.size());
			System.out.println("Total variant for topic in db = " + Oracle.getNoOfVariants(topic));
			FileUtil.saveFile(new File("C:\\temp\\"), fileName,
					output, "");			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private static List<CodeSnippet> dropResultsOfLatentTopics(
			List<CodeSnippet> filteredResults, Map<String, Integer> trigramsMap) {
		//List<String> results = new ArrayList<String>();
		List<CodeSnippet> codeSnippets = new ArrayList<CodeSnippet>();
		
		
		for (CodeSnippet result : filteredResults) {
			String title = result.title.toLowerCase();
			boolean drop = false;
			for(String key: trigramsMap.keySet()) {
				if (title.contains(key.toLowerCase())) {
					drop = true;
					break;
				}
			}
			if (!drop) {				
				/*outputNoLT = outputNoLT + result.id + " " + result.title + "\n"
						+ result.methodDef + "\n" + result.algoString + "\n"
						+ result.complexity + "\n\n\n";
				results.add(result.algoString);*/
				codeSnippets.add(result);
				
			}
		}
		return codeSnippets;
	}

	private static List<CodeSnippet> dropResultsByComplexityCutoff(
			List<CodeSnippet> results, int limit) {
		List<CodeSnippet> filteredResults = new ArrayList<CodeSnippet>();
		for (CodeSnippet result : results) {
			if (result.complexity < limit) {				
				/* */
				//filteredResults.add(result.algoString);
				filteredResults.add(result);
			}
		}
		return filteredResults;
	}

	public static int[] getComplexities(List<CodeSnippet> filteredResults
			) {
		int i = 0;
		int[] complexities = new int[filteredResults.size()];
		for (CodeSnippet result : filteredResults) {
			complexities[i++] = result.complexity;			
		}
		return complexities;
	}

	private static List<CodeSnippet> dropZeroComplexityResults(
			List<CodeSnippet> results) {
		List<CodeSnippet> filteredResults = new ArrayList<CodeSnippet>();
		for (CodeSnippet result : results) {
			int complexity = ASTUtil.getStructuralComplexity(result.methodDef);
			String title = result.title;
			if ((complexity > 0) && (!title.toLowerCase().contains("c#"))) {
				result.complexity = complexity;
				filteredResults.add(result);
			}
		}
		return filteredResults;
	}
	
	private static List<CodeSnippet> dropResultswithBadTitles(
			List<CodeSnippet> results) {
		List<CodeSnippet> filteredResults = new ArrayList<CodeSnippet>();
		for (CodeSnippet result : results) {			
			String title = result.title;
			if (!title.toLowerCase().contains("c#")) {				
				filteredResults.add(result);
			}
		}
		return filteredResults;
	}

	private static Map<String, Integer> getLatentTopics(List<CodeSnippet> filteredResults) {
		String output = "";
		for (CodeSnippet result : filteredResults) {			
				output = output + result.id + " " + result.title + "\n"
						+ result.methodDef + "\n" + result.algoString + "\n"
						+ result.complexity + "\n\n\n";			
			
		}
		
		String outputLowerCase = output.toLowerCase();
		outputLowerCase = outputLowerCase.trim().replaceAll(" +", " ");
		outputLowerCase = outputLowerCase.replaceAll("\n", " ");
		List<String> stops = FileUtil.readFromFileAsList(stopFilePath);
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
		return trigramsMap;
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
