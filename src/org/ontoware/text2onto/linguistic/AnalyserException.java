package org.ontoware.text2onto.linguistic;

import org.ontoware.text2onto.util.Text2OntoException;


public class AnalyserException extends Text2OntoException { 

	public AnalyserException( String sError ){
		super( sError );
	}   
 
	public AnalyserException( Throwable e ){
		super( e ); 
	}

	public AnalyserException( String sError, Throwable e ){
		super( sError, e );
	}
}