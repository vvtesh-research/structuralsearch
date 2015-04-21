package in.ac.iiitd.pag.janne;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.ac.iiitd.pag.util.FileUtil;

public class TFNormalizer {
	public static void main(String[] args) {
		try {
			Map<String, Float> collection = getMapFromFile("allCodeTF.csv");
			Map<String, Float> removeWord = getMapFromFile("temp2.txt");
			
			Map<String, Float> weights = normalizeWeights(collection, removeWord, 250);
			writeMap(weights, "normalizedTF.csv");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Float> normalizeWeights(Map<String, Float> collectionTerms,
			Map<String, Float> entityTerms, int collectionMinFreqCutOff) throws IOException {
		/*float maxCTFreq = 0;
		for(String word: collectionTerms.keySet()) {
			if (collectionTerms.get(word) > maxCTFreq) 
				maxCTFreq = collectionTerms.get(word);
		}
		
		float maxETFreq = 0;
		for(String word: entityTerms.keySet()) {
			if (entityTerms.get(word) > maxETFreq) 
				maxETFreq = entityTerms.get(word);
		}*/
					
		Map<String, Float> normalizedTerms = new HashMap<String, Float>();
		for(String word: entityTerms.keySet()) {
			if (collectionTerms.containsKey(word)) {				
				if (collectionTerms.get(word) < collectionMinFreqCutOff/100f) continue;
				float prob = (float) ((float) (entityTerms.get(word)) / collectionTerms.get(word));
				//System.out.println(word + "," + prob);
				//javaWords.put(word + "(" + collection.get(word) + " : " + removeWord.get(word) + ")", prob);
				normalizedTerms.put(word, prob * 100);
			} 
		}
		
		//normalizedTerms = FileUtil.sortByFloatValues(normalizedTerms);
		
		return normalizedTerms;
	}
	
	
	
	public static Map<String, Integer> normalizeWeightsSimple(Map<String, Integer> collectionTerms,
			Map<String, Float> entityTerms, int collectionMinFreqCutOff) throws IOException {
				
		float maxETFreq = 0;
		for(String word: entityTerms.keySet()) {
			if (entityTerms.get(word) > maxETFreq) 
				maxETFreq = entityTerms.get(word);
		}
		
		float maxCTFreq = 0;
		for(String word: collectionTerms.keySet()) {
			if (collectionTerms.get(word) > maxCTFreq) 
				maxCTFreq = collectionTerms.get(word);
		}
					
		Map<String, Integer> normalizedTerms = new HashMap<String, Integer>();
		for(String word: collectionTerms.keySet()) {
			if (collectionTerms.get(word) < collectionMinFreqCutOff/100f) continue;
			if (entityTerms.containsKey(word)) {				
				
				int prob = (int) (collectionTerms.get(word) * maxCTFreq / maxETFreq);
				//System.out.println(word + "," + prob);
				//javaWords.put(word + "(" + collection.get(word) + " : " + removeWord.get(word) + ")", prob);
				normalizedTerms.put(word, prob);
			} 
		}
		
		normalizedTerms = FileUtil.sortByValues(normalizedTerms);
		
		return normalizedTerms;
	}

	private static void writeMap(Map<String, Float> computerWords, String fileName)
			throws IOException {
		FileWriter fw = new FileWriter(fileName);		
		BufferedWriter bw = new BufferedWriter(fw);	
		int i=0;
		for(String key: computerWords.keySet()) {
			if (computerWords.get(key) == null) {
				System.out.println(i++ + ". " + key);
				continue;
			}
			bw.write(key + "," + computerWords.get(key) + "\n"); 
		}
		bw.close();
		fw.close();
	}
	
	public static Map<String, Float> getMapFromFile(String fileName) {
		Map<String, Float> tokens = new HashMap<String, Float>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split(",");
				try {
					String term = vals[0];
					float count = Float.parseFloat(vals[1]);
					tokens.put(term, count);
				} catch (Exception e) {}
			}
			reader.close();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 

		return tokens;
		
	}

	public static Map<String, Float> normalizeFloatWeightsSimple(
			Map<String, Float> collectionTerms, Map<String, Float> entityTerms, int collectionMinFreqCutOff) {
		float maxETFreq = 0;
		for(String word: entityTerms.keySet()) {
			if (entityTerms.get(word) > maxETFreq) 
				maxETFreq = entityTerms.get(word);
		}
		
		float maxCTFreq = 0;
		for(String word: collectionTerms.keySet()) {
			if (collectionTerms.get(word) > maxCTFreq) 
				maxCTFreq = collectionTerms.get(word);
		}
					
		Map<String, Float> normalizedTerms = new HashMap<String, Float>();
		for(String word: collectionTerms.keySet()) {
			//if (collectionTerms.get(word) < collectionMinFreqCutOff/100f) continue;
			if (entityTerms.containsKey(word)) {				
				
				float prob = (float) (collectionTerms.get(word) * maxCTFreq / maxETFreq);
				//System.out.println(word + "," + prob);
				//javaWords.put(word + "(" + collection.get(word) + " : " + removeWord.get(word) + ")", prob);
				normalizedTerms.put(word, prob);
			} 
		}
		
		normalizedTerms = FileUtil.sortByFloatValues(normalizedTerms);
		
		return normalizedTerms;
	}
}
