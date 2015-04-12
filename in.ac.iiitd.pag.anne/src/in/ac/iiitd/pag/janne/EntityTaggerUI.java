package in.ac.iiitd.pag.janne;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

public class EntityTaggerUI {
	public static void main(String[] args) {
		String code = FileUtil.readFromFile("snippet.java");
		String entityName = "remove";
		Map<String, Float> weights = FileUtil.getFloatMapFromFile("weights-" + entityName + ".csv");		
		String taggedCode = "";
		try {
			taggedCode = tag(code, weights, entityName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(taggedCode);
	}

	public static String tag(String code, Map<String, Float> weights, String entityName) throws IOException {
		String taggedCode = "";
		String[] lines = code.split("\r\n|\r|\n");
		Map<String,Integer> weightedCode = new HashMap<String, Integer>();
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
			   
			  			   
			   List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
			   
			   String newLine = "";
			   float lineWeight = 0;
			   for(String token: tokens) {
				   if (token.contains(".")) {
					   String[] subTokens = token.split("\\.");
					   for(String subToken: subTokens) {
						   newLine = newLine + subToken + " ";
						   if (weights.containsKey(subToken)) {
							   newLine = newLine  + " "; //+ "(" + weights.get(subToken) + ")"
							   lineWeight = lineWeight + weights.get(subToken);							   
						   }
					   }
				   } else {
					   newLine = newLine + token + " ";
					   if (weights.containsKey(token)) {
						   newLine = newLine + " "; //"(" + weights.get(token) + ")"
						   lineWeight = lineWeight + weights.get(token);
						   
					   }
				   }
			   }		       
			   if (maxLineWeight < lineWeight) maxLineWeight = lineWeight;
			   weightedCode.put(newLine, (int) lineWeight );
		   }                	
		
		for (String codeLine: weightedCode.keySet()) {
			int wt = weightedCode.get(codeLine);
			if (wt == (int) maxLineWeight) {
				taggedCode = codeLine +  " \t\\\\" + entityName + "\n"; //"(" + wt + ")" +	
				break;
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
