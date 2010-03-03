package org.ontoware.text2onto.algorithm.taxonomic.instanceOf;
 
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm; 
import org.ontoware.text2onto.explanation.AbstractExplanation; 
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMConcept; 
import org.ontoware.text2onto.pom.POMInstance;
import org.ontoware.text2onto.pom.POMInstanceOfRelation; 
import org.ontoware.text2onto.pom.POMSimilarityRelation;
import org.ontoware.text2onto.util.ProbabilityComparator;

/*
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class ContextInstanceClassification extends AbstractSimpleAlgorithm implements AbstractInstanceClassification { 
 
	protected void initialize() { 
		// TODO
	}

	protected List<ChangeRequest> getEvidenceChanges() {
		return new ArrayList<ChangeRequest>();
	}
	
	protected AbstractExplanation getExplanation( POMChange change ) {
		return null;
	}
 
	protected List<ChangeRequest> getReferenceChanges() {
		return new ArrayList<ChangeRequest>();
	}

	protected List<ChangeRequest> getPOMChanges() throws Exception {
		List<ChangeRequest> pomChangeRequests= new ArrayList<ChangeRequest>();
		List<Object> objects = m_pom.getChangedObjectsFor( this );
		ArrayList<POMSimilarityRelation> changedRelations = new ArrayList<POMSimilarityRelation>(); 
		for( Object object: objects )
		{
			if( object instanceof POMSimilarityRelation ){
				changedRelations.add( (POMSimilarityRelation)object );
			}
		}	
		System.out.println( "ContextInstanceClassification.getPOMChanges: "+ changedRelations ); 
		Collections.sort( changedRelations, new ProbabilityComparator() );
		
		HashMap hmInst2MaxSim = new HashMap();
		HashMap hmInst2Concept = new HashMap();
		HashMap hmInst2Change = new HashMap();
		
		for( POMSimilarityRelation relation: changedRelations )
		{   
			POMEntity domain = relation.getDomain();
			POMEntity range = relation.getRange();
			if( ( domain instanceof POMInstance && range instanceof POMConcept ) 
				/* || ( domain instanceof POMConcept && range instanceof POMInstance ) */ )
			{
				ArrayList<Integer> types = new ArrayList<Integer>();
				types.add( Change.Type.ADD );
				types.add( Change.Type.REMOVE ); 
				Change change = m_pom.getLastChangeFor( this, relation, types );

				double dSim = relation.getProbability();
				Double dMaxSim = (Double)hmInst2MaxSim.get( domain );
				if( dMaxSim == null || dSim > dMaxSim.doubleValue() )
				{
					hmInst2MaxSim.put( domain, new Double( dSim ) );
					hmInst2Concept.put( domain, range );
					hmInst2Change.put( domain, change );
				} 				
 			}
 		}
		Iterator instIter = hmInst2Concept.keySet().iterator();
		while( instIter.hasNext() )
		{			
			POMInstance instance = (POMInstance)instIter.next();
			POMConcept concept = (POMConcept)hmInst2Concept.get( instance );
			Change change = (Change)hmInst2Change.get( instance );
			Double dSim = (Double)hmInst2MaxSim.get( instance );
				
			POMInstanceOfRelation instRel = m_pom.newInstanceOfRelation( instance, concept ); 
			instRel.setProbability( dSim.doubleValue() );
				
			if( dSim == 0.0 ){
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, instRel, change ) ) );
			}
			else {
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, instRel, dSim, change ) ) );
			}  	
		}
		return pomChangeRequests;
	}
}
