package in.ac.iiitd.pag.janne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.NGramBuilder;
import in.ac.iiitd.pag.util.StringUtil;

public class EntityTaggerUINgram {
	public static void main(String[] args) {
		String fileName = "snippet.java";
		String outputFileName = "systemAnnotations.txt";
		int threshold = 500;
		int k1 = 1;
		int k2 = 1;
		if (args.length == 5) {
			fileName = args[0];
			outputFileName = args[1];
			threshold = Integer.parseInt(args[2]);	
			k1 = Integer.parseInt(args[3]);
			k2 = Integer.parseInt(args[4]);
		}
		String code = FileUtil.readFromFile(fileName);

		Map<String, Map<String, Integer>> allWeightsUnigram = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> allWeightsngram = new HashMap<String, Map<String, Integer>>();
		
		List<String> entityNames = FileUtil.readFromFileAsList(ConfigUtil.getInputStream("knownEntities.txt"));

		for(String entityName: entityNames) {
			String unigramPatternTFFile = entityName + "-NormalizedTF.txt";
			String ngramPatternTFFile = entityName + "-Long-Normalized.txt";
			Map<String,Integer> ngramEntityTF = FileUtil.getMapFromFile(ngramPatternTFFile);
			Map<String,Integer> unigramEntityTF = FileUtil.getMapFromFile(unigramPatternTFFile);
			
			allWeightsUnigram.put(entityName, unigramEntityTF);
			allWeightsngram.put(entityName, ngramEntityTF);
		}		
		
		String taggedCode = "";
		try {
			//computePRPerEntity(code, allWeights, entityNames);
			computeAveragePR(code, allWeightsUnigram, allWeightsngram, entityNames, outputFileName, threshold, k1, k2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void computeAveragePR(String code,
			Map<String, Map<String, Integer>> allWeightsUnigram, Map<String, Map<String,Integer>> allWeightsngram, List<String> entityNames, String annotatedFile
			, int confidenceCutOff, int k1, int k2)
			throws IOException {
		String taggedCode = tag(code, allWeightsUnigram, allWeightsngram, confidenceCutOff, k1, k2);			
		FileUtil.saveFile(annotatedFile, taggedCode);
		System.out.println(taggedCode);
		List<String> systemAnnotations = FileUtil.readFromFileAsList(annotatedFile);
		Oracle.computePR(systemAnnotations, entityNames, "snippet - annotated.java");	
	}
	
	

	private static void computePRPerEntity(String code,
			Map<String, Map<String, Integer>> allWeightsUnigram,
			Map<String, Map<String, Integer>> allWeightsngram, List<String> entityNames, int confidenceCutOff,
			int k1, int k2)
			throws IOException {
		String taggedCode;
		for(String entityName: entityNames) {
			List<String> justOneEntity = new ArrayList<String>();
			justOneEntity.add(entityName);
			System.out.println(entityName);
			for(int factor =0; factor < 11; factor++) {					
				System.out.print(factor/10f + " ");
				taggedCode = tag(code, allWeightsUnigram,  allWeightsngram, confidenceCutOff, k1, k2);			
				FileUtil.saveFile("systemAnnotations.txt", taggedCode);
				List<String> systemAnnotations = FileUtil.readFromFileAsList("systemAnnotations.txt");
				Oracle.computePR(systemAnnotations, justOneEntity, "snippet - annotated.java");	
			}
		}
	}

	private static int getCutOff(Map<String, Float> weights, float cutoffFactor) {
		float maxWt = 0;
		float minWt = 0;
		for(String entity: weights.keySet()) {
			if (weights.get(entity) > maxWt) maxWt = weights.get(entity);
			if (weights.get(entity) < minWt) minWt = weights.get(entity);
		}		
		return (int) ((maxWt - minWt)/2 * cutoffFactor);
	}

	private static String tag(String code,
			Map<String, Map<String, Integer>> allWeightsUnigram,
			Map<String, Map<String, Integer>> allWeightsngram, int confidenceCutOff, int k1, int k2) throws IOException {
		String taggedCode = "";
		String[] lines = code.split("\r\n|\r|\n");
		String[] tagsForLines = new String[lines.length];
		Map<String,Set<String>> tagAssociations = new HashMap<String, Set<String>>();
		float maxLineWeight = 0;
		int index = 0;
		for(String lineItem: lines) {
			   //tagAssociations = new HashMap<String, List<String>>();
			   String preservedLine = lineItem;
			   lineItem = lineItem.toLowerCase();
			   lineItem = lineItem.trim();
			   lineItem = StringUtil.cleanCode(lineItem);
			   //System.out.println(lineItem);
			   if (inValid(lineItem)) {
				   taggedCode = taggedCode + lineItem + "\n";
				   continue;
			   }
			   
			   for(String entityName: allWeightsUnigram.keySet()) {
				   Map<String, Integer> unigramEntityTF = allWeightsUnigram.get(entityName);		
				   Map<String, Integer> ngramPatternsTF = allWeightsngram.get(entityName);		
				   String newLine = "";
				   float lineWeight = 0;
				   int docLen = 0;
				   int confidence = NGramLineRanker.score(lineItem, unigramEntityTF, ngramPatternsTF, k1, k2);
				   
				   if (confidence > confidenceCutOff) {					   
					   Set<String> entityNamesAssociated = null;
					   if (tagAssociations.containsKey(preservedLine)) {
						   entityNamesAssociated = tagAssociations.get(preservedLine);
					   } else {
						   entityNamesAssociated = new HashSet<String>();						   
					   }
					   entityNamesAssociated.add(entityName);
					   tagAssociations.put(preservedLine, entityNamesAssociated);					   
				   } 
			   }
		   }                	
		
		taggedCode = getCode(code, tagAssociations);
		return taggedCode;
	}

	/*private static int isEntityFound(String lineItem,
			Map<String, Integer> weights) throws IOException {
		boolean entityFound = false;
		String line = formatLine(lineItem);
		Set<String> ngrams = new HashSet<String>();
		for(int i=6; i>0; i--) {
			Set<String> ngramsFound = NGramBuilder.getSequentialNgramsAnyN(lineItem, i);
			for(String ngramFound: ngramsFound) {
				for(String patternInCode: weights.keySet()) {
					if (ngramsFound.contains(patternInCode)) {
						entityFound = true;
						break;
					}
				}
			}
		}
		return entityFound;
	}
*/
	private static String formatLine(String lineItem) throws IOException {
		String newLine = "";
		List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
		for(String token: tokens) {
			   if (token.contains(".")) {
				   String[] subTokens = token.split("\\.");
				   for(String subToken: subTokens) {
					   newLine = newLine + subToken + " ";
				   }
			   }  else {
				   newLine = newLine + token + " ";
			   }
		}
		return newLine.trim();
	}

	private static String getCode(String code, Map<String, Set<String>> tagAssociations) {
		String taggedCode = "";
		String[] lines = code.split("\r\n|\r|\n");
		for (String line: lines) {			
			String tags = "";
			if ((line.trim().length() > 0)&&(tagAssociations.get(line) != null)&&(tagAssociations.get(line).size() > 0)) {
				tags = StringUtil.getAsCSV(tagAssociations.get(line)).replace(",", " ");
				taggedCode = taggedCode + line +  " \t:= " + tags + "\n"; //"(" + wt + ")" +
			} else {
				taggedCode = taggedCode + line + "\n";
			}				
		}
		return taggedCode;
	}

	private static boolean inValid(String lineItem) {
		boolean invalid = false;
		if (lineItem.startsWith("//")) invalid = true;
		   if (lineItem.length() <=2 ) invalid = true;
		   if (lineItem.startsWith("import ")) invalid = true;
		   if (lineItem.startsWith("public")) invalid = true;
		   if (lineItem.startsWith("private")) invalid = true;
		   if (lineItem.startsWith("protected")) invalid = true;
		   if (lineItem.startsWith("class")) invalid = true;
		return invalid;
	}
}
