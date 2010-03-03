package org.ontoware.text2onto.util;


public class Text2OntoException extends Exception {

	private Throwable m_exception;


	public Text2OntoException( String sError ){
		super( sError );
	}   
 
	public Text2OntoException( Throwable e ){
		super( e.toString() );
		m_exception = e;
	}

	public Text2OntoException( String sError, Throwable e ){
		super( sError );
		m_exception = e;
	}
 
	public void printStackTrace(){
		super.printStackTrace();
		if( m_exception != null )
		{
			System.err.println( "Source:" );
			m_exception.printStackTrace();
		}
	}
}