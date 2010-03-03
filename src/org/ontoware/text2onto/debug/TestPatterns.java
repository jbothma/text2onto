package org.ontoware.text2onto.debug;

import java.net.URI;

import org.ontoware.text2onto.pom.POM;
import org.ontoware.text2onto.pom.POMFactory;
import org.ontoware.text2onto.corpus.Corpus;
import org.ontoware.text2onto.corpus.CorpusFactory;
import org.ontoware.text2onto.corpus.DocumentFactory;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.algorithm.AlgorithmController; 


public class TestPatterns {

	public static void main( String args[] ){
		String sCorpus = null;
		String sAnnType = null;  
		if( args.length == 0 ){
			System.exit(0);
		}
		if( args.length > 0 ){
			sCorpus = args[0];
		} 
		if( args.length > 1 ){
			sAnnType = args[1];
		}
		TestPatterns test = new TestPatterns( sCorpus, sAnnType );
	}

	public TestPatterns( String sCorpusDir, String sAnnType ){
		try {
			Corpus corpus = CorpusFactory.newCorpus( sCorpusDir );
			POM pom = POMFactory.newPOM();
			 
			AlgorithmController ac = new AlgorithmController( corpus, pom ); 
			ac.addAlgorithm( new PatternAlgorithm( sAnnType ) ); 
			
			ac.execute();
 
			// POMTable pomtable1 = new POMTable( pom );
			  
			System.out.println( "\nPOM:\n"+ pom +"\n" );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}
}
