package org.ontoware.text2onto.linguistic;

import gate.AnnotationSet;
import gate.CreoleRegister;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.GateConstants;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.creole.gazetteer.DefaultGazetteer;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
 
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.corpus.Corpus;
import org.ontoware.text2onto.util.Settings;
import org.ontoware.text2onto.persist.GateDataStore;
import org.ontoware.text2onto.persist.GateSerialDataStore;
import org.ontoware.text2onto.persist.GateDBDataStore;
import org.ontoware.text2onto.change.ChangeObserver;
/*
import learning.common.corpus.AbstractDocument;
import learning.common.corpus.Corpus; 
import learning.common.util.Settings;
import learning.common.util.Parameter;
*/
/* 
import org.ontoware.text2onto.persist.PersistenceException;
import org.ontoware.text2onto.persist.PersistenceManager;
*/

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class LinguisticPreprocessor implements ChangeObserver, Serializable {

	private Corpus m_corpus;
	
	private GateCorpus m_gateCorpus;
	

	private HashMap m_hmID2Corpus;
	
	private HashMap m_hmID2GateCorpus;
  

	private ArrayList m_alTransducers;
  
	private SerialAnalyserController m_application;
 
	private boolean INIT = false;
 
	// setting parameter variables
	private String m_sEncoding = "UTF-8";
	private String m_sLanguage = Settings.get( Settings.LANGUAGE );
	private String m_sDataStore = Settings.get( Settings.DATASTORE );
	private String m_sLanguageDir; 
	
	private static GateDataStore gds;
	public final static String DS_SERIAL = "serial";
	public final static String DS_POSTGRES = "postgres";
 
 
	protected void init() throws Exception {
		// first, set the indicated language
		setLanguage( Settings.get( Settings.LANGUAGE ) );
		 
		System.out.println( "\nPreprocessor.init" );  
		m_hmID2Corpus = new HashMap();
		m_hmID2GateCorpus = new HashMap(); 

		Gate.init();
		
		CreoleRegister creoleRegister = Gate.getCreoleRegister();
		// URL creoleURL = new URL( "file:" + Settings.get( Settings.CREOLE_DIR ) );
		URL creoleURL = new URL( "file:" + Settings.get( Settings.GATE_DIR ) );		
		System.out.println( "\nLinguisticPreprocessor.init: "+ creoleURL ); 
		creoleRegister.addDirectory( creoleURL );
		creoleRegister.registerDirectories( creoleURL );
				
		creoleURL = new URL( "file:" + System.getProperty( "gate.plugins.home", null ) + "/ANNIE" );
		creoleRegister.addDirectory( creoleURL );
		creoleRegister.registerDirectories( creoleURL ); 

		creoleURL = new URL( "file:" + System.getProperty( "gate.plugins.home", null ) + "/Tools" );
		creoleRegister.addDirectory( creoleURL );
		creoleRegister.registerDirectories( creoleURL );

		creoleURL = new URL( "file:" + System.getProperty( "gate.plugins.home", null ) + "/Stemmer" );
		creoleRegister.addDirectory( creoleURL );
		creoleRegister.registerDirectories( creoleURL );
 
		initApplication(); 
		initTransducers();
	}
	
	// manage several language Gate applications
	private void setLanguage( String sLanguage ) {
		m_sLanguage = sLanguage;
		m_sLanguageDir = Settings.get( Settings.GATE_DIR ) + m_sLanguage + "/"; 
		if( m_sLanguage.equals( Settings.GERMAN ) ) {
			m_sEncoding = "ISO-8859-1";
		}
		else {
			m_sEncoding = m_sEncoding;
		}
	}
	
	public void initTransducers() throws MalformedURLException, ResourceInstantiationException {
		System.out.println( "LinguisticPreprocessor: initializing transducers..." );
		Transducer mainTransducer = new Transducer();
		mainTransducer.setGrammarURL( ( new File( m_sLanguageDir + Settings.get( Settings.JAPE_MAIN ) ) ).toURL() );
		// mainTransducer.setGrammarURL( new URL( "file:"+ Settings.get( Settings.GATE_DIR ) +"main.jape" ) ); 
		mainTransducer.setEncoding( m_sEncoding );
 		/*
 		Transducer postTransducer = new Transducer();
		postTransducer.setGrammarURL( new URL( "file:" + m_sLanguageDir + "tokeniser/postprocessGerman.jape" ) );
		postTransducer.setEncoding( m_sEncoding );
		*/
		m_alTransducers = new ArrayList();
		m_alTransducers.add( mainTransducer.init() );
	}
	
	public void initApplication() throws Exception {
		//String sAppFile = Settings.get( Settings.GATE_DIR ) + "application.gate"; 
		String sAppFile = m_sLanguageDir + Settings.get( Settings.GATE_APP );
		File appFile = new File( sAppFile );
		if( INIT || !appFile.exists() ) 
		{
			System.out.println( "LinguisticPreprocessor: creating application " + sAppFile + "..." );
			m_application = createApplication( m_sLanguage, sAppFile );
			// appFile = createApplication( m_sLanguage, sAppFile );
		}
		// System.out.println( "LinguisticPreprocessor: loading application (" + sAppFile + ")..." );
		// m_application = (SerialAnalyserController)gate.util.persistence.PersistenceManager.loadObjectFromFile( appFile ); 
	}
 
	public void addCorpus( String sID, Corpus corpus ) throws Exception {
		System.out.println( "\nLinguisticPreprocessor.addCorpus: "+ corpus );
		if( m_hmID2Corpus.get( sID ) != null )
		{
			System.out.println( "Preprocessor.addCorpus: corpus "+ sID +" already exists!" );
			return;
		}
		m_hmID2Corpus.put( sID, corpus ); 
		
		String gdstype = Settings.get( Settings.DATASTORE );
		if( gdstype.equals( DS_SERIAL ) )
		{
			// gds = PersistenceManager.getGateSerialDataStore();
			gds = new GateSerialDataStore( DS_SERIAL );
		}
		else if( gdstype.equals( DS_POSTGRES ) )
		{
			// gds = PersistenceManager.getGateDBDataStore( GateDataStore.POSTGRES );
			gds = new GateDBDataStore(2); // 2 means we want a Storage in a postgresql DB, look in the GateDBDataStore.java class
		}
		// additional database or possibly xml storage possibilities have to be programmed here
		else {
			gds = new GateSerialDataStore( DS_SERIAL );
		} 		
		// GateSerialDataStore gds =  new GateSerialDataStore( Settings.get( Settings.DATASTORE ) ); 
		GateCorpus gateCorpus = new GateCorpus( gds );
		gateCorpus.setEncoding( m_sEncoding );
		corpus.addChangeObserver( this );
 
		List documents = corpus.getDocuments();
		Iterator iter = documents.iterator();
		while( iter.hasNext() ) 
		{
			AbstractDocument doc = (AbstractDocument)iter.next();
			gateCorpus.addDocument( doc );
		}
		m_hmID2GateCorpus.put( sID, gateCorpus );
	} 
	
	public void setCorpus( String sID ) throws Exception { 
		m_corpus = (Corpus)m_hmID2Corpus.get( sID );
		m_gateCorpus = (GateCorpus)m_hmID2GateCorpus.get( sID );
	}
	
	public void setCorpus( Corpus corpus ) throws Exception {
		addCorpus( "default", corpus );
		setCorpus( "default" );
	}
	
	protected void doPreprocessing() throws Exception {
		if( m_corpus.hasChangesFor( this ) ) {
			System.out.println( "\nLinguisticPreprocessor: preprocessing corpus..." );
			m_gateCorpus.apply( m_application, m_alTransducers, m_corpus.getChangesFor( this ) );
		}
		else {
			System.out.println( "LinguisticPreprocessor: corpus not changed" );
		}
		m_gateCorpus.save();
		System.out.println( "\nPreprocessor: "+ m_gateCorpus );
		m_corpus.resetChangesFor( this );
	}
  
  	protected AnnotationSet getAnnotations( AbstractDocument doc ) {
		return m_gateCorpus.getAnnotations( doc );
	}

	protected AnnotationSet getAnnotations( AbstractDocument doc, Long lStart, Long lEnd ) {
		return m_gateCorpus.getAnnotations( doc, lStart, lEnd );
	}

	protected String getDocumentContent( Long lStart, Long lEnd, AbstractDocument doc ) throws InvalidOffsetException {
		return m_gateCorpus.getDocumentContent( lStart, lEnd, doc );
	}

	private File createApplication( String sAppFile ) throws Exception { 
		SerialAnalyserController controller = null;
		controller = (SerialAnalyserController)Factory.createResource( "gate.creole.SerialAnalyserController", 
			Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_" + Gate.genSym() );
		for( int i = 0; i < ANNIEConstants.PR_NAMES.length; i++ )
		{
			FeatureMap params = Factory.newFeatureMap();
			System.out.println( ANNIEConstants.PR_NAMES[i] );
			ProcessingResource pr = (ProcessingResource)Factory.createResource( ANNIEConstants.PR_NAMES[i], params );
			controller.add( pr );
		}
		System.out.println( "gate.creole.morph.Morph" );
		ProcessingResource pr = (ProcessingResource)Factory.createResource( "gate.creole.morph.Morph", Factory.newFeatureMap() );
		controller.add( pr );

		// System.out.println( "stemmer.SnowballStemmer" );
		// ProcessingResource pr = (ProcessingResource) Factory.createResource(
		// "stemmer.SnowballStemmer", Factory.newFeatureMap() );
		// controller.add( pr );

		System.out.println( "org.ontoware.text2onto.linguistic.StopwordDetection" );
		FeatureMap params = Factory.newFeatureMap();
		params.put( "stopwordsURL", new URL( "file:" + Settings.get( Settings.GATE_DIR ) + "stopwords.txt" ) );
		params.put( "testUpperCase", new Boolean( true ) );
		pr = (ProcessingResource)Factory.createResource( "org.ontoware.text2onto.linguistic.StopwordDetection", params );
		controller.add( pr );
		 
		File file = new File( sAppFile );
		gate.util.persistence.PersistenceManager.saveObjectToFile( controller, file );
		Factory.deleteResource( controller );
		controller = null;
		return file;
	}
 
	private SerialAnalyserController createApplication( String sLanguage, String sAppFile ) throws Exception {
		SerialAnalyserController controller = null;
		controller = (SerialAnalyserController)Factory.createResource( "gate.creole.SerialAnalyserController", 
			Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_" + Gate.genSym() );
		if( sLanguage.equals( Settings.ENGLISH ) ) 
		{
			for( int i = 0; i < ANNIEConstants.PR_NAMES.length; i++ ) {
				FeatureMap params = Factory.newFeatureMap();
				System.out.println( ANNIEConstants.PR_NAMES[i] );
				ProcessingResource pr = (ProcessingResource)Factory.createResource( ANNIEConstants.PR_NAMES[i], params );
				controller.add( pr );
			}
			System.out.println( "gate.creole.morph.Morph" );
			ProcessingResource pr = (ProcessingResource)Factory.createResource( "gate.creole.morph.Morph", Factory.newFeatureMap() );
			controller.add( pr );

			// System.out.println( "stemmer.SnowballStemmer" );
			// ProcessingResource pr = (ProcessingResource) Factory.createResource(
			// "stemmer.SnowballStemmer", Factory.newFeatureMap() );
			// controller.add( pr );

			System.out.println( "org.ontoware.text2onto.linguistic.StopwordDetection" );
			FeatureMap params = Factory.newFeatureMap();
			params.put( "stopwordsURL", new URL( "file:" + m_sLanguageDir + Settings.get( Settings.STOP_FILE ) ) );
			params.put( "testUpperCase", new Boolean( true ) );
			pr = (ProcessingResource)Factory.createResource( "org.ontoware.text2onto.linguistic.StopwordDetection", params );
			controller.add( pr );
		}
		else if( sLanguage.equals( Settings.GERMAN ) ) 
		{
			for( int i = 0; i < ANNIEConstants.PR_NAMES.length; i++ ) {
				FeatureMap params = Factory.newFeatureMap();
				System.out.println( ANNIEConstants.PR_NAMES[i] );
				ProcessingResource pr = (ProcessingResource)Factory.createResource( ANNIEConstants.PR_NAMES[i], params );
				if( i == 1 ) {
					params.put( "tokeniserRulesURL", new URL( "file:" + m_sLanguageDir + "tokeniser/GermanTokeniser.rules" ) );
					pr = (ProcessingResource)Factory.createResource( ANNIEConstants.PR_NAMES[i], params );
					( (DefaultTokeniser)pr ).setEncoding( m_sEncoding );
				}
				if( i == 2 ) {
					String sPlugins = System.getProperty( "gate.plugins.home", "/c:/HIWI/GATE/plugins" );
					params.put( "listsURL", new URL( "file:" + sPlugins + "/german/resources/gazetteer/lists.def" ) );
					pr = (ProcessingResource)Factory.createResource( ANNIEConstants.PR_NAMES[i], params );
					( (DefaultGazetteer)pr ).setEncoding( m_sEncoding );
				}
				controller.add( pr );
			}
			System.out.println( "gate.creole.POSTagger" );
			FeatureMap params = Factory.newFeatureMap();

			params.put( "lexiconURL", new URL( "file:" + m_sLanguageDir + "tagger/german.lexicon" ) );
			params.put( "rulesURL", new URL( "file:" + m_sLanguageDir + "tagger/german.full.rules" ) );
			ProcessingResource pr = (ProcessingResource)Factory.createResource( "gate.creole.POSTagger", params );
			( (gate.creole.POSTagger)pr ).setEncoding( m_sEncoding );
			controller.add( pr );

			System.out.println( "stemmer.SnowballStemmer" );
			params = Factory.newFeatureMap();
			params.put( "language", "german" );
			pr = (ProcessingResource)Factory.createResource( "stemmer.SnowballStemmer", params );
			controller.add( pr );

			System.out.println( "org.ontoware.text2onto.linguistic.StopwordDetection" );
			params = Factory.newFeatureMap();
			params.put( "stopwordsURL", new URL( "file:" + m_sLanguageDir + Settings.get( Settings.STOP_FILE ) ) );
			params.put( "testUpperCase", new Boolean( true ) );
			pr = (ProcessingResource)Factory.createResource( "org.ontoware.text2onto.linguistic.StopwordDetection", params );
			controller.add( pr );
		}
		else if( sLanguage.equals( Settings.SPANISH ) ) 
		{
			System.out.println("starting application.gate creation for spanish...");
			for( int i = 0; i < ANNIEConstants.PR_NAMES.length; i++ ) {
				FeatureMap params = Factory.newFeatureMap();
				System.out.println( ANNIEConstants.PR_NAMES[i] );
				ProcessingResource pr = (ProcessingResource)Factory.createResource( ANNIEConstants.PR_NAMES[i], params );
				controller.add( pr );
			}
			System.out.println( "org.ontoware.text2onto.linguistic.TreeTagger" );
			FeatureMap params = Factory.newFeatureMap();
			String sPlugins = System.getProperty( "gate.plugins.home", "/c:/HIWI/GATE/plugins" );
			params.put( "taggerScript",  Settings.get( Settings.TAGGER_DIR ) + "tag-spanish.bat" );
			ProcessingResource prTagger = (ProcessingResource)Factory.createResource( "org.ontoware.text2onto.linguistic.TreeTagger", params );
			controller.add( prTagger );

			System.out.println( "org.ontoware.text2onto.linguistic.Lemmatizer" );
			params = Factory.newFeatureMap();
			params.put( "lemmatizerScript", Settings.get( Settings.TAGGER_DIR ) + "tag-spanish.bat" );
			ProcessingResource prLemmatizer = (ProcessingResource)Factory.createResource( "org.ontoware.text2onto.linguistic.Lemmatizer", params );
			controller.add( prLemmatizer );
			
			System.out.println( "org.ontoware.text2onto.linguistic.StopwordDetection" );
			params = Factory.newFeatureMap();
			params.put( "stopwordsURL", new URL( "file:" + m_sLanguageDir + Settings.get( Settings.STOP_FILE ) ) );
			params.put( "testUpperCase", new Boolean( true ) );
			ProcessingResource prStopwords = (ProcessingResource)Factory.createResource( "org.ontoware.text2onto.linguistic.StopwordDetection", params );
			controller.add( prStopwords );			
		}
		System.out.println( "LinguisticPreprocessor.createApplication: done" );
		return controller;
		
		// File file = new File( sAppFile );
		// gate.util.persistence.PersistenceManager.saveObjectToFile( controller, file );
		// Factory.deleteResource( controller );
		// controller = null;
		// return file;
	}
	
	public String toString() {
		return "Preprocessor: [ " + m_gateCorpus + "]";
	}	
}
