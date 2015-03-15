package in.ac.iiitd.pag.algorepobuilder;

import in.ac.iiitd.pag.structuralsearch.CodeSnippet;
import in.ac.iiitd.pag.structuralsearch.Search;
import in.ac.iiitd.pag.structuralsearch.SearchUtil;
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
 * We have a list of post ids. We need to build a repo with only these ids.
 * We search the entire index by id and add items to a new repo.
 * @author Venkatesh
 *
 */
public class HeterogeneityTestDataRepoBuilder {
	public static void main(String[] args) {
		buildRepo();
	}

	private static void buildRepo() {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		
		String testIndexFilePath = props.getProperty("TEST_REPO_INDEX_FILE_PATH");
		String soIndexFilePath = props.getProperty("INDEX_FILE_PATH");
		try {
			
			Version v = Version.LUCENE_4_8;
			Analyzer analyzer = new WhitespaceAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(testIndexFilePath));
			IndexWriterConfig iwConf 
		        = new IndexWriterConfig(v,analyzer);
		    iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		    IndexWriter indexWriter
		        = new IndexWriter(fsDir,iwConf);
			
		    int i = 0;
			List<String> ids = FileUtil.readFromFileAsList("c:\\temp\\idslist.csv");
			for(String row: ids) {
				
				String[] items = row.split(",");
				String topic = items[0];
				int id = 0;
				try {
					id = Integer.parseInt(items[1]);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				String hetergeneityBucket = items[2];
				
				
				List<CodeSnippet> filteredResults = SearchUtil.searchIndex(id,
							"id", 1, soIndexFilePath, 2, false,	"");
				System.out.println("Trying " + id + ". Got " + filteredResults.size() + " results.");
				if (filteredResults.size() == 1) {	
					i++;
					CodeSnippet result = filteredResults.get(0);
				    System.out.println(i + ". "  + result.id);
				}
				    
			}		
			
				        
		   
	        indexWriter.close();
	        
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
}
