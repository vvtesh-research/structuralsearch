package in.ac.iiitd.pag.algogrep.model;

import in.ac.iiitd.pag.algogrep.util.ASTUtil;


public class CodeModeller {

	public static SemanticCodeModel getSemanticModelFromCode(String codeFragment) {
		SemanticCodeModel semanticCodeModel = new SemanticCodeModel();
		String[] names = ASTUtil.getVariableNames(codeFragment);		
		semanticCodeModel.setBagOfWords(names);
		return semanticCodeModel;
	}

	public static StructuralCodeModel getStructuralModelFromCode(
			String codeFragment) {
		StructuralCodeModel structuralCodeModel = new StructuralCodeModel();
		ASTUtil.getKeyExpressions(codeFragment);
		String[] structures = ASTUtil.getKeyStructures().toArray(new String[ASTUtil.getKeyStructures().size()]);
		
		structuralCodeModel.setKeyStructures(structures);
		return structuralCodeModel;
	}

	public static SyntacticCodeModel getSyntacticModelFromCode(
			String codeFragment) {
		SyntacticCodeModel syntacticCodeModel = new SyntacticCodeModel();
		String[] expressions = ASTUtil.getKeyExpressions(codeFragment);
		
		syntacticCodeModel.setKeyExpression(expressions);
		return syntacticCodeModel;
	}

	public static CodeModel getModelInstance(
			SemanticCodeModel semanticCodeModel,
			StructuralCodeModel structuralCodeModel,
			SyntacticCodeModel syntacticCodeModel) {
		// TODO Auto-generated method stub
		CodeModel codeModel = new CodeModel();
		codeModel.setSemanticCodeModel(semanticCodeModel);
		codeModel.setStructuralCodeModel(structuralCodeModel);
		codeModel.setSyntacticCodeModel(syntacticCodeModel);
		return codeModel;
	}

}
