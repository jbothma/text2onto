package org.ontoware.text2onto.algorithm;

import gate.persist.PersistenceException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.ontoware.text2onto.pom.POM;
import org.ontoware.text2onto.corpus.Corpus;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.linguistic.SpanishLinguisticAnalyser;
import org.ontoware.text2onto.evidence.AbstractEvidenceStore;
import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.reference.AbstractReferenceStore;
import org.ontoware.text2onto.reference.ReferenceManager;
import org.ontoware.text2onto.util.Settings;
import org.ontoware.text2onto.change.Changeable;
import org.ontoware.text2onto.change.ChangeObserver;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public abstract class AbstractAlgorithm implements ChangeObserver, Serializable { 
 
	/** probabilistic ontology model to be used */
	protected POM m_pom;

	/** Corpus from which the pom will be extracted */
	protected Corpus m_corpus;
 
	protected EvidenceManager m_evidenceManager;
	 
	protected ReferenceManager m_referenceManager;

	/** LinguisticAnalyser for tagging etc. */
	protected LinguisticAnalyser m_analyser;

	/** list of sub-algorithms to be executed */
	protected ArrayList<AbstractAlgorithm> m_algorithms;
	 
	protected HashMap<Class,AbstractEvidenceStore> m_localEvidenceStores;

	protected HashMap<Class,AbstractReferenceStore> m_localReferenceStores;

	protected HashMap<Class,AbstractEvidenceStore> m_globalEvidenceStores;

	protected HashMap<Class,AbstractReferenceStore> m_globalReferenceStores;
	
	protected AbstractNormalizer m_normalizer;
	
	protected AbstractAlgorithm m_parent = null;
	
	protected AbstractAlgorithmController m_algorithmController;
	
	protected String m_sName;
	
	
	public AbstractAlgorithm()
	{
		m_algorithms = new ArrayList<AbstractAlgorithm>(); 
		m_localEvidenceStores = new HashMap<Class,AbstractEvidenceStore>();
		m_localReferenceStores = new HashMap<Class,AbstractReferenceStore>();
		m_globalEvidenceStores = new HashMap<Class,AbstractEvidenceStore>();
		m_globalReferenceStores = new HashMap<Class,AbstractReferenceStore>(); 
	}
	
	/**
	 * sets the POM for this algorithm
	 *
	 * @param pom		POM to be used as background knowledge
	 */
	protected void setPOM( POM pom ){
		m_pom = pom;
		m_pom.addChangeObserver( this );
	}

	/**
	 * sets the Corpus for this algorithm
	 *
	 * @param corpus	corpus from which the POMObjects will be extracted
	 */
	protected void setCorpus( Corpus corpus ){
		m_corpus = corpus; 
		m_corpus.addChangeObserver( this );
	}
 
	protected void setEvidenceManager( EvidenceManager evidenceManager ){
		m_evidenceManager = evidenceManager;
	}
	 
	protected void setReferenceManager( ReferenceManager referenceManager ){
		m_referenceManager = referenceManager;
	}
	
	protected void setAlgorithmController( AbstractAlgorithmController ac ){
		m_algorithmController = ac;
	}
	
	protected void setNormalizer( AbstractNormalizer normalizer ){
		m_normalizer = normalizer;
	}

	/**
	 * sets the LinguisticAnalyser for this algorithm
	 * @param analyser	the LinguisticAnalyser to be used for obtaining linguistic information
	 */
	protected void setLinguisticAnalyser( LinguisticAnalyser analyser ){
		m_analyser = analyser;
	}

	/**
	 * adds a sub-algorithm
	 * @param algo	the results of the additional algorithm will be used by this algorithm's execute method
	 */
	protected void addAlgorithm( AbstractAlgorithm algorithm ){ 
		m_algorithms.add( algorithm );
		algorithm.setParent( this );
	}
	
	public List<AbstractAlgorithm> getAlgorithms(){
		return m_algorithms;
	}
	 
	protected void setParent( AbstractAlgorithm algorithm ){ 
		m_parent = algorithm;
	}
	
	protected AbstractAlgorithm getParent(){
		return m_parent;
	}
	
	public String toString(){
		String s = this.getClass().getSimpleName();
		if( m_algorithms.size() > 0 ){
			s += m_algorithms.toString();
		}
		return s;
	}

	protected abstract void initialize() throws Exception;
	
	protected void reset(){
		for( Changeable c: m_localEvidenceStores.values() ){
			c.resetChangesFor( this );
		}
		for( Changeable c: m_localReferenceStores.values() ){
			c.resetChangesFor( this );
		}
		for( Changeable c: m_globalEvidenceStores.values() ){
			c.resetChangesFor( this );
		}
		for( Changeable c: m_globalReferenceStores.values() ){
			c.resetChangesFor( this );
		}
		m_corpus.resetChangesFor( this );
		m_pom.resetChangesFor( this );
	}
	
	protected void addLocalEvidenceStore( AbstractEvidenceStore store ){
		m_localEvidenceStores.put(store.getClass(), store);
	}
	
	protected void addLocalReferenceStore( AbstractReferenceStore store ){
		m_localReferenceStores.put( store.getClass(), store );
	}
	
	protected void addGlobalEvidenceStore( AbstractEvidenceStore store ){
		m_globalEvidenceStores.put( store.getClass(), store );
	}
	
	protected void addGlobalReferenceStore( AbstractReferenceStore store ){
		m_globalReferenceStores.put( store.getClass(), store );
	}
	
	// just shortcuts
	
	protected final AbstractEvidenceStore getLocalEvidenceStore( Class c ){
        return m_localEvidenceStores.get(c);
	}
	
	protected final AbstractReferenceStore getLocalReferenceStore( Class c ){
		return m_localReferenceStores.get(c);
	}

	protected final AbstractEvidenceStore getGlobalEvidenceStore( Class c ){
		return m_globalEvidenceStores.get(c);
	}
	
	protected final AbstractReferenceStore getGlobalReferenceStore( Class c ){
		return m_globalReferenceStores.get(c);
	}
	
	protected String getName(){
		if( m_sName != null ){
			return m_sName;
		}
		return this.getClass().getSimpleName();
	}
}

