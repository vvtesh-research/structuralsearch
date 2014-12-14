package in.ac.iiitd.pag.structuralsearch;

import in.ac.iiitd.pag.entity.SONavigator;
import in.ac.iiitd.pag.util.ASTUtil;
import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.LanguageUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.jdt.internal.core.ModelUpdater;

public class SearchUtil {
	
	static Map<Integer,SONavigator> idTitle = null;
	static String luceneIndexFilePath = "";
	static IndexWriter indexWriter = null;
	
	public static void main(String[] args) {
		String queryString = "factorial";
		int maxResults = 500;
		boolean showCodeResults = true;
		
		if (args.length >= 1) {
			if (args[0].trim().length() >=1 ) {
				queryString = args[0]; 
			}
		}
		
		if (args.length >= 2) {
			maxResults = Integer.parseInt(args[1]); 
		}
		
		if (args.length >= 3) {
			showCodeResults = Boolean.parseBoolean(args[2]);
		}
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		String luceneIndex = props.getProperty("INDEX_FILE_PATH");		
		String operatorsFile = props.getProperty("OPERATORS_FILE");
		String searchIn = "title";
		List<CodeSnippet> results = searchIndex(queryString, searchIn, 0, luceneIndex, maxResults, showCodeResults,operatorsFile);
		for(CodeSnippet result: results) {
			System.out.println(result.title);
			System.out.println(result.methodDef);
		}
		
	}

	public static List<CodeSnippet> searchIndex(String queryString, String searchIn, int outputType, String indexFilePath, int maxResults, boolean showCode, String operatorsFile) {
		
		List<CodeSnippet> results = new ArrayList<CodeSnippet>();
		
		luceneIndexFilePath = indexFilePath;
		
		
		try {
			
			Version v = Version.LUCENE_48;
			Analyzer analyzer = new StandardAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(luceneIndexFilePath));

			IndexReader reader = IndexReader.open(fsDir);
			//System.out.println("The index " + luceneIndexFilePath + " has " + reader.maxDoc() + " documents.");
	        IndexSearcher searcher = new IndexSearcher(reader);
	        
	        QueryParser parser 
	            = new QueryParser(searchIn,analyzer);

	        Query finalQuery = null;
	        
	        if (searchIn.equalsIgnoreCase("methodname")) {
	        	finalQuery = getBooleanANDQuery(queryString, searchIn);
	        } else {
	        	finalQuery = getSpanNearQuery(queryString, searchIn);
	        } 
	        
	        TopDocs hits = searcher.search(finalQuery,500);
	        //System.out.println(hits.totalHits + " documents found.");
	        ScoreDoc[] scoreDocs = hits.scoreDocs;
	        
	        int minVal = maxResults;
	        if (scoreDocs.length < maxResults) minVal = scoreDocs.length;
	        
	        for (int n = 0; n < minVal; ++n) {
	            ScoreDoc sd = scoreDocs[n];
	            float score = sd.score;
	            int docId = sd.doc;
	            Document d = searcher.doc(docId);	            
	            String code = d.get("code");
	            String algo = d.get("algo");
	            CodeSnippet cs = new CodeSnippet();	 
	            cs.id = d.get("id");
	            cs.title = d.get("title");
	            cs.methodDef = code;
	            cs.algoString = algo;
	            cs.methodName = d.get("methodname");
	            results.add(cs);	            
	        }
	        
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return results;
	}
	
	private static Query getSpanNearQuery(String queryString, String searchIn)  {
		
		String[] terms = queryString.split(" ");
		SpanQuery[] spanQueries = new SpanQuery[terms.length];
		for(int i=0; i<terms.length; i++) {
			spanQueries[i] = new SpanTermQuery(new Term(searchIn, terms[i]));
		}
		
		SpanQuery query = new SpanNearQuery(spanQueries,
				  100,
				  true);		
		
		return query;
	}
	
		
	private static Query getBooleanANDQuery(String queryString, String searchIn) {
		BooleanQuery q = new BooleanQuery();

		String[] terms = queryString.split(" ");
		
        for (String term : terms) {
        	term = term.trim();
        	if (term.length() == 0) continue;
            q.add(new TermQuery(new Term(searchIn, term)), BooleanClause.Occur.MUST);
        }
        
        return q;
	}
}
