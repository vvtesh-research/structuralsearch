package in.ac.iiitd.pag.matcher;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.LuceneUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.StructureUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Match methods in given code with repo of algorithms
 * @author Venkatesh
 *
 */
public class Matcher {
	
	static String operatorsFile = "";
	static String luceneIndexFilePath = "";
	
	public static void main(String[] args) {
		match();
	}

	private static void match() {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		operatorsFile = props.getProperty("OPERATORS_FILE");
		String filePath = props.getProperty("MATCH_PARENT_FOLDER");
			
		luceneIndexFilePath = props.getProperty("ALGO_REPO_INDEX_FILE_PATH");
		LuceneUtil.printAll(luceneIndexFilePath, "algo");
		try {
			processFiles(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		if (!f.getName().endsWith(".java")) return;
		String code = FileUtil.readFromFile(f.getAbsolutePath());
		Set<String> methods = grabMethods(code);
		//System.out.println( "File:" + f.getAbsoluteFile() );
		for(String method: methods) {	
			try {
				List<String> algoElements = StructureUtil.getAlgo(method, operatorsFile);
				List<String> flattenedAlgo = StructureUtil.flattenAlgo(algoElements);
				String methodName = StructureUtil.getMethodName(method);			
				String algo = StringUtil.getAsCSV(flattenedAlgo);
				algo = algo.replaceAll(",", " ");
				System.out.println("checking " + f.getName().replace(".java", "") + "." + methodName +  "[" + algo + "] ...");
				SimpleLuceneSearch.search(algo, "algo", luceneIndexFilePath, 500);
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
