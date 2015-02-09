package in.ac.iiitd.pag.algogrep.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Algorithm {
	public List<String> variables = null;
	public Set<String> tokens = null;
	public List<String> flattenedAlgo= null;
	public int score = 0;
	public List<Integer> structure = new ArrayList<Integer>();
	public String codeSnippet;
	public int id;
	public String title;
	
		
	public void computeScore(Map<String, Integer> scores, String algoName) {
		int score = 0;
		
		String[] algoNameWords = algoName.split(" ");
		
		String[] titleWords = title.split(" ");
		
		int titleScore = 1;
		int varScore = 1;
		int falgoScore = 1;
		int tokenScore = 1;
		
		//seq matters
		titleScore = scoreTitle(algoNameWords, titleWords, 6);		
		varScore = scoreTitle(algoNameWords, variables.toArray(new String[variables.size()]), 3);
		
		for(String algoNameWord: algoNameWords) {

			for(String item: tokens) {
				if (algoNameWord.equals(item)) {					
					tokenScore = tokenScore * 2;
					break;
				}
			}
		
		}
		score = titleScore + varScore + falgoScore + tokenScore;
		this.score = score * 100 / (scores.size());
	}

	private int scoreTitle(String[] algoNameWords, String[] titleWords, int weight) {
		int score = weight;
		int jIndex = 0;
		int prevWordLoc = 0;
		int distBetweenWords = 0;
		for(String word: algoNameWords) {
			for(; jIndex<titleWords.length; jIndex++) {				
				if (word.equalsIgnoreCase(titleWords[jIndex])) {					
					if (prevWordLoc != 0) distBetweenWords = distBetweenWords + (jIndex - prevWordLoc);
					prevWordLoc = jIndex;
					score = score * weight;
					break;
				}
			}			
		}
		if (prevWordLoc != 0) score = score / prevWordLoc;
		return score;
	}

	public int getDistance(Algorithm algo) {
		int distance = 0;
		int similarity = 0;
		int total1 = 0;
		int total2 = 0;
		for(int i=0; i<structure.size();i++) {
			if ((algo.structure.get(i) == structure.get(i))&&(structure.get(i) == 1)) {
				similarity++;
			}
			if (algo.structure.get(i) == 1) {
				total1++;
			}
			if (structure.get(i) == 1) {
				total2++;
			}
		}
		int minTotal = Math.min(total1, total2);
		distance = 100 - Math.round((100.0f * similarity) / minTotal);
		return distance;
	}
}
