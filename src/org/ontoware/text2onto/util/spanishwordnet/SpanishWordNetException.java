package org.ontoware.text2onto.util.spanishwordnet;

import org.ontoware.text2onto.util.Text2OntoException;


public class SpanishWordNetException extends Text2OntoException 
{
	public SpanishWordNetException( String sError ) {
		super( sError );
	}

	public SpanishWordNetException( Throwable e ) {
		super( e );
	}

	public SpanishWordNetException( String sError, Throwable e ) {
		super( sError, e );
	}
}

