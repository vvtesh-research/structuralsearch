package in.ac.iiitd.pag.structuralsearch;

import java.util.ArrayList;

public class DuplicateRemoval {

	public static void main(String[] args) {
		/*String a = "a b c d e";
		String b = "g b d f";*/
		
		String a = "branch<= branch- branchrecursion branch*";
		String b = "branch== branch- branchrecursion branch*";
		
		System.out.println(score(a.split(" "), b.split(" ")));
	}
	
	/** Find the longest interleaved sequence. We are interested
	 * in finding if longer algo is contained within shorter algo.
	 * If not fully contained, how long is the longest common sequence?
	 * The longest common sequence need not be contiguous. It could be 
	 * interleaved with other statements.
	 * For instance, when "a b c d e" and "g b d f" is compared, b occurs before d
	 * and hence the result is 2 / {max length} = 2 / 5 = .4. 
	 * 
	 * @param algo1
	 * @param algo2
	 * @return
	 */
	public static float score(String[] algo1, String[] algo2) {
		float score = 0.0f;
		
		ArrayList<String>  lcs = longestCommonSubsequence(algo1, algo2);
		//System.out.println(lcs.size());
		/*String lcsSeq = "";
		for(int i=0; i<lcs.size();i++) {
			lcsSeq = lcsSeq + " " + lcs.get(i);
		}
		System.out.println(lcsSeq.trim());*/
		
		score = lcs.size() * 1.0f / (Math.max(algo1.length, algo2.length));
		
		return score;
	}
	
	/**
	 * Taken from http://java.dzone.com/articles/generic-text-comparison-tool
	 * @param text1Words
	 * @param text2Words
	 * @return
	 */
	private static ArrayList<String> longestCommonSubsequence(String[] text1Words, String[] text2Words) {
        
        int text1WordCount = text1Words.length;
        int text2WordCount = text2Words.length;
       
        int[][] solutionMatrix = new int[text1WordCount + 1][text2WordCount + 1];
       
        for (int i = text1WordCount - 1; i >= 0; i--) {
            for (int j = text2WordCount - 1; j >= 0; j--) {
                if (text1Words[i].equals(text2Words[j])) {
                    solutionMatrix[i][j] = solutionMatrix[i + 1][j + 1] + 1;
                }
                else {
                    solutionMatrix[i][j] = Math.max(solutionMatrix[i + 1][j],
                        solutionMatrix[i][j + 1]);
                }
            }
        }
       
        int i = 0, j = 0;
        ArrayList<String> lcsResultList = new ArrayList<String>();
        while (i < text1WordCount && j < text2WordCount) {
            if (text1Words[i].equals(text2Words[j])) {
                lcsResultList.add(text2Words[j]);
                i++;
                j++;
            }
            else if (solutionMatrix[i + 1][j] >= solutionMatrix[i][j + 1]) {
                i++;
            }
            else {
                j++;
            }
        }
        
        return lcsResultList;
    }
}
