package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class TFCalculator {
	public static void main(String[] args) {
		try {
			Map<String, Integer> tf = construct("code-remove.txt");
			FileUtil.writeMapToFile(tf, "code-removeTF.csv", 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, Integer> construct(String inputFile) throws IOException {
		Map<String, Integer> counts = new HashMap<String,Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(inputFile), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		while ((line = reader.readLine()) != null) {
			lineCount++;
			if (lineCount % 100000 == 0) System.out.print(".");
		    line = line.replace(".", " ");
			String[] words = line.split(" ");
			int docLen = words.length;
			for(int i=0; i<docLen; i++) {
				String word = words[i].replaceAll("\\r|\\n", "").toLowerCase();
				word = word.replace(",", "");
				//word = word.replaceAll("[^a-zA-Z0-9]", "");
				if (word.trim().length() <= 1) {
					continue;
				}			
				int newCount = 0;
				if (counts.containsKey(word)) {
					newCount = counts.get(word);
				}
				newCount++;
				
				counts.put(word, newCount);
			}
		}
		reader.close();
		return counts;
		
	}
	
	public static Float precision(int decimalPlace, Float d) {

	    BigDecimal bd = new BigDecimal(Float.toString(d));
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd.floatValue();
	  }
}
