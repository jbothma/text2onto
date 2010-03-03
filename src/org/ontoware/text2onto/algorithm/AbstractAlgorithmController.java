package org.ontoware.text2onto.algorithm;

import java.io.Serializable;
import java.util.*;

import org.ontoware.text2onto.corpus.Corpus;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.pom.POM;
import org.ontoware.text2onto.pom.POMWrapper;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.reference.ReferenceManager;
import org.ontoware.text2onto.explanation.AbstractExplanation;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public abstract class AbstractAlgorithmController implements Serializable {
 
	/** probabilistic ontology model to be used */
	protected POMWrapper m_pomWrapper;

	/** corpus from which the POM will be extracted */
	protected Corpus m_corpus;

	/** linguistic analyser for tagging etc. */
	protected LinguisticAnalyser m_analyser;

	/** list of algorithms to be executed */
	protected ArrayList<AbstractAlgorithm> m_algorithms;
	
	protected HashMap<Class, List<AbstractAlgorithm>> m_hmClass2Algorithms;

	protected EvidenceManager m_evidenceManager;
	
	protected ReferenceManager m_referenceManager;
	
	protected AbstractNormalizer m_normalizer;
	
	protected Set<ProgressListener> m_progressListeners;


	public ReferenceManager getReferenceManager(){
		return m_referenceManager;
	}
	
	public EvidenceManager getEvidenceManager(){
		return m_evidenceManager;
	}
	
	public void setNormalizer( AbstractNormalizer normalizer ){
		m_normalizer = normalizer;
	}

	public POM getPOM(){
		return m_pomWrapper.getChangeable();
	}
	
	/**
	 * adds an algorithm to this controller
	 *
	 * @param algo	an algorithm to be executed
	 */
	public void addAlgorithm( AbstractAlgorithm algorithm ) { 
		algorithm.setCorpus( m_corpus );
		algorithm.setPOM( m_pomWrapper.getChangeable() );
		algorithm.setLinguisticAnalyser( m_analyser );
		algorithm.setEvidenceManager( m_evidenceManager );
		algorithm.setReferenceManager( m_referenceManager );
		algorithm.setAlgorithmController( this );
		algorithm.setNormalizer( m_normalizer );
		m_algorithms.add( algorithm );
		updateClass2Algorithms( algorithm );
	}
	
	public void addAlgorithmTo( AbstractAlgorithm parent, AbstractAlgorithm child ){ 
		child.setCorpus( m_corpus );
		child.setPOM( m_pomWrapper.getChangeable() );
		child.setLinguisticAnalyser( m_analyser );
		child.setEvidenceManager( m_evidenceManager );
		child.setReferenceManager( m_referenceManager );
		child.setAlgorithmController( this );
		child.setNormalizer( m_normalizer );
		parent.addAlgorithm( child );
		updateClass2Algorithms( child );
	}
	
	private void updateClass2Algorithms( AbstractAlgorithm algorithm ){ 
		Class algoClass = algorithm.getClass();
		ArrayList<AbstractAlgorithm> algos = (ArrayList)m_hmClass2Algorithms.get( algoClass );
		if( algos == null ){
			algos = new ArrayList();			
			m_hmClass2Algorithms.put( algoClass, algos );
		}
		algos.add( algorithm );		
	}
	
	public List<AbstractAlgorithm> getAlgorithms(){
		List<AbstractAlgorithm> algorithms = new ArrayList<AbstractAlgorithm>();
		for( AbstractAlgorithm algorithm: m_algorithms )
		{
			if( algorithm instanceof AbstractComplexAlgorithm ){
				algorithms.addAll( algorithm.getAlgorithms() );
			}
			else {
				algorithms.add( algorithm );
			}
		}
		return algorithms;
	}
	
	public List<AbstractAlgorithm> getAlgorithms( Class algoClass ){
		return (List)m_hmClass2Algorithms.get( algoClass );		
	}
	
	public boolean containsAlgorithm( String sName ) {
		return ( getAlgorithm( sName ) != null );
	}
 
	public AbstractAlgorithm getAlgorithm( String sName ) {
		for( AbstractAlgorithm algo: m_algorithms ){
			if( sName.equals( algo.getName() ) ){
				return algo;
			}
		}
		return null; 
	}
 
	public List<AbstractAlgorithm> getComplexAlgorithms() {
		return m_algorithms;
	}
	
	public AbstractExplanation getExplanation( AbstractSimpleAlgorithm algorithm, POMChange change ) throws ControllerException {
		AbstractExplanation explanation = null;
		try {
			explanation = algorithm.getExplanation( change );
		} 
		catch( Exception e ){
			throw new ControllerException( e );
		}
		return explanation;
	} 
	
	public void requestLocalEvidenceStore( AbstractAlgorithm algo, Class c ) {
		if( algo instanceof AbstractAuxiliaryAlgorithm ){
			algo.addLocalEvidenceStore(m_evidenceManager.getLocalStoreWrapper( algo.getParent(), c ).getChangeable() );
		} else {
			algo.addLocalEvidenceStore(m_evidenceManager.getLocalStoreWrapper( algo, c ).getChangeable() );
		}
	}
	
	public void requestLocalReferenceStore( AbstractAlgorithm algo, Class c ) {
		if( algo instanceof AbstractAuxiliaryAlgorithm ){
			algo.addLocalReferenceStore( m_referenceManager.getLocalStoreWrapper( algo.getParent(), c ).getChangeable() );
		} else {
			algo.addLocalReferenceStore( m_referenceManager.getLocalStoreWrapper( algo, c).getChangeable() );
		}
	}
	
	public void requestGlobalEvidenceStore( AbstractAlgorithm algo, Class c ) {
		algo.addGlobalEvidenceStore( m_evidenceManager.getGlobalStoreWrapper( c ).getChangeable() );
	}
	
	public void requestGlobalReferenceStore( AbstractAlgorithm algo, Class c ) {
		algo.addGlobalReferenceStore( m_referenceManager.getGlobalStoreWrapper( c ).getChangeable() );
	}
	
	public void removeAlgorithm( AbstractAlgorithm algorithm ) {
		if( m_algorithms.contains( algorithm ) ){
			m_algorithms.remove( algorithm );
		}
	}

	public void resetAlgorithms() {
		m_algorithms = new ArrayList<AbstractAlgorithm>();
	}
	
	public void addProgressListener( ProgressListener listener ){
		if( m_progressListeners == null ){
			m_progressListeners = new HashSet<ProgressListener>();
		}
		m_progressListeners.add( listener );
	}
	
	protected void notifyProgressListeners( String sAlgorithm, int iAlgorithm, int iAll ){
		if( m_progressListeners == null ){
			return;
		}
		for( ProgressListener listener: m_progressListeners ){
			listener.progressChanged( sAlgorithm, iAlgorithm, iAll );
		}
	}

	/**
	 * executes all the algorithms previously added to this AlgorithmController
	 */
	public abstract void execute() throws ControllerException;
	
	public abstract List execute( AbstractAlgorithm algo ) throws ControllerException;
	
	public String toString(){
		return m_algorithms.toString();
	}
}