package in.ac.iiitd.pag.util;


import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneUtil {
	public static void checkIndex(String indexPath) {
		try {

			Version v = Version.LUCENE_48;
			Analyzer analyzer = new StandardAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(indexPath));

			IndexReader reader = IndexReader.open(fsDir);
			System.out.println("The index " + indexPath + " has "
					+ reader.maxDoc() + " documents.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static Map<String, Integer> printAll(String indexPath, String field) {
		Map<String, Integer> methodNames = new HashMap<String, Integer>();
		try {
			Version v = Version.LUCENE_48;
			Analyzer analyzer = new StandardAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(indexPath));

			IndexReader reader = IndexReader.open(fsDir);

			for (int i = 0; i < reader.maxDoc(); i++) {
				Document d = reader.document(i);
				String splitMethodName = d.get(field).toLowerCase();
				System.out.println(splitMethodName);
				/*if (splitMethodName.contains("factorial")) {
					System.out.println(splitMethodName);
				}*/
				splitMethodName = splitMethodName.replaceAll("_", " ");
				if (methodNames.containsKey(splitMethodName)) {
					methodNames.put(splitMethodName,
							methodNames.get(splitMethodName) + 1);
				} else {
					methodNames.put(splitMethodName, 1);
				}
			}

			methodNames = FileUtil.sortByValues(methodNames);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return methodNames;
	}

	public static Map<String, Integer> getAllCodeTokens(String indexPath) {
		Map<String, Integer> tokens = new HashMap<String, Integer>();
		try {
			Version v = Version.LUCENE_48;
			Analyzer analyzer = new StandardAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(indexPath));

			IndexReader reader = IndexReader.open(fsDir);

			for (int i = 0; i < reader.maxDoc(); i++) {
				Document d = reader.document(i);
				String code = d.get("code");
				if (code == null) continue;
				code = d.get("code").toLowerCase();
				Set<String> tokensFound = cleanCode(code);
				
				for(String token: tokensFound) {
					int count = 1;
					if (tokens.containsKey(token)) {
						count = tokens.get(token) + 1;
						if (count > 99999)
							count = 99999;
					}
					tokens.put(token, count);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return tokens;
	}

	private static Set<String> cleanCode(String code) throws IOException {
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
