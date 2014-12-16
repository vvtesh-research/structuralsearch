package in.ac.iiitd.pag.matcher;

import in.ac.iiitd.pag.structuralsearch.CodeSnippet;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.LuceneUtil;

import java.io.File;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Given index path and query, return <= maxResults.
 * @author Venkatesh
 *
 */
public class SimpleLuceneSearch {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		
		String luceneIndexFilePath = props.getProperty("ALGO_REPO_INDEX_FILE_PATH");
		
		LuceneUtil.printAll(luceneIndexFilePath, "algo");
		
		String field = "algo";
		String queryString = "loop<= loop*=";
		int maxResults = 500;
		System.out.println("Searching...");
		search(queryString, field, luceneIndexFilePath, maxResults);
	}

	public static void search(String queryString, String field,
			String luceneIndexFilePath, int maxResults) {

		try {
			
			Version v = Version.LUCENE_48;
			Analyzer analyzer = new StandardAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(luceneIndexFilePath));

			IndexReader reader = IndexReader.open(fsDir);
			//System.out.println("The index " + luceneIndexFilePath + " has " + reader.maxDoc() + " documents.");
	        IndexSearcher searcher = new IndexSearcher(reader);
	        
	        QueryParser parser 
	            = new QueryParser(field,analyzer);

	        Query finalQuery = getSpanNearQuery(queryString, field);
	       	        
	        TopDocs hits = searcher.search(finalQuery,maxResults);
	        
	        ScoreDoc[] scoreDocs = hits.scoreDocs;
	        
	        int minVal = maxResults;
	        if (scoreDocs.length < maxResults) minVal = scoreDocs.length;
	        
	        for (int n = 0; n < minVal; ++n) {
	            ScoreDoc sd = scoreDocs[n];
	            float score = sd.score;
	            int docId = sd.doc;
	            Document d = searcher.doc(docId);	            
	            
	            String algo = d.get("algo");
	            String topic = d.get("topic");
	            
	            System.out.println(topic + " " + score + " " + algo);            
	        }
	        
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
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
}
