package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
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
			String entityName = "remove";
			Map<String, Float> collectionTF = FileUtil.getFloatMapFromFile("allCodeTF.csv");
			Map<String,Integer> weightedCode = computeTermWeights(filePath, entityName, collectionTF);
			FileUtil.writeMapToFile(weightedCode, "code-remove-ranked.txt", 0); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Map<String, Integer> computeTermWeights(String filePath,
			String entityName, Map<String, Float> collectionTF) throws FactoryConfigurationError, IOException {
		List<String> code = LineBasedCodeExtractor.getUniqueTokensPerCodeFragment(filePath, entityName);
		FileUtil.writeListToFile(code, "temp1.txt"); 
		Map<String, Integer> tf = TFCalculator.construct("temp1.txt");
		FileUtil.writeMapToFile(tf, "temp2.txt", 0);		
		Map<String, Float> entityTF = FileUtil.getFloatMapFromFile("temp2.txt");
		Map<String, Float> weights = TFNormalizer.normalizeWeights(collectionTF, entityTF);
		FileUtil.writeMapToFileFloat(weights, "weights-" + entityName + ".csv", 0);
		Set<Integer> ids = LineRanker.extract(filePath, entityName);
		Map<String,Integer> weightedCode = LineRanker.getAllCode(ids, filePath, weights);
		weightedCode = FileUtil.sortByValues(weightedCode);		
		return weightedCode;
	}
}
