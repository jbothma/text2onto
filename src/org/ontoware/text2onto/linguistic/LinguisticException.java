package org.ontoware.text2onto.linguistic;

import org.ontoware.text2onto.util.Text2OntoException;


public class LinguisticException extends Text2OntoException { 

	public LinguisticException( String sError ){
		super( sError );
	}   
 
	public LinguisticException( Throwable e ){
		super( e ); 
	}

	public LinguisticException( String sError, Throwable e ){
		super( sError, e );
	}
}