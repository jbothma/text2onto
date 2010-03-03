package org.ontoware.text2onto.algorithm.taxonomic.subclassOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.linguistic.Lemmatizer;
import org.ontoware.text2onto.pom.POMAbstractRelation;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMSubclassOfRelation;
import org.ontoware.text2onto.reference.document.DocumentReferenceStore;
import org.ontoware.text2onto.util.ProbabilityComparator;
import org.ontoware.text2onto.util.spanishwordnet.SpanishWordNet;



/**
 * @author Sergi
 *
 */

public class SpanishWordNetConceptClassification extends AbstractSimpleAlgorithm implements AbstractConceptClassification {
 
	private SpanishWordNet m_spanishWordNet; 
	
	private final int m_iMaxConcepts1 = 100;
	
	private final int m_iMaxConcepts2 = 100;
	
	//private final Lemmatizer m_lemmatizer = new Lemmatizer();
	 
 
	protected void initialize() { 
		m_spanishWordNet = SpanishWordNet.getWordNet();
		m_algorithmController.requestLocalReferenceStore( this, DocumentReferenceStore.class );
	}
	
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		return new ArrayList<ChangeRequest>();
	}
	
	protected List<ChangeRequest> getPOMChanges() throws Exception {
		List<ChangeRequest> pomChangeRequests= new ArrayList<ChangeRequest>();
		List<Object> objects = m_pom.getChangedObjectsFor( this );
		ArrayList<POMConcept> changedConcepts = new ArrayList<POMConcept>(); 
		for( Object object: objects )
		{
			if( object instanceof POMConcept ){
				changedConcepts.add( (POMConcept)object );
			}
		}	
		System.out.println( "SpanishWordNetConceptClassification.getPOMChanges: "+ changedConcepts );
		int iConcepts1 = 0;
		Collections.sort( changedConcepts, new ProbabilityComparator() );
		for( POMConcept concept1: changedConcepts )
		{   
			iConcepts1++;
			int iConcepts2 = 0; 
			List<Change> allChanges = m_pom.getChangesFor( this, concept1 );
			ArrayList<Integer> types = new ArrayList<Integer>();
			types.add( Change.Type.ADD );
			types.add( Change.Type.REMOVE ); 
			Change change = m_pom.getLastChangeFor( this, concept1, types );
            if (change == null)
                continue;
			int iChange = change.getType();
			
			List<POMConcept> concepts = m_pom.getObjects( POMConcept.class );
			System.out.print( "*" ); 
			Collections.sort( concepts, new ProbabilityComparator() );
			for( POMConcept concept2: concepts )
			{
				if( concept1.equals( concept2 ) ){
					continue;
				}
//				System.out.print( "." );
				if( iChange == Change.Type.ADD 
					&& iConcepts1 < m_iMaxConcepts1 && iConcepts2 < m_iMaxConcepts2 )
				{ 
					iConcepts2++; 
				
					POMSubclassOfRelation relation1 = m_pom.newSubclassOfRelation( concept1, concept2 );
					double dProb1 = getProbability( relation1 );
					if( dProb1 > 0 )
					{	
						relation1.setProbability( dProb1 );
						pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, relation1, dProb1, change ) ) );
					}
					POMSubclassOfRelation relation2 = m_pom.newSubclassOfRelation( concept2, concept1 );
					double dProb2 = getProbability( relation2 );
					if( dProb2 > 0 )
					{
						relation2.setProbability( dProb2 );
						pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, relation2, dProb2, change ) ) );
					} 
				}	
				else if( iChange == Change.Type.REMOVE )
				{	 
					List<POMAbstractRelation> relations = new ArrayList<POMAbstractRelation>();
					relations.addAll( m_pom.getRelations( POMSubclassOfRelation.class, concept1, concept2 ) ); 
					relations.addAll( m_pom.getRelations( POMSubclassOfRelation.class, concept2, concept1 ) );
					for( POMAbstractRelation relation: relations )
					{
						pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, relation, change ) ) );
					}
				}
			} 
		}
		System.out.println();
		return pomChangeRequests;
	}
	
	protected AbstractExplanation getExplanation( POMChange change ) {
		return null;
	}
 
	protected List<ChangeRequest> getReferenceChanges() {
		return new ArrayList<ChangeRequest>();
	}
  
	private Double getProbability( POMSubclassOfRelation relation ) throws Exception { 
		POMConcept domain = relation.getDomain();
		POMConcept range = relation.getRange(); 
		String s = "";
		/*String SRange  = m_lemmatizer.getLemma( range.getLabel() );
		String sDomain = m_lemmatizer.getLemma( domain.getLabel() );*/
		//m_lemmatizer.getLemma( range.getLabel() );
		String sRange  = range.getLabel();
		String sDomain = domain.getLabel();
		return m_spanishWordNet.isHypernymOf( sRange, sDomain );	 
	} 
}