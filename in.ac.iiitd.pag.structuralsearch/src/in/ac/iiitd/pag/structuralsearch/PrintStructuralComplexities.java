package in.ac.iiitd.pag.structuralsearch;

import java.util.Arrays;
import java.util.List;

import in.ac.iiitd.pag.util.FileUtil;

public class PrintStructuralComplexities {
	public static void main(String[] args) {
		List<String> algos = FileUtil.readFromFileAsList("C:\\data\\svn\\iiitdsvn\\research\\algogrep\\full-paper\\12Topics.txt");
		String output = "";
		int maxBins = 35;
		int[] bins = new int[maxBins];
		
		for(String algo: algos) {
			output = output + algo + "\t";
			List<CodeSnippet> results = Search.searchTopic(algo, 500, false);
			int[] complexities = Search.getComplexities(results);
			Arrays.sort(complexities);
			for (int i = 0; i < complexities.length; i++) {
				if (complexities[i] > 330) continue;
				int bin = (complexities[i] - (complexities[i] % 10))/10;
				bins[bin] = bins[bin]+1;
				output = output + complexities[i] + " ";
			}
			output = output.substring(0, output.length()-1);
			//System.out.println(output);
			output = "";
			printIntArray(bins);
			bins = new int[maxBins];
		}
	}

	private static void printIntArray(int[] x) {
		String output = "";
		for (int i = 0; i < x.length; i++) {
			output = output + x[i] + " ";
		}
		output = output.substring(0, output.length()-1);
		System.out.println(output);
	}
}
