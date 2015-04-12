package in.ac.iiitd.pag.janne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

public class EntityTaggerUI {
	public static void main(String[] args) {
		String code = FileUtil.readFromFile("snippet.java");
		System.out.println(code);
		Map<String, Map<String, Float>> allWeights = new HashMap<String, Map<String, Float>>();
		List<String> entityNames = FileUtil.readFromFileAsList(ConfigUtil.getInputStream("knownEntities.txt"));
		for(String entityName: entityNames) {
			Map<String, Float> weights = FileUtil.getFloatMapFromFile("weights-" + entityName + ".csv");
			allWeights.put(entityName, weights);
		}		
		
		String taggedCode = "";
		try {
			taggedCode = tag(code, allWeights);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(taggedCode);
	}

	private static int getCutOff(Map<String, Float> weights) {
		float maxWt = 0;
		float minWt = 0;
		for(String entity: weights.keySet()) {
			if (weights.get(entity) > maxWt) maxWt = weights.get(entity);
			if (weights.get(entity) < minWt) minWt = weights.get(entity);
		}		
		return (int) ((maxWt - minWt)/2 * 0.5);
	}

	public static String tag(String code,Map<String, Map<String, Float>>  allWeights) throws IOException {
		String taggedCode = "";
		String[] lines = code.split("\r\n|\r|\n");
		Map<String,List<String>> tagAssociations = new HashMap<String, List<String>>();
		float maxLineWeight = 0;
		for(String lineItem: lines) {
			
			   lineItem = lineItem.toLowerCase();
			   lineItem = lineItem.trim();
			   lineItem = StringUtil.cleanCode(lineItem);
			   //System.out.println(lineItem);
			   if (inValid(lineItem)) {
				   taggedCode = taggedCode + lineItem + "\n";
				   continue;
			   }
			   
			   for(String entityName: allWeights.keySet()) {
				   Map<String, Float> weights = allWeights.get(entityName);		
				   int cutoffWeight = getCutOff(weights);
				   List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
				   
				   String newLine = "";
				   float lineWeight = 0;
				   int docLen = 0;
				   for(String token: tokens) {
					   if (token.contains(".")) {
						   String[] subTokens = token.split("\\.");
						   for(String subToken: subTokens) {
							   newLine = newLine + subToken + " ";
							   if (weights.containsKey(subToken)) {
								   docLen++;
								   newLine = newLine   + " "; //+  +  "(" + weights.get(subToken) + ")"
								   lineWeight = lineWeight + weights.get(subToken);							   
							   }
						   }
					   } else {
						   newLine = newLine + token + " ";
						   if (weights.containsKey(token)) {
							   newLine = newLine  + " "; //+ "(" + weights.get(token) + ")"
							   lineWeight = lineWeight + weights.get(token);
							   docLen++;
						   }
					   }
				   }		       
				   if (lineWeight/docLen > cutoffWeight) {					   
					   List<String> entityNamesAssociated = null;
					   if (tagAssociations.containsKey(newLine)) {
						   entityNamesAssociated = tagAssociations.get(newLine);
					   } else {
						   entityNamesAssociated = new ArrayList<String>();						   
					   }
					   entityNamesAssociated.add(entityName);
					   tagAssociations.put(newLine, entityNamesAssociated);
				   }
			   }
		   }                	
		
		taggedCode = getCode(tagAssociations);
		if (tagAssociations.keySet().size() == 0) taggedCode = "";
		return taggedCode;
	}

	private static String getCode(Map<String, List<String>> tagAssociations) {
		String taggedCode = "";
		for (String codeLine: tagAssociations.keySet()) {			
			String tags = "";
			if (tagAssociations.get(codeLine) != null) {
				tags = StringUtil.getAsCSV(tagAssociations.get(codeLine));
			}
			taggedCode = taggedCode + codeLine +  " \t\\\\" + tags + "\n"; //"(" + wt + ")" +	
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
