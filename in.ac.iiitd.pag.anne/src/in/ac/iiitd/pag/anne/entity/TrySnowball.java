package in.ac.iiitd.pag.anne.entity;

import snowballstemmer.EnglishStemmer;


public class TrySnowball {
	public static void main(String[] args) {
		
		EnglishStemmer stemmer = new EnglishStemmer();
		stemmer.setCurrent("removed");
		if(stemmer.stem())
		{
		        System.out.println(stemmer.getCurrent());
		}
	}
}
