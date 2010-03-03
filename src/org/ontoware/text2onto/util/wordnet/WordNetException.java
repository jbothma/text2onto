package org.ontoware.text2onto.util.wordnet;

import org.ontoware.text2onto.util.Text2OntoException;

public class WordNetException extends Text2OntoException
{
	public WordNetException( String sError ) {
		super( sError );
	}

	public WordNetException( Throwable e ) {
		super( e );
	}

	public WordNetException( String sError, Throwable e ) {
		super( sError, e );
	}
}
