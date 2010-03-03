package org.ontoware.text2onto.util;

import java.io.*;

/**
 * IO utilities.
 * 
 */
public class IOUtil {

	static public void serialize( Serializable obj, File file ) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream( file ) ) );
		out.writeObject( obj );
		out.close();
	}

	static public void serialize( Serializable[] objects, File file ) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream( file ) ) );
		for( Serializable obj : objects ) {
			out.writeObject( obj );
		}
		out.close();
	}

	static public Serializable deserialize( File file ) throws IOException {
		try {
			return deserialize( new FileInputStream( file ) );
		}
		catch( IllegalArgumentException e ) {
			throw new IllegalArgumentException( "can't read serialized object from " + file + ": " + e );
		}
	}

	public static Serializable deserialize( InputStream input ) throws IOException {
		try {
			ObjectInputStream in = new ObjectInputStream( new BufferedInputStream( input ) );
			Object obj = in.readObject();
			in.close();
			return (Serializable)obj;
		}
		catch( ClassNotFoundException e ) {
			throw new IllegalArgumentException( "can't read serialized object from " + input + ": " + e );
		}
	}

}
