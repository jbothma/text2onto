package org.ontoware.text2onto.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

public class HTMLDocument extends AbstractDocument {

	private String getText( String sPage ) {
		sPage = sPage.replaceAll("<(script|SCRIPT)[^>]*>[^(</)]+</(script|SCRIPT)>", " " );
		sPage = sPage.replaceAll("<(style|STYLE)[^>]*>[^(</)]+</(style|STYLE)>", " " );
		sPage = sPage.replaceAll( "<[^>]*>", " " );
		sPage = sPage.replaceAll( "&amp;", "and" );
		sPage = sPage.replaceAll( "&#[a-z\\d]", "" );
		sPage = sPage.replaceAll( "&[a-z]+;", " " );
		sPage = sPage.replaceAll( "\\s+", " " );
		sPage = sPage.replaceAll( "[ \t\n\f\r]{2,}", "\n" );

		return sPage;
	}

	protected void setURI( URI uri ) {
		super.setURI( uri );

		try {
			BufferedReader in = new BufferedReader( new FileReader( new File( uri ) ) );
			String input = "";
			String content = "";

			while ( ( input = in.readLine() ) != null ) {
				content += input;
			}
			
			setContent( getText( content ) );

		} catch ( IOException e ) {
			e.printStackTrace();
		}

	}

}
