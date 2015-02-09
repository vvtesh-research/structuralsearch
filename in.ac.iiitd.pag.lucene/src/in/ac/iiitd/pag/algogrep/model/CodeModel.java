package in.ac.iiitd.pag.algogrep.model;

import java.util.HashMap;

import org.apache.commons.collections.map.HashedMap;

import in.ac.iiitd.pag.algogrep.model.Result;
import in.ac.iiitd.pag.algogrep.util.StringUtil;


/**
 * A model that abstracts the class with its statistical, structural & semantic properties.
 * 
 * @author Venkatesh
 *
 */
public class CodeModel {
	private String codeFragment = "";
	private SemanticCodeModel semanticCodeModel = null;
	private StructuralCodeModel structuralCodeModel = null;
	private SyntacticCodeModel syntacticCodeModel = null;
	public SemanticCodeModel getSemanticCodeModel() {
		return semanticCodeModel;
	}
	public void setSemanticCodeModel(SemanticCodeModel semanticCodeModel) {
		this.semanticCodeModel = semanticCodeModel;
	}
	public StructuralCodeModel getStructuralCodeModel() {
		return structuralCodeModel;
	}
	public void setStructuralCodeModel(StructuralCodeModel structuralCodeModel) {
		this.structuralCodeModel = structuralCodeModel;
	}
	public SyntacticCodeModel getSyntacticCodeModel() {
		return syntacticCodeModel;
	}
	public void setSyntacticCodeModel(SyntacticCodeModel syntacticCodeModel) {
		this.syntacticCodeModel = syntacticCodeModel;
	}
	public static Result compare(CodeModel codeModel1, CodeModel codeModel2) {
		
		System.out.println("Code Model 1: \n");
		System.out.println(codeModel1.toString());
		System.out.println("Code Model 2: \n");
		System.out.println(codeModel2.toString());
		
		Result result = checkStructure(codeModel1,codeModel2);
				
		return result;
	}
	private static Result checkStructure(CodeModel codeModel1,
			CodeModel codeModel2) {
		Result result = new Result();
		HashMap<String,String> map1 = new HashMap<String,String>();
		result.setCodeFragment1(codeModel1.getCodeFragment());
		result.setCodeFragment2(codeModel2.getCodeFragment());
				
		if (codeModel1.getStructuralCodeModel().getKeyStructures().length > codeModel2.getStructuralCodeModel().getKeyStructures().length) {
			//swap them
			CodeModel temp = codeModel1;
			codeModel1 = codeModel2;
			codeModel2 = temp;
			
			result.setCodeFragment1(codeModel1.getCodeFragment());
			result.setCodeFragment2(codeModel2.getCodeFragment());
		} 		
		
		for(String structure1: codeModel1.getStructuralCodeModel().getKeyStructures()) {
			for (String structure2: codeModel2.getStructuralCodeModel().getKeyStructures()) {
				if (structure1.equalsIgnoreCase(structure2) || structure1.contains(structure2) || structure2.contains(structure1)) {
					map1.put(structure1, "found structural element");
					break;
				}
				map1.put(structure1, "not found  structural element");
			}
		}
		
		for(String word1: codeModel1.getSemanticCodeModel().getBagOfWords()) {
			for (String word2: codeModel2.getSemanticCodeModel().getBagOfWords()) {
				if (word1.equalsIgnoreCase(word2) || word1.contains(word2) || word2.contains(word1)) {
					map1.put(word1, "found variable");
					break;
				}
				map1.put(word1, "not found variable");
			}
		}
		
		for(String word1: codeModel1.getSyntacticCodeModel().getKeyExpression()) {
			for (String word2: codeModel2.getSyntacticCodeModel().getKeyExpression()) {
				if (word1.equalsIgnoreCase(word2) || word1.contains(word2) || word2.contains(word1)) {
					map1.put(word1, "found expression");
					break;
				}
				map1.put(word1, "not found expression");
			}
		}
		
		
		result.setElementsMap(map1);
		
		return result;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		//Semantic Model		
		String[] bagOfWords = semanticCodeModel.getBagOfWords();
		if (bagOfWords.length > 0) {
			sb.append("******************Semantic Elements**********************\n");
			sb.append("We found the following keywords in the program:\n");
			String words = StringUtil.covert(bagOfWords);		
			sb.append(words + ".\n");
		}
		String[] keyExpressions = syntacticCodeModel.getKeyExpression();
		if (keyExpressions.length > 0) {
			sb.append("******************Syntactic Elements**********************\n");
			sb.append("We found the following expressions in the program:\n");
			String words = StringUtil.covert(keyExpressions);		
			sb.append(words + ".\n");
		}
		String[] keyStructures = structuralCodeModel.getKeyStructures();
		if (keyStructures.length > 0) {
			sb.append("******************Structural Elements**********************\n");
			sb.append("We found the following structures in the program:\n");
			String words = StringUtil.covert(keyStructures);		
			sb.append(words + ".\n");
		}
		
		
		return sb.toString();
	}
	public String getCodeFragment() {
		return codeFragment;
	}
	public void setCodeFragment(String codeFragment) {
		this.codeFragment = codeFragment;
	}
 }
