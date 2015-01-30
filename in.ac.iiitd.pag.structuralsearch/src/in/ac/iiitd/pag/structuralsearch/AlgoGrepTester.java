package in.ac.iiitd.pag.structuralsearch;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.StructureUtil;

import java.util.List;
import java.util.Properties;

public class AlgoGrepTester {
	static String operatorsFile = "";
	
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null)
			System.out.println("ERROR: Cannot search. Cannot read properties.");
		operatorsFile = props.getProperty("OPERATORS_FILE");
		
		String methodDef = FileUtil.readFromFile("c:\\temp\\method1.txt");
		System.out.println(methodDef);
		
		List<String> algoElements = StructureUtil.getAlgo(methodDef, operatorsFile);
		List<String> flattenedAlgo = StructureUtil.flattenAlgo(algoElements);
		String algo = StringUtil.getAsCSV(flattenedAlgo);
		algo = algo.replaceAll(",", " ");
		
		System.out.println(algo);
	}
}
