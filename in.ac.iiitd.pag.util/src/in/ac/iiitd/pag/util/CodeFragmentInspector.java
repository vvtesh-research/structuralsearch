package in.ac.iiitd.pag.util;


import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.reflect.MethodUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;

/**
 * Retrieve all used variables from code snippets
 * @author venkateshv
 *
 */
public class CodeFragmentInspector {
	
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
				
		String folderPath = props.getProperty("TEMP_FOLDER_TO_WRITE");
		
		System.out.println("CodeFragmentInspector v1.0");
		System.out.println("====================================");		
		System.out.println("folderPath=" + folderPath);
		System.out.println("====================================");
		
		String codeFragment = FileUtil.readFromFile(folderPath + File.separator + "test1.java");
		
		List<String> vars1 = getStatements(codeFragment);
		for(String var: vars1) {
			System.out.println(var + "-->" + getIdentifiers(var,true));
		}
	}
	
	public static List<String> getStatements(String expression) {
		final List<String> variablesInExpression = new ArrayList<String>();
		ASTNode node = ASTUtil.getASTNode(expression);
		node.accept(new ASTVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				if (node instanceof Statement) {
					if (! (node instanceof Block)) {
						if (node instanceof IfStatement) return;
						if (node instanceof WhileStatement) return;
						if (node instanceof DoStatement) return;
						if (node instanceof ForStatement) return;
						if (node instanceof MethodInvocation) return;
						if (node instanceof MethodRef) return;
						if (node instanceof MethodDeclaration) return;
						if (containsMethodCall(node.toString())) return;
							
						variablesInExpression.add(node.toString() + node.getNodeType());
					}
				}
			}
		});
		
		return variablesInExpression;
	}
	
	
	/**
	 * Matches strings like {@code obj.myMethod(params)} and
	 * {@code if (something)} Remembers what's in front of the parentheses and
	 * what's inside.
	 * <p>
	 * {@code (?U)} lets {@code \\w} also match non-ASCII letters.
	 */
	public static final Pattern PARENTHESES_REGEX = Pattern
	        .compile("(?U)([.\\w]+)\\s*\\((.*)\\)");

	/*
	 * After these Java keywords may come an opening parenthesis.
	 */
	private static List<String> keyWordsBeforeParens = Arrays.asList("while", "for", "if",
	        "try", "catch", "switch");

	private static boolean containsMethodCall(final String s) {
	    final Matcher matcher = PARENTHESES_REGEX.matcher(s);

	    while (matcher.find()) {
	        final String beforeParens = matcher.group(1);
	        final String insideParens = matcher.group(2);
	        if (keyWordsBeforeParens.contains(beforeParens)) {
	            System.out.println("Keyword: " + beforeParens);
	            return containsMethodCall(insideParens);
	        } else {
	            System.out.println("Method name: " + beforeParens);
	            return true;
	        }
	    }
	    return false;
	}
	
	/**
	 * Pass isLine = true if you know that this expression is a single line of java code.
	 * @param expression
	 * @param isLine
	 * @return
	 */
	public static List<String> getIdentifiers(String expression, boolean isLine) {
		final List<String> variablesInExpression = new ArrayList<String>();
		ASTNode node = null;
		if (isLine) {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(expression.toCharArray());
			parser.setResolveBindings(false);
			
			parser.setKind(ASTParser.K_STATEMENTS);
			node = parser.createAST(null);
			
			if (node.toString().trim().equalsIgnoreCase("{\n}")) {
				parser = ASTParser.newParser(AST.JLS3);
				parser.setSource(expression.toCharArray());
				parser.setResolveBindings(false);
			
				parser.setKind(ASTParser.K_EXPRESSION);
				node = parser.createAST(null);
			}
			
		} else {
			node = ASTUtil.getASTNode(expression);
		}
		
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(SimpleName node) {
				
				variablesInExpression.add(node.getFullyQualifiedName());
				return super.visit(node);
			}
		});
		
		return variablesInExpression;
		
	}
	
	public static String unCamelCase(String s) {
	    return s.replaceAll("(?<=\\p{Ll})(?=\\p{Lu})|(?<=\\p{L})(?=\\p{Lu}\\p{Ll})", " ");
	}
	
	public static String getMethodName(String codeSnippet) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(codeSnippet.toCharArray());
		parser.setResolveBindings(false);
		
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		ASTNode node = parser.createAST(null);
		
		final List<String> methods = new ArrayList<String>();
		
		node.accept(new ASTVisitor() {
			public void preVisit(ASTNode node) {
				
				if (node instanceof MethodInvocation) {
					String name = ((MethodInvocation) node).getName().toString();
					methods.add(name);
				}	
			}			
			
		});
		
		return methods.get(0);
		
	}
	
	public static Set<String> tokenize(String code) throws IOException {
		Set<String> tokensFound = new HashSet<String>();
		
		StreamTokenizer st = new StreamTokenizer(new StringReader(code));
		
		st.parseNumbers();
		st.wordChars('_', '_');
		st.eolIsSignificant(true);
		st.ordinaryChars(0, ' ');
		st.slashSlashComments(true);
		st.slashStarComments(true);

		int token = st.nextToken();
		while (token != StreamTokenizer.TT_EOF) {
			String tokenFound = "";			
			switch (token) {
			case StreamTokenizer.TT_NUMBER:
				tokenFound = st.nval + "";
				break;
			case StreamTokenizer.TT_WORD:
				tokenFound = st.sval;
				break;
			case '"':
				//tokenFound = st.sval;
				break;
			case '\'':
				//tokenFound = st.sval;
				break;
			case StreamTokenizer.TT_EOL:
				break;
			case StreamTokenizer.TT_EOF:
				break;
			default:
				char character = (char) st.ttype;
				tokenFound = character + "";
				break;
			}
			if (tokenFound!= null)  {
				tokenFound = tokenFound.replaceAll(",", " ");
				tokenFound = tokenFound.replaceAll("\n", " ");		
				tokenFound = tokenFound.replaceAll("\'", " ");
				tokenFound = tokenFound.replaceAll("\"", " ");
				tokenFound = tokenFound.trim();
				if (tokenFound.trim().length() > 0) {
					tokensFound.add(tokenFound);
				}
			}
			token = st.nextToken();
		}

		
		return tokensFound;
	}
}
