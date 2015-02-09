package in.ac.iiitd.pag.structureextractor;

import in.ac.iiitd.pag.util.Canonicalizer;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.StructureUtil;

import java.util.List;
import java.util.Properties;

public class StructureExtractor {
	
		
	public static String extract(String method) {
		String structure = "";
		
		
		List<String> algoElements = StructureUtil.getAlgo(method);
		List<String> flattenedAlgo = StructureUtil.flattenAlgo(algoElements);
		structure = StringUtil.getAsCSV(flattenedAlgo);
		structure = structure.replaceAll(",", " ");
		structure = Canonicalizer.canonicalize(structure);
		
		return structure;
	}

	public static void init(Properties props) {
		Canonicalizer.init(props);		
		
	}
}
