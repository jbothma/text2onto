package org.ontoware.text2onto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Properties;

public class Settings {

	public final static String DEFAULT = System.getProperty( "user.dir" ) + "/text2onto.properties";

	public final static String GATE_DIR = "gate_dir";

	public final static String CREOLE_DIR = "creole_dir";

	public final static String GATE_APP = "gate_app";

	public final static String JAPE_MAIN = "jape_main";

	public final static String STOP_FILE = "stop_file";

	public final static String JWNL_PROPERTIES = "jwnl_properties";

	public final static String TEMP_CORPUS = "temp_corpus";

	public final static String LANGUAGE = "language";

	public final static String ENGLISH = "english";

	public final static String GERMAN = "german";

	public final static String SPANISH = "spanish";

	public final static String ALGORITHMS_XML = "algorithms";

	public final static String ICONS = "icons";

	public final static String SESSION_FILE = "session_file";

	public static final String DATASTORE = "datastore";
	
	public static final String TAGGER_DIR = "tagger_dir";
	
	public static final String SPANISH_WORD_NET_DIR = "spanish_wn_dir";

	

	private static Properties m_properties;
	static {
		m_properties = new Properties();
	}

	public static void load() throws IOException, FileNotFoundException {
		load( DEFAULT );
	}

	public static void load( String sFile ) throws IOException, FileNotFoundException {
		System.out.println( "loading settings from file " + sFile );
		m_properties.load( new FileInputStream( new File( sFile ) ) );
	}
	
	public static void load( Properties props ){
		m_properties = props;
	}

	public static String get( String sKey ) {
		return clean( sKey, m_properties.getProperty( sKey ) );
	}

	public static void set( String sKey, String sValue ) {
		m_properties.setProperty( sKey, sValue );
	}

	public static void save() throws FileNotFoundException {
		FileOutputStream fos = new FileOutputStream( DEFAULT );
		m_properties.save( fos, "" );
	}
	
	public static Properties getProperties(){
		return m_properties;
	}

	/*
	 * TODO: bad hack!
	 */
	private static String clean( String sKey, String sValue ){
		if( sValue == null ){
			return null;
		} 
		String sClean = sValue.toLowerCase();
		if( sClean.indexOf( ":" ) != -1 )
		{
			sClean = sClean.replaceAll( ":", ":/" );
		}
		if( sClean.indexOf( "\t" ) != -1 )
		{
			sClean = sClean.replaceAll( "\t", "/t" );
		}
		if( sClean.indexOf( "\\" ) != -1 )
		{ 
			sClean = sClean.replaceAll( "\\", "/" );
		}		
		// System.out.println( "Settings.clean( "+ sKey +" ): "+ sValue +" -> "+ sClean );
		return sClean;
	}
}