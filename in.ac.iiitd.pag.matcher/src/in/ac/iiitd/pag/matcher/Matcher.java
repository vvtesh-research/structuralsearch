package in.ac.iiitd.pag.matcher;

import in.ac.iiitd.pag.structureextractor.StructureExtractor;
import in.ac.iiitd.pag.util.ASTUtil;
import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.StructureUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.document.Document;

/**
 * Match methods in given code with repo of algorithms
 * @author Venkatesh
 *
 */
public class Matcher {
	
	static String operatorsFile = "";
	static String luceneIndexFilePath = "";
	static List<String> algosFromMatchInputFolder = new ArrayList<String>();
	static Map<String, String> methodSnippets = new HashMap<String,String>();
	static int count = 0;
	static String output = "";
	static String topicDistribution = "";
	static Set<String> allVocabItems = new HashSet<String>();
	static Set<String> addedMethods = new HashSet<String>();
	
	public static void main(String[] args) {
		long startTime = (new Date()).getTime();
		match();
		try {
			FileUtil.writeListToFile(allVocabItems, "c:\\temp\\matchervocab.txt");
			FileUtil.saveFile(new File("c:\\temp\\"), "matcheroutput1.txt", output, "");
			FileUtil.saveFile(new File("c:\\temp\\"), "matcheroutput2.txt", topicDistribution, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long finishTime = (new Date()).getTime();
		System.out.println("Took " + (finishTime - startTime)/60000 + " minutes.");
	}

	private static void match() {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		operatorsFile = props.getProperty("OPERATORS_FILE");
		String filePath = props.getProperty("MATCH_PARENT_FOLDER2");
		StructureExtractor.init(props);
		
		luceneIndexFilePath = props.getProperty("ALGO_REPO_INDEX_FILE_PATH");
		//LuceneUtil.printAll(luceneIndexFilePath, "algo");
		try {
			processFiles(filePath);
			//FileUtil.writeListToFile(algosFromMatchInputFolder, "c:\\temp\\algosFromEclipse.txt");
			//algosFromMatchInputFolder = FileUtil.readFromFileAsList("c:\\temp\\algosFromEclipse.txt");
			processMethods();
			//System.out.println(output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//save algos
		
	}
	
	private static void processMethods() {
		int found = 0;
		for(String row: algosFromMatchInputFolder) {
			//System.out.print(".");
			found++;
			String[] items = row.split(",");
			if (items.length < 3) continue;
			String methodName = items[1];
			String algo = items[2].toLowerCase();
			if (algo.split(" ").length < 5) continue;
			String method = methodSnippets.get(methodName + algo);
			if (!((StringUtil.countLines(method) > 9) && (StringUtil.countLines(method) < 16)))  continue;
					
			SearchResult results = SimpleLuceneSearch.search(algo, "algo", luceneIndexFilePath, methodSnippets.get(methodName + items[2]), 400);
			/*if (result.trim().length() > 0) {
				if (!result.toLowerCase().contains("c#")) {
					//System.out.println(++found + ". " + methodName + ":" + result);
				}
			}*/
			String outputStr = "";
			outputStr += "\n=============Begin============\n";
			outputStr += "Input: (" + algo + ")\n";
			outputStr += "Complexity: " + results.inputComplexity + "\n";
			outputStr += methodSnippets.get(methodName + algo) + "\n";
			
			
			String topicDistributionStr = "";
			int resultCount = 0;
			if ((results.docs != null) && (results.docs.size() > 0)) {
				outputStr += "Output:\n";
				
				HashSet<String> topicsFound = new HashSet<String>();
				topicDistributionStr = methodName + ",";
				for(int i=0; i<results.docs.size(); i++) {
					int structuralMatchCount = results.structuralElementsMatched.get(i);
					Document result = results.docs.get(i);
					String code = result.get("code");
					int complexity = ASTUtil.getStructuralComplexity(code);
					
					if (complexity > Math.max(results.inputComplexity * 3, 50)) continue;
					if (complexity > 200) continue;
					resultCount++;
					VocabularyEntity vocab = results.vocabulary.get(i);
					topicDistributionStr += result.get("topic") + ",";
					topicsFound.add(result.get("topic"));
					
					outputStr += "Topic Found: ";
					outputStr += result.get("topic") + "(" + result.get("algo") + ")"+ "\n";
					outputStr += "\tPost id" + result.get("id") + ". SO Title: (" +  result.get("title")  + ")"+ "\n"; 
					outputStr += "\t Structures Matched = " + structuralMatchCount + "\n";
					for(String item: vocab.vocabulary) {
						allVocabItems.add(item);
					}
					outputStr += "\tVocabulary Stats: " + vocab.score + " " +  StringUtil.getAsCSV(vocab.vocabulary) + "\n";
										
					outputStr += "\tComplexity: " + complexity + "\n";	
					//outputStr += code + "\n";					
				}
				outputStr += "------------------------------\n"; 
				if ((resultCount > 0)&&(topicsFound.size() < 3)) {
					output += outputStr;
					topicDistribution += resultCount + "," + topicsFound.size() + "," + topicDistributionStr + "\n";					
				}
				topicDistributionStr = "";
				topicsFound.clear();				
			}
			
		}
	}

	public static void processFiles( String path ) throws IOException {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	processFiles( f.getAbsolutePath() );                
            }
            else {
                processFile(f);                
            }
        }
    }
	public static void processFile(File f) throws IOException {
		if (!f.getName().toLowerCase().endsWith(".java")) return;
		
		String code = FileUtil.readFromFile(f.getAbsolutePath());
		Set<String> methods = grabMethods(code);
		//System.out.println( "File:" + f.getAbsoluteFile() );
		
		for(String method: methods) {	
			try {
				String methodName = StructureUtil.getMethodName(method).toLowerCase();
				String algo = StructureExtractor.extract(method).toLowerCase();
				String row = ++count + "," + methodName + "," + algo;
				algosFromMatchInputFolder.add(row);
				if (!addedMethods.contains(method)) {
					addedMethods.add(method);
					methodSnippets.put(methodName + algo, method);
				}
				/*
				System.out.print(".");
				i++;
				if (i % 100 == 0) System.out.println("");
				//System.out.println("checking " + f.getName().replace(".java", "") + "." + methodName +  "[" + algo + "] ...");
				String result = SimpleLuceneSearch.search(algo, "algo", luceneIndexFilePath, 2);
				if (result.trim().length() > 0) {
					System.out.println(methodName + "\n" + method + "\n\n");
				}*/
			}  catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private static Set<String> grabMethods(String code) {
		Set<String> methods = new HashSet<String>();
		methods.addAll(in.ac.iiitd.pag.util.ASTUtil.getMethods(code));		
		return methods;
	}
	
	
}
