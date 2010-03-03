package org.ontoware.text2onto.algorithm.taxonomic.instanceOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
import org.ontoware.text2onto.algorithm.taxonomic.AbstractPatternClassification;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.pom.POMAbstractRelation;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMInstance;
import org.ontoware.text2onto.pom.POMInstanceOfRelation;
import org.ontoware.text2onto.pom.POMSimilarityRelation;
import org.ontoware.text2onto.pom.POMSubclassOfRelation;
import org.ontoware.text2onto.pom.POMTaxonomicRelation;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.util.ProbabilityComparator;
import org.ontoware.text2onto.util.Settings;
import org.ontoware.text2onto.util.google.GoogleWrapper;

public class GoogleInstanceClassification extends AbstractSimpleAlgorithm implements AbstractInstanceClassification {

	private final int m_iMaxInstances = 10;
	
	private final int m_iMaxConcepts = 10;
	
	private GoogleWrapper m_GoogleWrapper;
	
	private long m_sSumResults;
	
	private HashMap<String,Integer> m_hmConcept2Results;
	
	protected void initialize() {
		m_GoogleWrapper = new GoogleWrapper(); 
		m_GoogleWrapper.init();
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
			m_hmConcept2Results = new HashMap();;
			System.out.print( "*" ); 
			Collections.sort( lPOMConcepts, new ProbabilityComparator() );
			int iConcepts = 0;
			for ( POMConcept concept: lPOMConcepts ) {
				if( iChange == Change.Type.ADD 
						&& iInstances < m_iMaxInstances && iConcepts < m_iMaxConcepts )
					{ 
						iConcepts++;
						POMInstanceOfRelation relation = m_pom.newInstanceOfRelation( instance, concept );
						m_hmConcept2Results.put( concept.getLabel(), getResults (relation) );
					}
			}
			m_sSumResults = sumResults();
			iConcepts = 0;
			for ( POMConcept concept: lPOMConcepts ) {
				System.out.print( "." );
				if( iChange == Change.Type.ADD 
					&& iInstances < m_iMaxInstances && iConcepts < m_iMaxConcepts )
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

	private Integer getResults (POMInstanceOfRelation relation) throws Exception {
		String sDomain = relation.getDomain().getLabel();
		String sRange = relation.getRange().getLabel();
		int iNumOcurrences = 0;
		if (Settings.get( Settings.LANGUAGE ).compareTo( Settings.ENGLISH ) == 0 ) {
			m_GoogleWrapper.setQuery( GoogleWrapper.IS_A, sDomain ,sRange );
			m_GoogleWrapper.run();
			iNumOcurrences = m_GoogleWrapper.getResults();
			m_GoogleWrapper.setQuery( GoogleWrapper.SUCH_AS, sRange, sDomain );
			m_GoogleWrapper.run();
			iNumOcurrences = iNumOcurrences + m_GoogleWrapper.getResults();
		}
		else if (Settings.get( Settings.LANGUAGE ).compareTo( Settings.SPANISH ) == 0 ) {
			m_GoogleWrapper.setQuery( GoogleWrapper.COMO, sDomain ,sRange );
			m_GoogleWrapper.run();
			iNumOcurrences = m_GoogleWrapper.getResults();
		}
		else if (Settings.get( Settings.LANGUAGE ).compareTo( Settings.GERMAN ) == 0 ) {
			m_GoogleWrapper.setQuery( GoogleWrapper.WIE, sDomain ,sRange );
			m_GoogleWrapper.run();
			iNumOcurrences = m_GoogleWrapper.getResults();
		}
		return new Integer(iNumOcurrences);
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
		long lResult = integerResult.longValue()/m_sSumResults;
		return lResult;
	}
}
