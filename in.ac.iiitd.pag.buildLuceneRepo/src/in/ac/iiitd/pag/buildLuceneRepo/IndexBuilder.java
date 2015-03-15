package in.ac.iiitd.pag.buildLuceneRepo;

import in.ac.iiitd.pag.entity.SONavigator;
import in.ac.iiitd.pag.structureextractor.StructureExtractor;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.LanguageUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;
import in.ac.iiitd.pag.util.StructureUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

public class IndexBuilder {
	
	static Map<Integer,SONavigator> idTitle = null;
	static String luceneIndexFilePath = "";
	static IndexWriter indexWriter = null;
	static String operatorsFile = "";
	
	public static void main(String[] args) {
		long startTime = (new Date()).getTime();
		indexDoc();
		long finishTime = (new Date()).getTime();
		System.out.println("Took " + (finishTime - startTime)/60000 + " minutes.");
	}

	private static void indexDoc() {
		
		
		
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		StructureExtractor.init(props);
		String idTitlePath = props.getProperty("ID_TITLES_VOTES_JAVA_FILE_PATH");
		idTitle = SOUtil.loadIdTitleMapNoIsJava(idTitlePath);
		
		String filePath = props.getProperty("FILE_PATH");
		luceneIndexFilePath = props.getProperty("INDEX_FILE_PATH156");
		operatorsFile = props.getProperty("OPERATORS_FILE");
		
		try {
			
			Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_48);
			Directory fsDir = FSDirectory.open(new File(luceneIndexFilePath));
			IndexWriterConfig iwConf 
		        = new IndexWriterConfig(Version.LUCENE_48,analyzer);
		    iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		    indexWriter
		        = new IndexWriter(fsDir,iwConf);	        
	        
	        buildIndex(filePath, fsDir, analyzer);
	        indexWriter.close();
	        
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

		
	private static void buildIndex(String filePath, Directory fsDir,
			Analyzer an)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		
		processFile(filePath);
					
		System.out.println("Completed indexing.");		
		
		int numDocs = indexWriter.numDocs();
		System.out.println("num docs=" + numDocs);
	}
	
	private static void processFile(String filePath) {
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
			String line = null;			
			int lineCount = 0;
			System.out.println("Reading file...");
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (lineCount % 2000 == 0) System.out.print(".");
				if (lineCount % 100000 == 0) {System.out.println(lineCount);}
				try {
					if (!line.trim().startsWith("<row")) continue;
					
					XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
			        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));        
			        while(xmlEventReader.hasNext()){
			           XMLEvent xmlEvent = xmlEventReader.nextEvent();
			           if (xmlEvent.isStartElement()){
			               StartElement startElement = xmlEvent.asStartElement();
			               if(startElement.getName().getLocalPart().equalsIgnoreCase("row")){			            	   
		                	   int id = XMLUtil.getIntElement(startElement, "Id");
		                	   int parentId = XMLUtil.getIntElement(startElement, "ParentId");
		                	   String body = XMLUtil.getStringElement(startElement, "Body");
		                	   String tags = XMLUtil.getStringElement(startElement, "Tags");
		                	   String title = XMLUtil.getStringElement(startElement, "Title");
		                	   
		                	   processbody(id, parentId, body, tags, title);
			               }
			           }
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private static void processbody(int id, int parentId, String body,
			String tags, String title) throws IOException {
							
		if (parentId > 0) {
			if  (idTitle.get(parentId)!= null) {
				title = idTitle.get(parentId).title;
			}
		}
		
		if (title == null) {
			System.out.println("Title is null");
			return; 
		}
		
		if (title.trim().length() == 0) return;
		
		Set<String> codeSet = SOUtil.getCodeSet(body);
 	    String methodDef = "";
 	    int methodCount = 0;
	    for(String code: codeSet) {
		   if (LanguageUtil.getJavaMethodCount(code)==1) {
			   methodCount++;			   
			   methodDef = StringUtil.cleanCode(code);
		   }
	    }
	    //System.out.println(methodDef);
	    String methodName = StructureUtil.getMethodName(methodDef);	
	    body = Jsoup.parse(body).text();
	    body = body.toLowerCase();
	    /*List<String> algoElements = StructureUtil.getAlgo(methodDef, operatorsFile);
		List<String> flattenedAlgo = StructureUtil.flattenAlgo(algoElements);
		String algo = StringUtil.getAsCSV(flattenedAlgo);
		algo = algo.replaceAll(",", " ");*/
	    
	    String algo = StructureExtractor.extract(methodDef).toLowerCase();
		if (methodName == null) {
			return;
			//methodName = "";
			//System.out.println(id + "*" +  title + "-" + methodName + "-" + algo + ">>>" + methodDef);
			//System.exit(0);
		}
	    if (methodCount == 1) {		        
			Document d = new Document();
			d.add(new IntField("id", id, Store.YES));
	        d.add(new TextField("title", title.toLowerCase(),
	                        Store.YES));
	        d.add(new StringField("methodname", methodName,
					Store.YES));
	        d.add(new TextField("algo",algo,
	                Store.YES));
	        d.add(new StringField("code", methodDef,
                    Store.YES));
	        d.add(new TextField("body", title.toLowerCase() + " " + body, Store.YES));
	        indexWriter.addDocument(d);
	        
	    }
	}

}
