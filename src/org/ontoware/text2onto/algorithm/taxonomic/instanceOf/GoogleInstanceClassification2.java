package org.ontoware.text2onto.algorithm.taxonomic.instanceOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.pom.POMAbstractRelation;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMInstance;
import org.ontoware.text2onto.pom.POMInstanceOfRelation;
import org.ontoware.text2onto.util.ProbabilityComparator;
import org.ontoware.text2onto.util.Settings;
import org.ontoware.text2onto.util.google.GoogleWrapper;
import org.ontoware.text2onto.linguistic.Lemmatizer;

public class GoogleInstanceClassification2 extends AbstractSimpleAlgorithm implements AbstractInstanceClassification {

    private Lemmatizer m_lemmatizer;
    
	private final int m_iMaxInstances = 100;
	
	private GoogleWrapper m_GoogleWrapper;
	
	private long m_sSumResults;
	
	private HashMap<String,Integer> m_hmConcept2Results;
	
	protected void initialize() {
		System.out.print( "*\n" ); 
		m_GoogleWrapper = new GoogleWrapper();
		System.out.print( "*\n" ); 
		m_lemmatizer = new Lemmatizer();
		m_GoogleWrapper.init();
		System.out.print( "*\n" ); 
	}
	
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		return new ArrayList<ChangeRequest>();
	}

	protected AbstractExplanation getExplanation(POMChange change) throws Exception {
		return null;
	}

	protected List<ChangeRequest> getReferenceChanges() throws Exception {
		return new ArrayList<ChangeRequest>();
	}
	
	protected List<ChangeRequest> getPOMChanges() throws Exception {
		List<ChangeRequest> pomChangeRequests= new ArrayList<ChangeRequest>();
		List<Object> objects = m_pom.getChangedObjectsFor( this );
		ArrayList<POMInstance> changedInstances = new ArrayList<POMInstance>(); 
		ArrayList<POMConcept> changedConcepts = new ArrayList<POMConcept>(); 
		for( Object object: objects )
		{
			if( object instanceof POMInstance ) {
				changedInstances.add( (POMInstance)object );
			}
			else if ( object instanceof POMConcept ) {
				changedConcepts.add( (POMConcept)object );
			}
		}	
		System.out.println( "GoogleInstanceClassification.getPOMChanges: "+ changedInstances ); 
		int iInstances = 0;
		Collections.sort( changedInstances, new ProbabilityComparator() );
		for( POMInstance instance: changedInstances ) {   
			iInstances++;
			ArrayList<Integer> types = new ArrayList<Integer>();
			
			types.add( Change.Type.ADD );
			types.add( Change.Type.REMOVE ); 
			Change change = m_pom.getLastChangeFor( this, instance, types );
			if (change == null)
				continue;
			int iChange = change.getType();
			List<POMConcept> lPOMConcepts = m_pom.getObjects( POMConcept.class );
			m_hmConcept2Results = new HashMap();
			System.out.print( "*\n" ); 
			Collections.sort( lPOMConcepts, new ProbabilityComparator() );
			int iConcepts = 0;
			HashMap hmConcept2Occurrences = getHMResults( instance.getLabel() );
			for ( POMConcept concept: lPOMConcepts ) {
				if ( hmConcept2Occurrences.containsKey( concept.getLabel() ) ) {
					m_hmConcept2Results.put(  concept.getLabel(), (Integer) hmConcept2Occurrences.get( concept.getLabel() ) );
				}				
			}
			m_sSumResults = sumResults();
			//iConcepts = 0; && iConcepts < m_iMaxConcepts 
			for ( POMConcept concept: lPOMConcepts ) {
				System.out.print( "." );
				if( iChange == Change.Type.ADD && iInstances < m_iMaxInstances )
				{ 
					iConcepts++; 
					POMInstanceOfRelation relation1 = m_pom.newInstanceOfRelation( instance, concept );
					double dProb1 = getProbability( relation1 );
					if( dProb1 > 0 )
					{	
						relation1.setProbability( dProb1 );
						pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, relation1, dProb1, change ) ) );
					}					
				}	
				else if( iChange == Change.Type.REMOVE )
				{	 
					List<POMAbstractRelation> relations = new ArrayList<POMAbstractRelation>();
					relations.addAll( m_pom.getRelationsWithDomain( POMInstance.class, instance) ); 
					for( POMAbstractRelation relation: relations )
					{
						pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, relation, change ) ) );
					}
				}
			}
		}
		for ( POMConcept concept: changedConcepts ) {
			ArrayList<Integer> types = new ArrayList<Integer>();
			types.add( Change.Type.ADD );
			types.add( Change.Type.REMOVE ); 
			Change change = m_pom.getLastChangeFor( this, concept, types );
			if (change == null)
				continue;
			int iChange = change.getType();
			if( iChange == Change.Type.REMOVE )
			{	 
				List<POMAbstractRelation> relations = new ArrayList<POMAbstractRelation>();
				relations.addAll( m_pom.getRelationsWithRange( POMConcept.class, concept) ); 
				for( POMAbstractRelation relation: relations )
				{
					pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, relation, change ) ) );
				}
			}
		}		
		return pomChangeRequests;
	}

	private HashMap getHMResults (String sWord) {
		HashMap<String,Integer> hmConcept2Occurrences = new HashMap();
		if (Settings.get( Settings.LANGUAGE ).compareTo( Settings.ENGLISH ) == 0 ) {
			m_GoogleWrapper.setQuery( m_GoogleWrapper.SUCH_AS, sWord );
		}
		else if (Settings.get( Settings.LANGUAGE ).compareTo( Settings.SPANISH ) == 0 ) {
			m_GoogleWrapper.setQuery( m_GoogleWrapper.COMO, sWord );
		}
		else if (Settings.get( Settings.LANGUAGE ).compareTo( Settings.GERMAN ) == 0 ) {
			m_GoogleWrapper.setQuery( m_GoogleWrapper.WIE, sWord );
		}
		try {
			m_GoogleWrapper.run();
			int iNumResults = m_GoogleWrapper.getResults();
			if ( iNumResults > 10 ) {
				for ( int iFirstIndex = 10; iFirstIndex < 100; iFirstIndex = iFirstIndex + 10 ) {
					m_GoogleWrapper.setStartResult ( iFirstIndex );
					hmConcept2Occurrences = m_GoogleWrapper.addResults2Hm( hmConcept2Occurrences );
					m_GoogleWrapper.run();
				}				
			}
			else if ( iNumResults < 100 ) {
				for ( int iFirstIndex = 10; iFirstIndex < iNumResults; iFirstIndex = iFirstIndex + 10 ) {
					m_GoogleWrapper.setStartResult ( iFirstIndex );
					hmConcept2Occurrences = m_GoogleWrapper.addResults2Hm( hmConcept2Occurrences );
					m_GoogleWrapper.run();
				}				
			}
			else {
				System.out.println( sWord );
				m_GoogleWrapper.addResults2Hm( hmConcept2Occurrences );
			}
			hmConcept2Occurrences = procesHashMap ( hmConcept2Occurrences );
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hmConcept2Occurrences;		
	}
 
	private HashMap<String, Integer> procesHashMap(HashMap<String, Integer> hmConcept2Occurrences) {
		try {
			HashMap<String,Integer> hmResult = new HashMap();
			String sKeySet = "";
			Set<String> setWords = hmConcept2Occurrences.keySet();
			Iterator wordsIterator = setWords.iterator();
			while ( wordsIterator.hasNext() ) {
				String sWord = (String) wordsIterator.next();
				sKeySet = sKeySet + " " + sWord;				
			}
			if ( sKeySet.split(" ").length < 2 ) {
				return hmResult;
			}
			ArrayList arWordLemma = m_lemmatizer.getWordsLemmas( sKeySet );
			Iterator iterator = arWordLemma.iterator();
			while ( iterator.hasNext() ) {
				String[] asWordLemma = (String[]) iterator.next();
				int iOcurrencesOfWord = 0;
 				if ( hmConcept2Occurrences.containsKey( asWordLemma[0] ) ) {
					iOcurrencesOfWord = hmConcept2Occurrences.get( asWordLemma[0] );
				}				
				if (hmResult.containsKey( asWordLemma[1] ) ) {
					hmResult.put( asWordLemma[1], hmResult.get( asWordLemma[1] ) + iOcurrencesOfWord );
				}
				else {
					hmResult.put( asWordLemma[1], iOcurrencesOfWord );
				}
			}
			return hmResult;
		} catch ( Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}
	
	private long sumResults() {
		long iTotalResults = 0;
		for ( String concept: m_hmConcept2Results.keySet() ) {
			iTotalResults = iTotalResults + m_hmConcept2Results.get(concept).longValue();
		}
		return iTotalResults;
	}
	
	private double getProbability(POMInstanceOfRelation relation) {
		String sDomain = relation.getDomain().getLabel();
		String sRange = relation.getRange().getLabel();
		if (m_sSumResults == 0) {
			return 0;
		}
		String s = relation.getDomain().getLabel();
		Integer integerResult = m_hmConcept2Results.get(relation.getRange().getLabel());
		if ( integerResult == null ) {
			return 0;
		}
		double num1 = integerResult.doubleValue();
		double num2 = m_sSumResults;
		double dResult = num1/num2;
		return dResult;
	}
}

