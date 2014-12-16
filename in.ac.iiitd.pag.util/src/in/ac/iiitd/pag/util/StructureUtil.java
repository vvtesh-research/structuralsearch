package in.ac.iiitd.pag.util;

import in.ac.iiitd.pag.util.ASTUtil;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.WhileStatement;

public class StructureUtil {
	public static List<String> getAlgo(String codeFragment, final String operatorsFile) {
		final ArrayList<String> elements = new ArrayList<String>();
		
		elements.clear();
		elements.add("<algo>");
		ASTNode node = ASTUtil.getASTNode(codeFragment);
		node.accept(new ASTVisitor() {
			boolean skip = false;
			@Override
			public void preVisit(ASTNode node) {
				if (node instanceof MethodInvocation) {
					String methodName = ((MethodInvocation) node).getName().toString();
					if (methodName.equalsIgnoreCase("println") || methodName.equalsIgnoreCase("log")) {
						skip = true;
					}
					//System.out.println("Inside " + ((MethodInvocation) node).getName() + " invoked.");
				}
				if (node instanceof IfStatement) {
					if (!skip) elements.add("<branch>");
				}
				if ((node instanceof ForStatement)||(node instanceof WhileStatement)) {
					if (!skip) elements.add("<loop>");
				}
				if (node instanceof Assignment) {
					List<String> operators = getOperator(node.toString(), operatorsFile);
					for(String op: operators) {
						if (!skip) elements.add(op); }
				}
			}			
			
			
			
			@Override
			public void endVisit(InfixExpression node) {
				List<String> operators = getOperator(node.toString(), operatorsFile);
				for(String op: operators)
					if (!skip) elements.add(op);
			}
			
			
			@Override
			public void endVisit(IfStatement node) {
				if (!skip) elements.add("</branch>");
			}
			
			@Override
			public void endVisit(ForStatement node) {
				if (!skip) elements.add("</loop>");
			}
			
			@Override
			public void endVisit(WhileStatement node) {
				if (!skip) elements.add("</loop>");
			}
			
			public void endVisit(MethodInvocation node) {
				String methodName = ((MethodInvocation) node).getName().toString();
				if (methodName.equalsIgnoreCase("println") || methodName.equalsIgnoreCase("log")) {					
					skip = false;
				}
				System.out.println(((MethodInvocation) node).getName() + " over.");
			};
			
		});	
		
		
		//flattenAlgo(elements);
	    elements.add("</algo>");
	    return elements;
	}
	
	public static List<String> getOperator(String str, String operatorsFile) {
		List<String> list = new ArrayList<String>();
		
		List<String> operators = FileUtil.readFromFileAsList(operatorsFile );
		for(int i=0; i<operators.size();i++) {
			if (str.contains(operators.get(i))) {
				list.add(operators.get(i));
				str = str.replace(operators.get(i), "");
			}
		}
		
		return list;
	}
	
	public static List<String> flattenAlgo(List<String> algo) {
		List<String> newElements = new ArrayList<String>();
		String prefix = "";
		for(int i=0; i<algo.size();i++) {
			String node = algo.get(i);
			switch (node) {
			case "<branch>":
				prefix = prefix + "branch";
				break;
			case "<loop>":
				prefix = prefix + "loop";
				break;
			case "</loop>":
				prefix = prefix.substring(0, prefix.length() - 4);
				break;
			case "</branch>":
				prefix = prefix.substring(0, prefix.length() - 6);
				break;
			case "<algo>":
				//do nothing
				break;
			case "</algo>":
				//do nothing
				break;
			default:
				//newElements.add(URLEncoder.encode(prefix + node));
				newElements.add(prefix + node);
			}
		}

		return newElements;
	}
	public static String getMethodName(String codeFragment) {
		final List<String> methodName = new ArrayList<String>();
		ASTNode node = ASTUtil.getASTNode(codeFragment);
		node.accept(new ASTVisitor() {			
			public void endVisit(MethodDeclaration node) {
				methodName.add(node.getName().getFullyQualifiedName());				
			}

		});			
	    if (methodName.size() > 0)
	    	return methodName.get(0);
	    else
	    	return null;
	}
}