package in.ac.iiitd.pag.structureextractor;

import in.ac.iiitd.pag.util.Canonicalizer;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.StructureUtil;

import java.util.List;
import java.util.Properties;

public class StructureExtractor {
	
	static String operatorsFile = "";
	
	public static String extract(String method) {
		String structure = "";
		
		Properties props = FileUtil.loadProps();
		if (props == null)
			System.out.println("ERROR: Cannot search. Cannot read properties.");
		operatorsFile = props.getProperty("OPERATORS_FILE");
		
		Canonicalizer.init(props);
		List<String> algoElements = StructureUtil.getAlgo(method, operatorsFile);
		List<String> flattenedAlgo = StructureUtil.flattenAlgo(algoElements);
		structure = StringUtil.getAsCSV(flattenedAlgo);
		structure = structure.replaceAll(",", " ");
		structure = Canonicalizer.canonicalize(structure);
		
		return structure;
	}
}
