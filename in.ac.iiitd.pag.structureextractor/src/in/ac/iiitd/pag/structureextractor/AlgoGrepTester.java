package in.ac.iiitd.pag.structureextractor;

import in.ac.iiitd.pag.util.FileUtil;

public class AlgoGrepTester {
		
	public static void main(String[] args) {		
		String method = FileUtil.readFromFile("c:\\temp\\method1.txt");
		System.out.println(StructureExtractor.extract(method));
	}
	
}
