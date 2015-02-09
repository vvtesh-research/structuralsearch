package in.ac.iiitd.pag.algorepobuilder;

import in.ac.iiitd.pag.structuralsearch.CodeSnippet;
import in.ac.iiitd.pag.structuralsearch.Search;
import in.ac.iiitd.pag.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Create a repository of algorithms. 
 * This allows us to search for known algorithms.
 * @author Venkatesh
 *
 */
public class AlgoRepositoryBuilder {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		String topic = "prime";
		String luceneIndexFilePath = props.getProperty("ALGO_REPO_INDEX_FILE_PATH");
					
		try {
			
			Version v = Version.LUCENE_4_8;
			Analyzer analyzer = new WhitespaceAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(luceneIndexFilePath));
			IndexWriterConfig iwConf 
		        = new IndexWriterConfig(v,analyzer);
		    iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		    IndexWriter indexWriter
		        = new IndexWriter(fsDir,iwConf);	        
		    List<CodeSnippet> algos = Search.searchTopic(topic, 500, true);
			for(CodeSnippet algo: algos) {				
			    Document d = new Document();
				d.add(new StringField("topic", topic,
						Store.YES));
		        d.add(new TextField("algo",algo.algoString,
		                Store.YES));		       
		        indexWriter.addDocument(d);	
		        System.out.println("added " + algo.algoString + " for " + topic);
			}
		    
	        indexWriter.close();
	        
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
	}
}
