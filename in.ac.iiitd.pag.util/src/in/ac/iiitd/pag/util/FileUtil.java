package in.ac.iiitd.pag.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FileUtil {

	public static Properties loadProps() {
		try {
			String computername="lenovo-THINK";
			try {
				computername =	InetAddress.getLocalHost().getHostName();
				//computername = "lenovo-THINK";
			} catch (Exception e) {
				System.out.println("Problem with inet" + e.getMessage());
			}
			System.out.println("Loading props from " + computername + ".properties");
			
			Properties mainProperties = new Properties();
			FileInputStream fis;
			fis = new FileInputStream(computername + ".properties");
			mainProperties.load(fis);
			
			return mainProperties;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		return null;

	}
	
	public static String readFromFile(String filePath) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;

			String ls = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 

		return stringBuilder.toString();
	}
	
	public static List<String> readFromFileAsList(String filePath) {
		ArrayList<String> tokens = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				line = line.replace("<pre>", "");
				line = line.replace("<p>", "");
				line = line.replace("</pre>", "");
				line = line.replace("</p>", "");
				tokens.add(line);
			}
			reader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 

		return tokens;
	}	
		
	public static List<String> urlEncode(List<String> items) {
		List<String> encoded = new ArrayList<String>();
		for(String item: items) {
			encoded.add(URLEncoder.encode(item));
		}
		return encoded;
	}
	
	public static void writeListToFile(List<String> terms, String filePath) throws IOException {
		FileWriter fw = new FileWriter(filePath);
		BufferedWriter bw = new BufferedWriter(fw);
		for(String term: terms) {	
			bw.write(term + "\n");
		}		
		bw.close();
	}

	public static void writeIntListToFile(List<Integer> terms, String filePath) throws IOException {
		FileWriter fw = new FileWriter(filePath);
		BufferedWriter bw = new BufferedWriter(fw);
		for(Integer term: terms) {	
			bw.write(term + "\n");
		}		
		bw.close();
	}

	public static void writeMapToFile(Map<String,Integer> terms, String filePath, int threshold) throws IOException {
		terms = sortByValues(terms);
		FileWriter fw = new FileWriter(filePath);
		BufferedWriter bw = new BufferedWriter(fw);
		for(String term: terms.keySet()) {	
			int val = terms.get(term);
			if (val >= threshold)
				bw.write(term.replace(",", "") + "," + terms.get(term) + "\n");
		}		
		bw.close();
	}

	public static void writeMapToFile(Map<Integer,Integer> terms, String filePath) throws IOException {
		FileWriter fw = new FileWriter(filePath);
		BufferedWriter bw = new BufferedWriter(fw);
		for(Integer term: terms.keySet()) {	
			int val = terms.get(term);
			bw.write(term + "," + val + "\n");
		}		
		bw.close();
	}
	
	public static void writeDblMapToFile(Map<String,Double> terms, String filePath, int threshold) throws IOException {
		FileWriter fw = new FileWriter(filePath);
		BufferedWriter bw = new BufferedWriter(fw);
		for(String term: terms.keySet()) {	
			double val = terms.get(term);
			if (val > threshold)
				bw.write(term.replace(",", "") + "," + terms.get(term) + "\n");
		}		
		bw.close();
	}
	
	public static void saveFile(File file, String name, String value, String namePrefix) throws IOException {
		String filePath = file.getAbsolutePath() + File.separator + namePrefix +name;
		FileWriter fw = new FileWriter(filePath);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(value);
		bw.close();
		
	}

	public static Map<String, Integer> getMapFromFile(String fileName) {
		Map<String, Integer> tokens = new HashMap<String, Integer>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split(",");
				try {
					String term = vals[0];
					int count = Integer.parseInt(vals[1]);
					tokens.put(term, count);
				} catch (Exception e) {}
			}
			reader.close();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 

		return tokens;
		
	}
	
	public static Map<Integer, Integer> getIntMapFromFile(String fileName) {
		Map<Integer, Integer> tokens = new HashMap<Integer, Integer>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split(",");
				try {
					Integer term = Integer.parseInt(vals[0]);
					int count = Integer.parseInt(vals[1]);
					tokens.put(term, count);
				} catch (Exception e) {}
			}
			reader.close();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 

		return tokens;
		
	}
	
	public static String getFormattedTokens(int id, String title, Set<String> tokens2, Map<String, Integer> fullTF) {
		String result = "";
		try {
			
			Set<String> tokens = StringUtil.getTokens(title);
			tokens.addAll(tokens2);
			Map<String,Integer> tokensWithFreq = new HashMap<String, Integer>();
			for(String token: tokens) {
				int freq = 0;
				if (fullTF.get(token) != null)
					freq = fullTF.get(token);
				tokensWithFreq.put(token,freq);
			}
			
			Map<String, Integer> sortedTokensWithFreq = sortByValues(tokensWithFreq); 
			List<String> tokensList = new ArrayList<String>();
			
			for(String token: sortedTokensWithFreq.keySet()) {
				int freq = 0;				
				tokensList.add(token+"("+sortedTokensWithFreq.get(token) + ")");
			}
			
			result = (StringUtil.getAsCSV(tokensList) + "\n");			

		} catch (Exception e) {
			e.printStackTrace();
		}		
		return result;
	}

	public static Map<String, Integer> sortByValues(
			Map<String, Integer> map) {
		List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               int v1 =(Integer) ((Map.Entry) (o1)).getValue();
	               int v2 = (Integer) ((Map.Entry) (o2)).getValue();
	               return (v1 - v2);
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	}

	public static Map<Integer, Float> sortIntMapByValues(
			Map<Integer, Float> postRankMap) {
		List list = new LinkedList(postRankMap.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               float v1 =(Float) ((Map.Entry) (o1)).getValue();
	               float v2 = (Float) ((Map.Entry) (o2)).getValue();
	               return (v1 > v2?1:-1);
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	}
	
	public static Map<Integer, Float> sortIntMapByValuesDesc(
			Map<Integer, Float> postRankMap) {
		List list = new LinkedList(postRankMap.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               float v1 =(Float) ((Map.Entry) (o1)).getValue();
	               float v2 = (Float) ((Map.Entry) (o2)).getValue();
	               return (v2 > v1?1:-1);
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	}
	
	public static Map<String, Integer> sortByValuesDesc(
			Map<String, Integer> map) {
		List list = new LinkedList(map.entrySet());
	       // Defined Custom Comparator here
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               int v1 =(Integer) ((Map.Entry) (o1)).getValue();
	               int v2 = (Integer) ((Map.Entry) (o2)).getValue();
	               return (v2 - v1);
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	}

	public static void writeMapToFileFloat(Map<String, Float> terms,
			String filePath, int threshold) throws IOException {		
		FileWriter fw = new FileWriter(filePath);
		BufferedWriter bw = new BufferedWriter(fw);
		for(String term: terms.keySet()) {	
			float val = terms.get(term);
			if (val >= threshold)
				bw.write(term.replace(",", "") + "," + terms.get(term) + "\n");
		}		
		bw.close();
		
	}

}
