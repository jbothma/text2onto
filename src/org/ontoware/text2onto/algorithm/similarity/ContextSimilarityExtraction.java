package org.ontoware.text2onto.algorithm.similarity;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMInstance;
import org.ontoware.text2onto.pom.POMSingleEntity;
import org.ontoware.text2onto.pom.POMSimilarityRelation;
import org.ontoware.text2onto.evidence.context.ContextVector;
import org.ontoware.text2onto.evidence.context.ContextVectorStore;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.util.ProbabilityComparator;

/**
 * @author Günter Ladwig
 */
public class ContextSimilarityExtraction extends AbstractSimilarityExtraction {
    
	private final static int m_iMax = 100;
	  
   
	protected void initialize() {
		m_algorithmController.requestLocalEvidenceStore( this, ReferenceStore.class );
		m_algorithmController.requestLocalEvidenceStore( this, ContextVectorStore.class );
	}

	protected List<ChangeRequest> getEvidenceChanges() {
		return new ArrayList<ChangeRequest>();
	}

	protected List<ChangeRequest> getPOMChanges() { 
		List<ChangeRequest> pomChangeRequests = new ArrayList<ChangeRequest>();
		ContextVectorStore cvStore = (ContextVectorStore)getLocalEvidenceStore( ContextVectorStore.class );
		cvStore.addChangeObserver( this );
		List<Object> objects = cvStore.getChangedObjectsFor( this );
		ArrayList<POMEntity> changedEntities = new ArrayList<POMEntity>(); 
		for( Object object: objects )
		{
			if( object instanceof POMSingleEntity ){
				changedEntities.add( (POMEntity)object );
			}
		}	
		System.out.println( "ContextSimilarityExtraction.getPOMChanges: "+ changedEntities ); 
		Collections.sort( changedEntities, new ProbabilityComparator() );

		int iConcepts = 0;
		int iInstances = 0; 
		for( POMEntity entity1 : changedEntities ) 
		{  
			if( entity1 instanceof POMConcept && iConcepts > m_iMax ){
			 	continue;
			}
			else if( entity1 instanceof POMInstance && iInstances > m_iMax ){
				continue;
			} 
			ArrayList<Integer> types = new ArrayList<Integer>();
			types.add( Change.Type.ADD );
			types.add( Change.Type.REMOVE ); 
			Change change = m_pom.getLastChangeFor( this, entity1, types );

			HashMap<POMEntity,Double> hmEntity2Sim = getSimilarEntities( entity1 ); 
			for( POMEntity entity2 : hmEntity2Sim.keySet() ) 
			{ 
				double dSim = hmEntity2Sim.get( entity2 );
				POMSimilarityRelation rel = m_pom.newSimilarityRelation(	entity1, entity2 );
				rel.setProbability( dSim );
				if( dSim == 0.0 ){
					pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, rel, dSim, change ) ) );
				}
				else {	
					pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, rel, dSim, change ) ) );
				}   
			} 
			if( entity1 instanceof POMConcept ){
				iConcepts++;
			}
			else if( entity1 instanceof POMInstance ){
				iInstances++;
			}
		}
		return pomChangeRequests;
	}

	protected AbstractExplanation getExplanation( POMChange change ) {
		return null;
	}

	protected List<ChangeRequest> getReferenceChanges() {
		return new ArrayList<ChangeRequest>();
	}

	private HashMap<POMEntity,Double> getSimilarEntities( POMEntity entity1 ) {
		System.out.print( "*" );
		HashMap<POMEntity,Double> hmEntity2Sim = new HashMap<POMEntity,Double>();
		List<POMEntity> entities = new ArrayList<POMEntity>();
		entities.addAll( m_pom.getObjects( POMConcept.class ) );
		// entities.addAll( m_pom.getObjects( POMInstance.class ) );
		Collections.sort( entities, new ProbabilityComparator() );
		int iConcepts = 0;
		int iInstances = 0;
		for( POMEntity entity2: entities ) 
		{ 
			System.out.print( "." );
			if( entity2 instanceof POMConcept && iConcepts > m_iMax / 2 ){
				continue;
			}
			else if( entity2 instanceof POMInstance && iInstances > m_iMax / 2 ){
				continue;
			}
			if( entity2.equals( entity1 ) ){
				continue;
			}
			ContextVector cv1 = ( (ContextVectorStore) getLocalEvidenceStore( ContextVectorStore.class ) ).getContextVector( entity1 );
			ContextVector cv2 = ( (ContextVectorStore) getLocalEvidenceStore( ContextVectorStore.class ) ).getContextVector( entity2 );
			// is this correct?
			double dSim;
			if( cv1 == null || cv2 == null ){
				dSim = 0.0;
			} else {
				dSim = cv1.getCosinusSimilarity( cv2 );
			}
			hmEntity2Sim.put( entity2, dSim );
			if( entity2 instanceof POMConcept ){
				iConcepts++;
			}
			else if( entity2 instanceof POMInstance ){
				iInstances++;
			}
		}
		return hmEntity2Sim;
	}
}
