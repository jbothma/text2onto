package org.ontoware.text2onto.persist;

import java.io.File;
import java.util.List;

import gate.Corpus;
import gate.Factory;
import gate.DataStore;


public class GateSerialDataStore extends GateDataStore{

	protected DataStore m_dataStore;

	protected Corpus m_persistCorpus;

	protected String m_sUrl;


	public GateSerialDataStore( String sUrl ) throws Exception {
		System.out.println( "\nGateSerialDataStore: "+ sUrl );
		// clean directory
		File dir = new File( sUrl );
		if( dir.exists() ){
			delete( dir );
		}
		else {
			dir.mkdir();
		}
		String sDirUrl = dir.toURL().toString();
		// m_dataStore = Factory.openDataStore( "gate.persist.SerialDataStore", sDirUrl );
		// m_dataStore.open();
		m_dataStore = Factory.createDataStore( "gate.persist.SerialDataStore", sDirUrl ); 
	}
	
	public void setUrl( String sUrl ) {
		m_sUrl = sUrl;
	}

	/**
	 * deletes a directory and all its subdirectories
	 */
	private static void delete( File file ){
		if( file == null ){
			return;
		} 
		else if( file.isDirectory() )
		{
			File files[] = file.listFiles();
			for( int i = 0; i < files.length; i++ ){
				delete( files[i] );
			}
		}
		file.delete();
	}

	public void syncSerialDataStore( Corpus corpus ) throws Exception {
		m_persistCorpus = (Corpus)m_dataStore.adopt( corpus, null );
	}
}
