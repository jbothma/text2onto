package org.ontoware.text2onto.corpus;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;

public final class DocumentFactory {


	public static AbstractDocument newDocument( String sContent ) throws CorpusException { 
		TextDocument doc = new TextDocument();
		doc.setContent( sContent );
		return doc;
	}

	public static AbstractDocument newDocument( URI uri ) throws CorpusException {
		String sURI = uri.toString();
		if( sURI.endsWith( ".htm" ) || sURI.endsWith( ".html" ) ){
			return newDocument( HTMLDocument.class, uri );
		} 
		else if( sURI.endsWith( ".pdf" ) ){
			return newDocument( PDFDocument.class, uri );
		}
		else if( sURI.endsWith( ".txt" ) ){
			return newDocument( TextDocument.class, uri );
		}
		return newDocument( TextDocument.class, uri );
	}
 
	public static AbstractDocument newDocument( Class c, URI uri ) throws CorpusException {
		AbstractDocument doc = null;
		try { 
			doc = (AbstractDocument)c.newInstance();
		} 
		catch ( Exception e ){
			throw new CorpusException( "class not found: " + c );
		}
		doc.setURI( uri );
		return doc;
	}
 
	public static AbstractDocument newDocument( Class c, String sURI ) throws CorpusException {
		URI uri = null; 
		try {
			uri = new URI( sURI ); 
		}
		catch( URISyntaxException e ){ 
			throw new CorpusException( "invalid URI: "+ sURI, e ); 
		} 
		return newDocument( c, uri ); 
	}
}