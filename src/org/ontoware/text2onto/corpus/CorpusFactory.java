package org.ontoware.text2onto.corpus;

import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
 

public class CorpusFactory { 

	public static Corpus newCorpus(){
		return new Corpus(); 
	}

	public static Corpus newCorpus( String sURI ) throws CorpusException {
		URI uri = null;
		try {
			uri =  new URI( sURI );
		} 
		catch( URISyntaxException e ){
			throw new CorpusException( "invalid URI: "+ sURI, e );
		}  
		return newCorpus( uri );
	}

	public static Corpus newCorpus( URI dirURI ) throws CorpusException {
		Corpus corpus = new Corpus();  
		for( AbstractDocument doc: getDocuments( dirURI ) ){
			corpus.addDocument( doc );
		}
		return corpus;
	}
	
	private static Set<AbstractDocument> getDocuments( URI dirURI ) throws CorpusException {
		Set<AbstractDocument> docs = new HashSet<AbstractDocument>();
		File dir = new File( dirURI );
		if( dir.exists() && dir.isDirectory() )
		{
			File files[] = dir.listFiles();
			for( int i=0; i<files.length; i++ )
			{
				if( !files[i].isDirectory() )
				{
					URI uri = files[i].toURI(); 
					AbstractDocument doc = DocumentFactory.newDocument( uri );
					if( doc != null ){ 
						docs.add( doc );
					}
				}
				else {
					docs.addAll( getDocuments( files[i].toURI() ) );
				}
			}
		}  
		return docs;
	}
}