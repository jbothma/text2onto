package org.ontoware.text2onto.corpus;

import org.ontoware.text2onto.util.Text2OntoException;


public class CorpusException extends Text2OntoException {

	public CorpusException( String sError ){
		super( sError );
	}   
 
	public CorpusException( Throwable e ){
		super( e ); 
	} 

	public CorpusException( String sError, Throwable e ){
		super( sError, e );
	}
}