package org.ontoware.text2onto.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class PDFDocument extends AbstractDocument {

	protected void setURI( URI uri ) {
		super.setURI( uri );

		Runtime r = Runtime.getRuntime();

		try {
			Process p = r.exec( "3rdparty/pdftotext.exe \""
					+ ( new File( uri ) ).toString() + "\" -" );
			BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			String input, content = "";
			while ( ( input = in.readLine() ) != null ) {
				content += input + "\n";
			}
			this.setContent( content );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

}
