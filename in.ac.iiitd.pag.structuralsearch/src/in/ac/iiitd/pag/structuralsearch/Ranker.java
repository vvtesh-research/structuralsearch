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
		Canonicalizer.init();
		
		for(int i=1; i<results.size(); i++) {
			String algo1 = results.get(i).algoString;			
			results.get(i).algoString = Canonicalizer.canonicalize(algo1);
			System.out.println(results.get(i).id + " " + results.get(i).algoString);
		}
		
		//find duplicates
		for(int i=1; i<results.size(); i++) {
			String algo1 = results.get(i).algoString;
			String[] algo1arr = algo1.split(" ");
			if (algo1arr.length <= 1) continue;
			for (int j=0; j<i; j++) {				
				String algo2 = results.get(j).algoString;
				String[] algo2arr = algo2.split(" ");				
				if (algo2arr.length <=1) continue;
				
				boolean isDuplicate = false;
				float duplicacyScore = DuplicateRemoval.score(algo1arr, algo2arr);
				if (duplicacyScore >= 0.6f) {
					isDuplicate = true;
					System.out.println("Duplicate Found.");
				}
								
				if (isDuplicate) {
					removals.add(i);
					System.out.println(results.get(i).methodDef);
					System.out.println("was similar to");
					System.out.println(results.get(j).methodDef);
					break;
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
		
		System.out.println("Found " + results.size() + " after duplicate removal..");
		
		return results;
		
		/*for(CodeSnippet result: results) {
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
		
		for(int i=1; i<codeSnippets.size(); i++) {
			String algo1 = codeSnippets.get(i).algoString;						
			System.out.println(results.get(i).id + " " + algo1);
		}
		
		return codeSnippets;*/
	}
	
}
