package in.ac.iiitd.pag.structuralsearch;

import in.ac.iiitd.pag.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Ranker {
	public static List<CodeSnippet> applyStructureCountBoost(List<CodeSnippet> results) {
		List<CodeSnippet> rankedResults = new ArrayList<CodeSnippet>();
		Map<String, List<CodeSnippet>> map1 = new HashMap<String, List<CodeSnippet>>();
		
		Set<Integer> removals = new HashSet<Integer>();
		
		for(int i=0; i<results.size(); i++) {
			String algo1 = results.get(i).algoString;
			for (int j=i+1; j<results.size(); j++) {
				String algo2 = results.get(j).algoString;
				if (algo1.equalsIgnoreCase(algo2)) {
					removals.add(j);
				}
			}
		}
		
		//Remove duplicates;
		List<CodeSnippet> resultsTemp = new ArrayList<CodeSnippet>();
		for(int i=0; i<results.size(); i++) {
			if (!removals.contains(i)) {
				resultsTemp.add(results.get(i));
			}
		}
		results.clear();
		results.addAll(resultsTemp);
		
		for(CodeSnippet result: results) {
			String algo = result.algoString;
			String[] split = algo.split(" ");
			Set<String> splitSet = new HashSet<String>();
			for(int i = 0; i<split.length; i++) {				 
				splitSet.add(split[i]);
			}
			String hash = StringUtil.sort(splitSet.toString()).trim();
			List<CodeSnippet> codeSnippets = new ArrayList<CodeSnippet>();
			if (map1.get(hash) != null) {
				codeSnippets = map1.get(hash);
			}
			codeSnippets.add(result);
			map1.put(hash, codeSnippets);			
		}
		Map<Integer, List<CodeSnippet>> map2 = new HashMap<Integer, List<CodeSnippet>>();
		for(String hash: map1.keySet()) {
			int count = map1.get(hash).size();
			List<CodeSnippet> codeSnippets = new ArrayList<CodeSnippet>();
			if (map2.get(count) != null) {
				codeSnippets = map2.get(count);
			}
			codeSnippets.addAll(map1.get(hash));
			map2.put(count, codeSnippets);
		}
		
		Integer[] counts = new Integer[map2.keySet().size()]; 
		counts = map2.keySet().toArray(counts);
		Arrays.sort(counts);
		List<CodeSnippet> codeSnippets = new ArrayList<CodeSnippet>();
		for(int count: counts) {
			codeSnippets.addAll(map2.get(count));
		}
		return codeSnippets;
	}
	
}
