package org.ontoware.text2onto.algorithm;

import org.ontoware.text2onto.util.Text2OntoException;


public class ControllerException extends Text2OntoException { 

	public ControllerException( String sError ){
		super( sError );
	}   
 
	public ControllerException( Throwable e ){
		super( e ); 
	}

	public ControllerException( String sError, Throwable e ){
		super( sError, e );
	}
}

