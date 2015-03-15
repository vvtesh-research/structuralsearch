package in.ac.iiitd.pag.algorepobuilder;

import in.ac.iiitd.pag.structuralsearch.CodeSnippet;
import in.ac.iiitd.pag.structuralsearch.Search;
import in.ac.iiitd.pag.util.FileUtil;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntField;
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
		
		long startTime = (new Date()).getTime();
		
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		
		String luceneIndexFilePath = props.getProperty("ALGO_REPO_INDEX_FILE_PATH");
		String topicFilePath = props.getProperty("ALGO_NAMES_FILE_PATH");
		buildRepoFromListofTopics(topicFilePath, luceneIndexFilePath);
		/*String topic = "prime";
		List<String> topics = new ArrayList<String>();
		topics.add(topic);
		
		buildRepo(topics, luceneIndexFilePath);*/
		
		long finishTime = (new Date()).getTime();
		System.out.println("Took " + (finishTime - startTime)/60000 + " minutes.");
	}
	
	private static void buildRepoFromListofTopics(String topicFilePath, String luceneIndexFilePath) {
		List<String> topics = FileUtil.readFromFileAsList(topicFilePath);
		buildRepo(topics, luceneIndexFilePath);		
	}

	private static void buildRepo(List<String> topics, String luceneIndexFilePath) {
		try {
			List<String> output = new ArrayList<String>();
			Version v = Version.LUCENE_4_8;
			Analyzer analyzer = new WhitespaceAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(luceneIndexFilePath));
			IndexWriterConfig iwConf 
		        = new IndexWriterConfig(v,analyzer);
		    iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		    IndexWriter indexWriter
		        = new IndexWriter(fsDir,iwConf);	
		    for(int i =0; i<topics.size(); i++) {
		    	String topic = topics.get(i);
			    List<CodeSnippet> algos = Search.searchTopic(topic, 50, true);
				for(CodeSnippet algo: algos) {				
				    Document d = new Document();
					d.add(new StringField("topic", topic,
							Store.YES));
			        d.add(new TextField("algo",algo.algoString,
			                Store.YES));		       
			        d.add(new TextField("code", algo.methodDef, Store.YES));
			        d.add(new TextField("title", algo.title, Store.YES));
			        d.add(new IntField("id", Integer.parseInt(algo.id), Store.YES));
			        d.add(new StringField("methodname", algo.methodName, Store.YES));
			        d.add(new TextField("body", algo.body, Store.YES));
			        indexWriter.addDocument(d);
			        String outputTxt = MessageFormat.format("{0},{1},{2},{3}", i, topic, algo.id, algo.title);
			        output.add(outputTxt);
				}
		    }
	        indexWriter.close();
	        FileUtil.writeListToFile(output, "c:\\temp\\retrievedAlgos.txt");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
