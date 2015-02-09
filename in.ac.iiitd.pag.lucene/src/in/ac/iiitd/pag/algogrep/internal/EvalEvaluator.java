package in.ac.iiitd.pag.algogrep.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import in.ac.iiitd.pag.algogrep.util.FileUtil;

public class EvalEvaluator {
	public static String evalPath = "";
	
	public static void init() {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		
		String tempFolderPath = props.getProperty("TEMP_FOLDER_TO_WRITE");
		
		evalPath = tempFolderPath + File.separator + "venkatesh-eval";
	}
	
	public static void main(String[] args) {
		init();
		analyze();
	}
	
	public static int evaluate(List<Integer> ids, String topic) {
		int result = 0;
		Map<String, Map<Integer,Integer>> evalMaps = evalMaps();
		Map<Integer,Integer> evalMap = evalMaps.get(topic);
		for(int id: ids) {
			result = result + evalMap.get(id);
			//System.out.println(id + "=" + evalMap.get(id));
		}
		result = (result * 100)/ids.size();
		return result;
	}
	
	public static int evaluateRecall(int k, List<Integer> ids, String topic) {
		int result = 0;
		Map<String, Map<Integer,Integer>> evalMaps = evalMaps();
		Map<Integer,Integer> evalMap = evalMaps.get(topic);
		int availableGoodResults = 0;
		for(int aResult: evalMap.keySet()) {
			availableGoodResults = availableGoodResults + evalMap.get(aResult);
		}
		for(int id: ids) {
			result = result + evalMap.get(id);
			//System.out.println(id + "=" + evalMap.get(id));
		}
		result = (result * 100)/Math.min(availableGoodResults, k);
		return result;
	}
	
	/**
	 * Computes precision/recall.
	 */
	public static void analyze() {
		Map<String, Map<Integer,Integer>> evalMaps = evalMaps();
		for(String name: evalMaps.keySet()) {
			Map<Integer,Integer> evalMap = evalMaps.get(name);
			int good = 0;
			for(int id: evalMap.keySet()) {
				good = good + evalMap.get(id);
			}
			System.out.println(name + "=" + good + " out of " + evalMap.size());
		}
	}
	
	/**
	 * 
	 * @return topic, (x,y) where x,y are ints stored in evalPath. x is post id, y is 1 or 0. (good or bad).
	 */
	public static Map<String, Map<Integer,Integer>> evalMaps() {
		
		Map<String, Map<Integer,Integer>> evalMaps = new HashMap<String, Map<Integer,Integer>>();		

		File file = new File(evalPath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for(File file1: files){
				Map<Integer,Integer> evalMap = new HashMap<Integer,Integer>();
				List<String> evals = FileUtil.readFromFileAsList(file1.getAbsolutePath());
				int total = 0;
				String count = "";
				for(String eval: evals) {
					String[] evalArr = eval.split(",");
					Integer key = Integer.parseInt(evalArr[1]);
					Integer value = Integer.parseInt(evalArr[2]);
					evalMap.put(key, value);
				}
				String topic = file1.getName().substring(5, file1.getName().length()-4);				
				evalMaps.put(topic, evalMap);
			}
		}
		return evalMaps;
	}
}
