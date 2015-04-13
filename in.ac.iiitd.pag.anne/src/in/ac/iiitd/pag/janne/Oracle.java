package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Oracle {
	
	public static void main(String[] args){
		List<String> systemAnnotations = FileUtil.readFromFileAsList("systemAnnotations.txt");
		List<String> entityNames = FileUtil.readFromFileAsList(ConfigUtil.getInputStream("knownEntities.txt"));
		computePR(systemAnnotations, entityNames);
	}

	
	/**
	 * Takes in path to Oracle file and Retrieval generated file as parameters
	 * Computes average precision and recall values
	 * @param entityNames 
	 * @param args
	 */
	public static void computePR(List<String> systemAnnotations, List<String> entityNames) {
		
		try {
			String manualAnnFile = "C:\\git\\IR_Project\\Oracle_Code_ManuallyAnnotated.txt";
			/* String manualFileContents = FileUtil.readFromFile(manualAnnFile);
			System.out.println(manualFileContents);
			String systemFileContents = FileUtil.readFromFile(systemAnnFile);
			System.out.println(systemFileContents); */
			
			List<String> manualAnnotations = FileUtil.readFromFileAsList(manualAnnFile);		
			Map<String,Set<String>> manualTags = buildAnnotations(manualAnnotations, entityNames);
			
					
			Map<String,Set<String>> systemTags = buildAnnotations(systemAnnotations, null);			
			List<String> code = removeAnnotations(systemAnnotations);
			
			float averagePrecision = 0;
			float averageRecall = 0;
			int annotatedLinesOfCode = 0;
			for(String statement: code) {
				float precision = computePrecision(manualTags.get(statement), systemTags.get(statement));
				float recall = computeRecall(manualTags.get(statement), systemTags.get(statement));
				//System.out.println(MessageFormat.format("P={0}, R={1}", precision, recall));
				if ((precision >= 0) && (recall >= 0)) {
					averagePrecision += precision;
					averageRecall += recall;
					annotatedLinesOfCode++;
				}
			}
			averagePrecision = averagePrecision * 1.0f / annotatedLinesOfCode;
			averageRecall = averageRecall * 1.0f / annotatedLinesOfCode;
			float f1 = 2 * averagePrecision * averageRecall / (averagePrecision + averageRecall);
			System.out.println(MessageFormat.format("P={0}, R={1} F1={2}", averagePrecision, averageRecall, f1));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		

	}

	private static float computeRecall(Set<String> manualTags, Set<String> systemTags) {
		float recall = 0;
		int relevantCount = 0;
		if (manualTags == null) return 1f;
		if ((manualTags.size() == 0) && (systemTags.size() == 0)) return -1f;
		if ((manualTags.size() == 0)) return 1f;
		
		for(String tag: manualTags) {
			if (systemTags.contains(tag)) {
				relevantCount++;
			}
		}
		if (relevantCount > 0)
			recall = relevantCount * 1.0f / manualTags.size();
		return recall;
	}
	
	private static float computePrecision(Set<String> manualTags, Set<String> systemTags) {
		float precision = 0;
		int relevantCount = 0;
		if ((manualTags.size() == 0) && (systemTags.size() == 0)) return -1f;
		if (systemTags == null) return precision;		
		for(String tag: systemTags) {
			if (manualTags.contains(tag)) {
				relevantCount++;
			}
		}
		if (relevantCount > 0)
			precision = relevantCount * 1.0f / systemTags.size();
		return precision;
	}

	private static List<String> removeAnnotations(List<String> systemAnnotations) {
		List<String> code = new ArrayList<String>();
		if (systemAnnotations == null) return code;
		for(String line: systemAnnotations) {
			String[] parts = line.split(":=");
			if (parts[0].trim().length() == 0) continue;
			code.add(parts[0].trim());
		}
		return code;
	}

	private static Map<String, Set<String>> buildAnnotations(
			List<String> manualAnnotations, List<String> entityNames) {
		Map<String,Set<String>> tagAssociations = new HashMap<String, Set<String>>();
		for(String line: manualAnnotations) {
			line = line.trim();
			if (line.length() == 0) continue;
			String[] parts = line.split(":=");
			if (parts.length == 1) {
				Set<String> tags = new HashSet<String>();
				tagAssociations.put(parts[0].trim(), tags);
			}
			if (parts.length == 2) {
				Set<String> tags = new HashSet<String>();
				String[] tokens = parts[1].split(" ");
				for(String token: tokens) {
					if (token.trim().length() == 0) continue;
					if (entityNames != null) {
						if (entityNames.contains(token))
								tags.add(token);
					} else {
						tags.add(token);
					}
				}
				tagAssociations.put(parts[0].trim(), tags);
			}
		}
		return tagAssociations;				
	}

}
