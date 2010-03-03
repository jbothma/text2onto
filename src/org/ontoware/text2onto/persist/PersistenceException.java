package org.ontoware.text2onto.persist;

import org.ontoware.text2onto.util.Text2OntoException;


public class PersistenceException extends Text2OntoException {

	public PersistenceException( String sError ){
		super( sError );
	}   
 
	public PersistenceException( Throwable e ){
		super( e ); 
	}

	public PersistenceException( String sError, Throwable e ){
		super( sError, e );
	}
}