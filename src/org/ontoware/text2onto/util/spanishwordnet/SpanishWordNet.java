package org.ontoware.text2onto.util.spanishwordnet;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

import org.ontoware.text2onto.util.Settings;
import org.ontoware.text2onto.util.wordnet.WordNet;
import org.ontoware.text2onto.util.wordnet.WordNetException;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sergi
 *
 */
public class SpanishWordNet extends WordNet {

	Environment m_environment;
	EnvironmentConfig m_environmentConfig;
	DatabaseConfig m_databaseConfig;
	Database m_database;
	Database m_databaseNames;
	Database m_databaseVerbs;
	Database m_databaseAdjectives;
	Database m_databaseAdverbs;
	String[] m_arrayString;
	
	public SpanishWordNet() {
		super();
	}
	
	public static SpanishWordNet getWordNet(){
		if( m_wordnet == null ){
			m_wordnet = new SpanishWordNet();
			m_wordnet.init();
		}
		return (SpanishWordNet) m_wordnet;
	}
	
	public void init() {
		try {
			//System.out.print( "");
			super.init();
			//reader_CompletWN_to_senses();
			m_environmentConfig = new EnvironmentConfig();
			m_environmentConfig.setAllowCreate( true );
			m_environment = new Environment( new File ( Settings.get( Settings.SPANISH_WORD_NET_DIR ) ), m_environmentConfig );
			m_databaseConfig = new DatabaseConfig();
			m_databaseConfig.setAllowCreate( true );
			List lDatabaseNames = m_environment.getDatabaseNames();
			if ( lDatabaseNames.size() == 0 ) {
				m_database = m_environment.openDatabase( null,"dataBase",m_databaseConfig );
				m_databaseNames = m_environment.openDatabase( null,"dataBaseNames",m_databaseConfig );
				m_databaseVerbs = m_environment.openDatabase( null,"databaseVerbs",m_databaseConfig );
				m_databaseAdjectives = m_environment.openDatabase( null,"databaseAdjectives",m_databaseConfig );
				loadDataBases();
			}
			else {
				m_database = m_environment.openDatabase( null,"dataBase",m_databaseConfig );
				m_databaseNames = m_environment.openDatabase( null,"dataBaseNames",m_databaseConfig );
				m_databaseVerbs = m_environment.openDatabase( null,"databaseVerbs",m_databaseConfig );
				m_databaseAdjectives = m_environment.openDatabase( null,"databaseAdjectives",m_databaseConfig );
			}
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadDataBases() throws IOException, DatabaseException {
		readSpanishWordNetFile();
		readMappingVersionFile( "wn16-20.noun", m_databaseNames );
		readMappingVersionFile( "wn16-20.verb", m_databaseVerbs );
		readMappingVersionFile( "wn16-20.adj", m_databaseAdjectives );
	}
	
	public void close() throws DatabaseException {
		if ( m_database != null ) {
			m_database.close();
		}
		if ( m_databaseNames != null ) {
			m_databaseNames.close();
		}
		if ( m_databaseVerbs != null ) {
			m_databaseVerbs.close();
		}
		if ( m_databaseAdjectives != null ) {
			m_databaseAdjectives.close();
		}
		if ( m_databaseAdverbs != null ) {
			m_databaseAdverbs.close();
		}
		if ( m_environment != null ) {
			 m_environment.cleanLog();
			 m_environment.close();
		}			
	}
	
	private void addEntry( Database dbTarget, String sKey, String sData ) throws UnsupportedEncodingException, DatabaseException {
		DatabaseEntry dbentryKey = new DatabaseEntry( sKey.getBytes("UTF-8") );
		DatabaseEntry dbentryData = new DatabaseEntry( sData.getBytes("UTF-8") );
		OperationStatus osStatus = dbTarget.put( null, dbentryKey, dbentryData );
		OperationStatus osStatus2 = dbTarget.get(null, dbentryKey, dbentryData, LockMode.DEFAULT);
		
	}
	
	private String[] getEntry( Database dbTarget, String sWord ) throws DatabaseException, UnsupportedEncodingException {
		DatabaseEntry dbentryKey = new DatabaseEntry( sWord.getBytes("UTF-8") );
		DatabaseEntry dbentryData = new DatabaseEntry();
		OperationStatus oStatus = dbTarget.get( null, dbentryKey, dbentryData, LockMode.DEFAULT );
			if (dbTarget.get( null, dbentryKey, dbentryData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				byte[] byteArray = dbentryData.getData();
				String sSynsets = new String(byteArray);
				return sSynsets.split( " " );
				}
			else return null;
	}
		
	private void readSpanishWordNetFile () throws IOException, DatabaseException {
		File fileWordNetSenses = new File( Settings.get( Settings.SPANISH_WORD_NET_DIR ) + "senses.src" );
		FileReader fileReader = new FileReader( fileWordNetSenses );
		int iCharachter;
		StringBuffer sb = new StringBuffer();
		while( ( iCharachter = fileReader.read() ) != -1 ) {
			sb.append( (char)iCharachter );
		}
		fileReader.close();		
		m_arrayString = sb.toString().split( "\n" );
		for (int i = 0; i<m_arrayString.length; i++) {
			addEntry( m_database, getWordSpanishWordNetFile(i), getSynsetsSpanishWordNetFile(i) );				
		}
	}
	
	private void readMappingVersionFile( String sFile, Database dbTarget ) throws IOException, DatabaseException {
		File fileWordNetMaps = new File( Settings.get( Settings.SPANISH_WORD_NET_DIR ) + sFile );
		FileReader fileReader = new FileReader( fileWordNetMaps );
		int iCharachter;
		StringBuffer sb = new StringBuffer();
		while( ( iCharachter = fileReader.read() ) != -1 ) {
			sb.append( (char)iCharachter );
		}
		fileReader.close();		
		m_arrayString = sb.toString().split( "\n" ); //07605416
		for ( int i=0; i<m_arrayString.length; i++ ){
			addEntry( dbTarget, getSynsetVersion16(i), getSynsetVersion20(i) );		
		}
	}
	
	private String getWordSpanishWordNetFile(int iNumLine) {
		String[] arraystringLine = m_arrayString[iNumLine].split( ":" );
		return arraystringLine[0];
	}
	
	private String getSynsetsSpanishWordNetFile(int iNumLine) {
		String[] arraystringLine = m_arrayString[iNumLine].split( ":" );
		return arraystringLine[1];
	}
	
	private String getSynsetVersion16(int iNumLine) {
		String[] arraystringLine = m_arrayString[iNumLine].split( " " );
		return arraystringLine[0];		
	}
	
	private String getSynsetVersion20(int iNumLine) {
		String[] arraystringLine = m_arrayString[iNumLine].split( " " );
		String sSynsetID = null;
		float fProbability = 0;
		if ( arraystringLine.length > 3 ) {
			for (int i = 2; i < arraystringLine.length; i = i+2 ) {
				if ( fProbability < Float.parseFloat( arraystringLine[i] ) ) {
					fProbability = Float.parseFloat( arraystringLine[i] );
					sSynsetID = arraystringLine[i-1];
				}				
			}			
		}
		else {
			sSynsetID = arraystringLine[1];
		}
		return sSynsetID;		
	}
	
	public Synset[] getSynsets( String sWord ) throws JWNLException, WordNetException {
		String[] arraystringPOSandOffsets = null;
		String[] arraystring;
		long lOffset;
		Synset synset = null;
		Synset[] arraySynsets = null;
		Database dbTarget;
		try {
			arraystringPOSandOffsets = getEntry( m_database, sWord );
			if ( arraystringPOSandOffsets == null ) {
				arraySynsets = new Synset[0];
				return arraySynsets;
			}
			arraySynsets = new Synset[arraystringPOSandOffsets.length-1];
			for ( int iCounter = 1; iCounter < arraystringPOSandOffsets.length; iCounter++ ) {
				if ( arraystringPOSandOffsets[0].compareTo( "n" ) == 0 ) {
					arraystring = getEntry( m_databaseNames, arraystringPOSandOffsets[iCounter] );
					if ( arraystring == null ) {
						Synset[] arraySynsetsAux = new Synset[iCounter-1];
						for ( int i = 0; i< iCounter-1; i++ ) {
							arraySynsetsAux[i] = arraySynsets [i]; 
						}
						return arraySynsetsAux;
					}
					else {
						lOffset = Integer.parseInt( arraystring[0] );
						synset = Dictionary.getInstance().getSynsetAt( POS.NOUN, Integer.parseInt( arraystring[0] ));
					}
				}
				else if ( arraystringPOSandOffsets[0].compareTo( "v" ) == 0 ) {
					arraystring = getEntry( m_databaseVerbs, arraystringPOSandOffsets[iCounter] );
					if ( arraystring == null ) {
						Synset[] arraySynsetsAux = new Synset[iCounter-1];
						for ( int i = 0; i< iCounter-1; i++ ) {
							arraySynsetsAux[i] = arraySynsets [i]; 
						}
						return arraySynsetsAux;
					}
					else {
						lOffset = Long.parseLong( arraystring[0] );
						synset = Dictionary.getInstance().getSynsetAt( POS.VERB, lOffset );
					}
				}
				else if ( arraystringPOSandOffsets[0].compareTo( "a" ) == 0 ) {
					arraystring = getEntry( m_databaseAdjectives, arraystringPOSandOffsets[iCounter] );
					if ( arraystring == null ) {
						Synset[] arraySynsetsAux = new Synset[iCounter-1];
						for ( int i = 0; i< iCounter-1; i++ ) {
							arraySynsetsAux[i] = arraySynsets [i]; 
						}
						return arraySynsetsAux;
					}
					else {
						lOffset = Long.parseLong( arraystring[0] );
						synset = Dictionary.getInstance().getSynsetAt( POS.ADJECTIVE, lOffset );
					}
				}
				arraySynsets[iCounter-1] = synset;			
			}
		} catch ( Exception e ) {
			throw new WordNetException( e );
		}
		return arraySynsets;
	}
	
	public void reader_CompletWN_to_senses() throws Exception {
		File fileWordNetSenses = new File( Settings.get( Settings.SPANISH_WORD_NET_DIR ) + "esWN-200509-variant" );
		FileReader fileReader = new FileReader( fileWordNetSenses );
		HashMap<String,ArrayList> hmSenses = new HashMap ();
		int iCharachter;
		StringBuffer sb = new StringBuffer();
		while( ( iCharachter = fileReader.read() ) != -1 ) {
			if ( iCharachter == '|' ) {
				iCharachter = ':';
			}
			sb.append( (char)iCharachter );
		}
		fileReader.close();		
		m_arrayString = sb.toString().split( "\n" );
		for (int iIndex = 0; iIndex < m_arrayString.length; iIndex++) {
			String[] arraystringLine = m_arrayString[iIndex].split( ":");
			String sWord = arraystringLine[2];
			String sPOS = arraystringLine[0];
			String sSynsetNum = arraystringLine[1];
			if ( hmSenses.containsKey( sWord ) ){
				hmSenses.get( sWord ).add( sSynsetNum );
			}
			else {
				ArrayList alPosSynset = new ArrayList();
				alPosSynset.add( sPOS );
				alPosSynset.add( sSynsetNum );
				hmSenses.put( sWord, alPosSynset );
			}						
		}
		File fileSenses = new File ( Settings.get( Settings.SPANISH_WORD_NET_DIR ) + "senses2.src" );
		FileWriter fileWriter = new FileWriter( fileSenses );
		Iterator iteratorSenses = hmSenses.keySet().iterator();
		while ( iteratorSenses.hasNext() ) {
			String sActualKey = (String)iteratorSenses.next();
			ArrayList alPosSynsets = hmSenses.get( sActualKey );
			String sLine = sActualKey + ":" + alPosSynsets.get(0);
			for ( int iIndex = 1; iIndex < alPosSynsets.size(); iIndex++ ) {
				String synset = (String) alPosSynsets.get( iIndex );
				sLine = sLine + " " + synset;
			}
			sLine = sLine + "\n";
			fileWriter.write( sLine );
		}
		fileWriter.close();
	}
}