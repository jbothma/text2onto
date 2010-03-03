package org.ontoware.text2onto.persist;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.ontoware.text2onto.util.IOUtil;
import org.ontoware.text2onto.util.Settings;

public class PersistenceManager {

	private static GateDataStore m_gateDataStore;

	/*private final static String GATE_DIR = System.getProperty( "java.io.tmpdir", "c:\\temp" ) + File.separator
			+ "GateDataStore";*/
			
	private final static String GATE_DIR = "C:\\Software\\GTTmp\\";
		
	private Session m_session;

	public PersistenceManager( Session session ) {
		m_session = session;
	}

	public static GateDataStore getGateSerialDataStore() throws PersistenceException {
		if( m_gateDataStore == null ) {
			try {
				m_gateDataStore = new GateSerialDataStore( GATE_DIR );
			}
			catch( Exception e ) {
				throw new PersistenceException( "PersistenceException::cannot open GateSerialDataStore...", e );
			}
		}
		return m_gateDataStore;
	}

	public static GateDataStore getGateDBDataStore( int iType ) throws PersistenceException {
		if( m_gateDataStore == null ) {
			try {
				m_gateDataStore = new GateDBDataStore( iType );
			}
			catch( Exception e ) {
				throw new PersistenceException( "cannot open GateDBDataStore", e );
			}
		}
		return m_gateDataStore;
	}

//	private static void initGateDataStore( int iType ) throws PersistenceException {
//		File dir = new File( GATE_DIR );
//		if( dir.exists() ) {
//			delete( dir );
//		}
//		try {
//			String sUrl = dir.toURL().toString();
//			m_gateDataStore = new GateDataStore( iType, sUrl );
//		}
//		catch( Exception e ) {
//			throw new PersistenceException( "cannot open GateDataStore", e );
//		}
//	}

	/**
	 * deletes a directory and all its subdirectories
	 */
	private static void delete( File file ) {
		if( file == null ) {
			return;
		}
		else if( file.isDirectory() ) {
			File files[] = file.listFiles();
			for( int i = 0; i < files.length; i++ ) {
				delete( files[i] );
			}
		}
		file.delete();
	}

	// TODO exceptions

	public static Serializable deserialize() {
		try {
			return IOUtil.deserialize( new File( Settings.get( Settings.SESSION_FILE ) ) );
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	public void serialize( File file ) {
		try {
			IOUtil.serialize( m_session, file );
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
	}

	public static Serializable deserialize( File file ) {
		try {
			return IOUtil.deserialize( file );
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}
}