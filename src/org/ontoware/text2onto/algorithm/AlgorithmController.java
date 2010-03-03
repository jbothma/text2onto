package org.ontoware.text2onto.algorithm;
 
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;

import org.ontoware.text2onto.corpus.Corpus;
import org.ontoware.text2onto.pom.POM;
import org.ontoware.text2onto.pom.POMFactory;
import org.ontoware.text2onto.pom.POMWrapper;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.linguistic.AnalyserException; 
import org.ontoware.text2onto.linguistic.SpanishLinguisticAnalyser;
import org.ontoware.text2onto.evidence.AbstractEvidenceStore;
import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.evidence.EvidenceWrapper;
import org.ontoware.text2onto.reference.AbstractReferenceStore;
import org.ontoware.text2onto.reference.ReferenceManager;
import org.ontoware.text2onto.reference.ReferenceWrapper;
import org.ontoware.text2onto.algorithm.normalizer.*;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.util.Settings;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class AlgorithmController extends AbstractAlgorithmController { 
 
	/**
	 * creates an instance of AlgorithmController 
	 *
	 * @param corpus	corpus for ontology extraction
	 */
	public AlgorithmController( Corpus corpus ) throws ControllerException {
		this( corpus, POMFactory.newPOM() );
	}

	public AlgorithmController( Corpus corpus, POM pom ) throws ControllerException {
		this( corpus, pom, null );
	}

	/**
	 * creates an instance of AlgorithmController
	 *
	 * @param corpus	corpus for ontology extraction
	 * @param pom		POM to be extended
	 */
	public AlgorithmController( Corpus corpus, POM pom, Properties settings ) throws ControllerException {
		try {
			if( settings != null ){
				Settings.load( settings );
			}
			else {
				Settings.load();
			}
		}
		catch( Exception e ){
			throw new ControllerException( "Unable to load settings", e );
		}
		m_algorithms = new ArrayList<AbstractAlgorithm>();
		m_hmClass2Algorithms = new HashMap<Class, List<AbstractAlgorithm>>();
		try {
			String sLanguage = Settings.get( Settings.LANGUAGE );
			if ( sLanguage.compareTo( Settings.SPANISH ) == 0 ) {
				m_analyser = new SpanishLinguisticAnalyser();
			}
			else {
				m_analyser = new LinguisticAnalyser();
			}
		} 
		catch( AnalyserException e ){
			throw new ControllerException( "Cannot initialize LinguisticAnalyser", e );
		}  
		m_corpus = corpus; 
		try {
			m_analyser.setCorpus( m_corpus );
		} 
		catch( AnalyserException e ){
			throw new ControllerException( e );
		}
		m_evidenceManager = new EvidenceManager();
		m_referenceManager = new ReferenceManager();
		m_pomWrapper = new POMWrapper( pom ); 
		m_normalizer = new DefaultNormalizer();
	}
	
	//***************************************************************************************************
		
	private List<ChangeRequest> execute( AbstractSimpleAlgorithm asa ) throws Exception { 	
		asa.initialize();
		
		List<ChangeRequest> evidenceChangeRequests = asa.getEvidenceChanges();
		for( ChangeRequest c: evidenceChangeRequests ) 
		{
			AbstractEvidenceStore target = (AbstractEvidenceStore)c.getTarget();
			EvidenceWrapper wrapper = m_evidenceManager.getWrapperByStore( target );
			if( wrapper == null ){
				throw new ControllerException( "No wrapper found for evidence store " + target );
			}
			wrapper.processChangeRequest(c);
			target.addChangeObserver(asa);
		}
		
		List<ChangeRequest> referenceChangeRequests = asa.getReferenceChanges();		
		for( ChangeRequest c: referenceChangeRequests )
		{
			AbstractReferenceStore target = (AbstractReferenceStore)c.getTarget();
			ReferenceWrapper wrapper = m_referenceManager.getWrapperByStore( target );
			if( wrapper == null )
			{
				// throw new ControllerException( "No wrapper found for reference store " + target );
				// System.out.println( "No wrapper found for reference store " + target );
				continue;
			}
			wrapper.processChangeRequest(c);
			target.addChangeObserver(asa);
		}
				
		List<ChangeRequest> pomChangeRequests = asa.getNormalizedPOMChanges();
		m_pomWrapper.getChangeable().addChangeObserver(asa);
		// ChangeObserver hier? wie reset?
			
		asa.reset();
			
		// POM changes are not applied here, but returned to the
		// calling method
			
		for( ChangeRequest c: pomChangeRequests ){
			c.setTarget( m_pomWrapper.getChangeable() );
		}
		return pomChangeRequests;
	}	
	
	private List<ChangeRequest> execute( AbstractComplexAlgorithm aca ) throws Exception { 
		aca.initialize();
			
		List<ChangeRequest> pomChangeRequests = aca.getNormalizedPOMChanges();
		for( ChangeRequest c: pomChangeRequests )
		{
			c.setTarget( m_pomWrapper.getChangeable() );
		}
		return pomChangeRequests;
	}
	
	private List<ChangeRequest> execute( AbstractAuxiliaryAlgorithm aaa ) throws Exception {	
		aaa.initialize();
		
		List<ChangeRequest> evidenceChangeRequests = aaa.getEvidenceChanges(); 
		for( ChangeRequest c: evidenceChangeRequests )
		{
			AbstractEvidenceStore target = (AbstractEvidenceStore)c.getTarget();
			EvidenceWrapper wrapper = m_evidenceManager.getWrapperByStore( target );
			if( wrapper == null ){
				throw new ControllerException( "No wrapper found for store "+ target );
			}
			wrapper.processChangeRequest(c);
			target.addChangeObserver(aaa);
		}	
		aaa.reset();
			
		return null;
	} 
	
	public List<ChangeRequest> execute( AbstractAlgorithm algorithm ) throws ControllerException {
		System.out.println( "\nALGORITHM_CONTROLLER.execute: "+ algorithm.getName() );
		try {
			if( algorithm instanceof AbstractSimpleAlgorithm ){
				return execute( (AbstractSimpleAlgorithm)algorithm );
			}
			else if( algorithm instanceof AbstractComplexAlgorithm ){
				return execute( (AbstractComplexAlgorithm)algorithm );
			}
			else if( algorithm instanceof AbstractAuxiliaryAlgorithm ){
				return execute( (AbstractAuxiliaryAlgorithm )algorithm );
			}
			else {
				throw new ControllerException( "Unknown algorithm type: "+ algorithm.getClass().getName() );
			}
		}
		catch( Exception e ){
			throw new ControllerException( e );
		}
	}
	
	/**
	 * executes all the algorithms previously added to this AlgorithmController
	 */
	public void execute() throws ControllerException {
		if( m_corpus == null ){
			throw new ControllerException( "no corpus" );
		} 
		else if( m_pomWrapper.getChangeable() == null ){
			throw new ControllerException( "no POM" ); 
		} 
		try { 
			m_analyser.doPreprocessing();
		} 
		catch( AnalyserException e ){
			throw new ControllerException( "Error while preprocessing corpus", e );
		} 	
		for( int i=0; i<m_algorithms.size(); i++ )
		{
			List<ChangeRequest> changes;
			
			AbstractAlgorithm algorithm = (AbstractAlgorithm)m_algorithms.get(i);
			
			notifyProgressListeners( algorithm.getName(), i+1, m_algorithms.size() );
			
			if( algorithm instanceof AbstractAuxiliaryAlgorithm ){
				throw new ControllerException( "Auxiliary algorithms must be sub-algorithms" );
			}
			try { 
				if( algorithm instanceof AbstractSimpleAlgorithm )
				{
					List<AbstractAlgorithm> auxAlgorithms = algorithm.getAlgorithms();
					for( AbstractAlgorithm aux: auxAlgorithms ){
						execute( aux );
					}
				}
				else if( algorithm instanceof AbstractComplexAlgorithm )
				{
					List<AbstractAlgorithm> simpleAlgorithms = algorithm.getAlgorithms();
					for( AbstractAlgorithm simple: simpleAlgorithms )
					{
						List<AbstractAlgorithm> auxAlgorithms = simple.getAlgorithms();
						for( AbstractAlgorithm aux: auxAlgorithms ){
							execute( aux );
						}
					}
				}
				changes = execute( algorithm );
			} 
			catch( Exception e ){
				throw new ControllerException( "Error while executing "+ algorithm.getName(), e );  
			}
			/* System.out.println( "\nALGORITHM_CONTROLLER: changes ( "+ algorithm.getName() +" ): " );
			for( ChangeRequest changeRequest: changes )
			{
				System.out.println( changeRequest.historyToString() );
				System.out.println( changeRequest.toString() ); 
			} */ 
			m_pomWrapper.processChangeRequests( changes );
			//m_pomWrapper.printInfo();
			algorithm.reset();
		}   
	}
	
	public void initPreprocessor() throws ControllerException, AnalyserException {
		m_analyser.initPreprocessor();
		m_analyser.setCorpus( m_corpus );
	}
}