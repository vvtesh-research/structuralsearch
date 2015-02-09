package in.ac.iiitd.pag.algogrep.model;


/**
 * Abstracts the semantic properties of a code fragment.
 * 
 * @author Venkatesh
 *
 */
public class SemanticCodeModel  {

	/**All variable names, class names, comments or any such thing that could 
	* give us a clue of what's happening.
	*/
	private String[] bagOfWords = null;

	public String[] getBagOfWords() {
		return bagOfWords;
	}

	public void setBagOfWords(String[] bagOfWords) {
		this.bagOfWords = bagOfWords;
	}  
}
