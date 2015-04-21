package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class EntityMiner {
	public static void main(String[] args) {
		int speedUp = 50;
		if (args.length > 0) {
			try {
				speedUp = Integer.parseInt(args[0]);
			} catch (Exception e) {}
		}
		mine(speedUp);
	}

	private static void mine(int speedUp) {
		try {
			long startTime = (new Date()).getTime();
			
			Properties props = FileUtil.loadProps();
			if (props == null) return;			
						
			String filePath = props.getProperty("FILE_PATH");
			List<String> entities = FileUtil.readFromFileAsList("knownEntities.txt");
			Map<String, Float> collectionTF = FileUtil.getLastNFloatMapFromFile("allCodeTF1.csv", 100);
			collectionTF = EntityTagger.normalize(collectionTF);
			FileUtil.writeMapToFileFloat(collectionTF, "normalizedAllCodeTF.csv", 0);
			Map<String, Set<String>> skipEntities = EntityTagger.getSkipLists("skipList.txt");
			for(String entityName: entities) {
				System.out.println("Processing " + entityName);
				Set<String> skipEntitySet = skipEntities.get(entityName);
				Map<String,Integer> weightedCode = EntityTagger.computeTermWeights(filePath, entityName, collectionTF, skipEntitySet, speedUp);
				FileUtil.writeMapToFile(weightedCode, entityName + "-lines-weighted.txt", 0); 
				
				Map<String, Float> entityTF = FileUtil.getLastNFloatMapFromFile(entityName + "-lines-weighted.txt", 500);
				Set<Integer> ids = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream(entityName + "-relevant-post-ids.txt")); 
				Map<String, Float> unigramEntityTF = FileUtil.getLastNFloatMapFromFile(entityName + "-entityTFNormalized.txt", 0);
				
				Map<String, Integer> patterns = LongestPatternMiner.findLongPatterns(filePath, ids, unigramEntityTF, entityTF, speedUp, 0);
				FileUtil.writeMapToFile(patterns, entityName + "-long-patterns.txt", 0); //-in-all-code
				
				Set<Integer> allIds = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream("allIds.txt")); //allIds.txt
				Map<String, Integer> patternsAll = LongestPatternMiner.findLongPatterns(filePath, allIds, unigramEntityTF, entityTF, speedUp, 5);
				FileUtil.writeMapToFile(patternsAll, entityName + "-long-patterns-in-all-code.txt", 0); //
				
				
				Map<String, Float> patternsMap = FileUtil.getLastNFloatMapFromFile(entityName + "-long-patterns-in-all-code.txt",100);			
				Map<String, Float> entityPatternFreq = FileUtil.getLastNFloatMapFromFile(entityName + "-long-patterns.txt", 0);
				
				patternsMap = TFNormalizer.normalizeFloatWeightsSimple(patternsMap, entityPatternFreq, 0);
				patternsMap = TFNormalizer.normalizeWeights(patternsMap, entityPatternFreq, 0);
				Map<String, Integer> patternsInt = LongestPatternMiner.convertToInt(patternsMap);
				patternsInt = FileUtil.sortByValues(patternsInt);
				FileUtil.writeMapToFile(patternsInt, entityName + "-long-patterns-normalized.txt", 0);
			}
			long finishTime = (new Date()).getTime();
			System.out.println("Took " + (finishTime - startTime)/60000 + " minutes.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
