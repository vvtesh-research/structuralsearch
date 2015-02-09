package in.ac.iiitd.pag.algogrep.model;

import java.util.HashMap;

/**
 * Result of clone detection attempt. Purpose is to carry information such as do we have
 * a clone, the level of confidence of such claim, etc. 
 * 
 * @author Venkatesh
 *
 */
public class Result {
	
	private boolean isClone = false;
	private int confidence = 0;
	private HashMap<String,String> elementsMap = new HashMap<String,String>(); //what elements constitute the program and which matched?
	
	public HashMap<String, String> getElementsMap() {
		return elementsMap;
	}

	public void setElementsMap(HashMap<String, String> elementsMap) {
		this.elementsMap = elementsMap;
		int found = 0;
		
		for(String key: elementsMap.keySet()) {
			System.out.println(key + " " + elementsMap.get(key));
			if (elementsMap.get(key).startsWith("found")) found++;
		}
		
		System.out.println("Out of " + elementsMap.keySet().size() + " totally " + found + " matched.");
		setConfidence(Math.round(100.0f * found/elementsMap.keySet().size()));
		
	}

	private String codeFragment1 = "";
	private String codeFragment2 = "";

	public boolean isClone() {
		return isClone;
	}

	public void setClone(boolean isClone) {
		this.isClone = isClone;
	}

	public String getCodeFragment1() {
		return codeFragment1;
	}

	public void setCodeFragment1(String codeFragment1) {
		this.codeFragment1 = codeFragment1;
	}

	public String getCodeFragment2() {
		return codeFragment2;
	}

	public void setCodeFragment2(String codeFragment2) {
		this.codeFragment2 = codeFragment2;
	}

	public int getConfidence() {
		return confidence;
	}

	public void setConfidence(int confidence) {
		this.confidence = confidence;
		if (confidence > 70) 
			setClone(true);
	}

}
