package in.ac.iiitd.pag.structuralsearch;

import in.ac.iiitd.pag.util.FileUtil;

import java.util.List;
import java.util.Properties;

public class Canonicalizer {

	static List<String> operators = null;
		
	public static void main(String[] args) {
		String element = "loop*=";
		System.out.println(canonicalize(element));
	}
	
	public static void init() {
		Properties props = FileUtil.loadProps();
		if (props == null)
			System.out.println("ERROR: Cannot search. Cannot read properties.");
		String operatorsFile = props.getProperty("CANONICALIZED_OPERATORS_FILE"); 
		operators = FileUtil.readFromFileAsList(operatorsFile);
	}
	
	public static String canonicalize(String algo) {
		String returnVal = "";
		String[] elements = algo.split(" ");
		for(String element: elements) {			
			returnVal = returnVal + " " + modifyElement(element);
		}
		return returnVal.trim();
	}

	private static String modifyElement(String element) {
		
		String modifiedElement = element;
		for(String operator: operators) {
			if (element.contains(operator)) {
				modifiedElement = element.replace(operator, operator.replace("=", ""));				
			}
		}
		return modifiedElement;
	}
}
