package in.ac.iiitd.pag.buildLuceneRepo;

import javax.xml.stream.XMLInputFactory;

/**
 * Parse one so post and get id, parentid, title, body tokens, code snippet, tags and structure details.
 * @author Venkatesh
 *
 */
public class SOParser {

	public static SOEntity parse(String row) {
		SOEntity entity = new SOEntity();
		
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		
		return entity;
	}
}
