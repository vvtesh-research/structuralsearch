package in.ac.iiitd.pag.oracle;

import in.ac.iiitd.pag.util.FileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Oracle {
	
	static Map<String,Map<Integer,Integer>> judgments = new HashMap<String,Map<Integer,Integer>>();
	
	
	public static void main(String[] args) {
		loadJudgments();
		getDistinctJudgmentCount("factorial");
		System.out.println(getTotalRelevantAnswers("factorial"));
		System.out.println(getTotalDocuments("factorial"));
		//System.out.println(getJudgment(17179735, "palindrome"));
		//System.out.println(getAllKnownPostIds().size());
	}

	public static Set<Integer> getAllKnownPostIds() {
		Set<Integer> knownIds = new HashSet<Integer>();
		Properties props = FileUtil.loadProps();
		if (props == null) return null;		
		String filePath = props.getProperty("JUDGMENTS_FILE");

		List<String> contents = FileUtil.readFromFileAsList(filePath);
		for(String content: contents) {
			try {
				String[] items = content.split(",");
				String[] postIdArr = items[1].split("-");
				int postId = Integer.parseInt(postIdArr[0]);				
				knownIds.add(postId);				
			} catch (Exception e) {
				//skip records that do not have judgments.
			}
		}
		return knownIds;
	}
	
	public static int getJudgment(int postId, String topic) {
		if (judgments.containsKey(topic)) {
			if (judgments.get(topic).containsKey(postId)) {
				return judgments.get(topic).get(postId);
			}				
		}
		return -1;
	}
	
	public static int getTotalRelevantAnswers(String topic) {
		int trAns = 0;
		if (judgments.containsKey(topic)) {
			for(int id: judgments.get(topic).keySet()) {
				int judgment = judgments.get(topic).get(id);
				if (judgment > 0) {
					trAns++;
				}
			}
		}
		return trAns;
	}

	public static int getTotalDocuments(String topic) {
		int td = 0;
		if (judgments.containsKey(topic)) {
			td = judgments.get(topic).size();
		}
		System.out.println("Total Docs: " + judgments.get(topic).keySet());
		return td;
	}

	public static void loadJudgments() {
		Properties props = FileUtil.loadProps();
		if (props == null) return;		
		String filePath = props.getProperty("JUDGMENTS_FILE");

		List<String> contents = FileUtil.readFromFileAsList(filePath);
		for(String content: contents) {
			try {
				String[] items = content.split(",");
				String[] postIdArr = items[1].split("-");
				int postId = Integer.parseInt(postIdArr[0].trim());
				int jValue = Integer.parseInt(items[2].trim());
				
				String algo = items[0];
				if (judgments.containsKey(algo)) {
					Map<Integer,Integer> postJudgmentMap = judgments.get(algo);
					postJudgmentMap.put(postId, jValue);
					judgments.put(algo, postJudgmentMap);
				} else {
					Map<Integer,Integer> postJudgmentMap = new HashMap<Integer,Integer>();
					postJudgmentMap.put(postId, jValue);
					judgments.put(algo, postJudgmentMap);
				}
			} catch (Exception e) {
				//skip records that do not have judgments.
				//System.out.println(e.getMessage());
			}
		}
	}

	public static int getDistinctJudgmentCount(String topic) {		
		Set<Integer> distinctJudgments = new HashSet<Integer>();
		if (judgments.containsKey(topic)) {
			for(int id: judgments.get(topic).keySet()) {
				int judgment = judgments.get(topic).get(id);
				if (judgment > 0) {
					distinctJudgments.add(judgment);
				}
			}
		}
		//System.out.println(distinctJudgments);
		return distinctJudgments.size();
	}
}
