package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;

public class EntityTagger {
	public static void main(String[] args) {
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;			
			String filePath = props.getProperty("FILE_PATH");
			String entityName = "array";
			Set<String> skipEntities = new HashSet<String>();
			skipEntities.add("arraylist");
			skipEntities.add("array list");
			Map<String, Float> collectionTF = FileUtil.getFloatMapFromFile("allCodeTF.csv");
			Map<String,Integer> weightedCode = computeTermWeights(filePath, entityName, collectionTF, skipEntities);
			FileUtil.writeMapToFile(weightedCode, "code-array-ranked.txt", 0); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Map<String, Integer> computeTermWeights(String filePath,
			String entityName, Map<String, Float> collectionTF, Set<String> skipEntities) throws FactoryConfigurationError, IOException {
		Set<Integer> ids = LineRanker.extract(filePath, entityName, skipEntities);
		List<String> code = LineBasedCodeExtractor.getAllCode(ids, filePath);
		FileUtil.writeListToFile(code, "temp1.txt"); 
		Map<String, Integer> tf = TFCalculator.construct("temp1.txt");
		FileUtil.writeMapToFile(tf, "temp2.txt", 0);		
		Map<String, Float> entityTF = FileUtil.getFloatMapFromFile("temp2.txt");
		Map<String, Float> weights = TFNormalizer.normalizeWeights(collectionTF, entityTF);
		FileUtil.writeMapToFileFloat(weights, "weights-" + entityName + ".csv", 0);		
		Map<String,Integer> weightedCode = LineRanker.getAllCode(ids, filePath, weights);
		weightedCode = FileUtil.sortByValues(weightedCode);		
		return weightedCode;
	}
}
