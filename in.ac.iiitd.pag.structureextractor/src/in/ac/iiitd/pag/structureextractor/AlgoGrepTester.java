package in.ac.iiitd.pag.structureextractor;

import java.util.List;
import java.util.Properties;

import in.ac.iiitd.pag.util.FileUtil;

/**
 * Extract structure from source code.
 * @author Venkatesh
 *
 */
public class AlgoGrepTester {
		
	public static void main(String[] args) {		
		String method = FileUtil.readFromFile("c:\\temp\\method1.txt");
		System.out.println(method);
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		String operatorsFile = props.getProperty("CANONICALIZED_OPERATORS_FILE"); 
		List<String> operators = FileUtil.readFromFileAsList(operatorsFile);
		System.out.println(StructureExtractor.extract(method, operators));
	}
	
}
