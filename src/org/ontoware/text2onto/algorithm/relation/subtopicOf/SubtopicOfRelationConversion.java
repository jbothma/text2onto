package org.ontoware.text2onto.algorithm.relation.subtopicOf;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
import org.ontoware.text2onto.algorithm.relation.AbstractRelationExtraction;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.explanation.reference.ObjectDocumentExplanation;
import org.ontoware.text2onto.pom.POMAbstractRelation;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMSubclassOfRelation;
import org.ontoware.text2onto.pom.POMSubtopicOfRelation;

public class SubtopicOfRelationConversion extends AbstractSimpleAlgorithm implements AbstractRelationExtraction {

	protected void initialize() {
		// TODO
	}
	
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		List<ChangeRequest> evidenceChangeRequests = new ArrayList<ChangeRequest>();
		return evidenceChangeRequests;
	}

	protected List<ChangeRequest> getPOMChanges(){
		List<ChangeRequest> pomChangeRequests = new ArrayList<ChangeRequest>(); 
		List<Object> objects = new ArrayList<Object>();
		objects.addAll( m_pom.getChangedObjectsFor( this ) );
		for( Object object: objects )
		{
			if( !( object instanceof POMSubclassOfRelation ) ){
				continue;
			}
			POMAbstractRelation relation = (POMAbstractRelation)object;
			List<Change> changes = m_pom.getChangesFor( this, relation );
			Change lastChange = (Change)changes.get( changes.size()-1 );
			System.out.println( "\nrelation="+ relation +" -> "+ changes );  
			
			double dProb = relation.getProbability();
			POMSubtopicOfRelation subtopicOf = m_pom.newSubtopicOfRelation( (POMConcept)relation.getDomain(), (POMConcept)relation.getRange() ); 
			subtopicOf.setProbability( dProb );
			if( lastChange.getType() == Change.Type.REMOVE ){
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, subtopicOf, dProb, changes ) ) );
			}
			else {
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.MODIFY, this, subtopicOf, dProb, changes ) ) );
			}		
		}
		System.out.println( "\nSubtopicOfRelationExtraction: "+ pomChangeRequests );
		return pomChangeRequests;
	}

	protected AbstractExplanation getExplanation( POMChange change ) {
		Object object = change.getObject();
		if( !( object instanceof POMSubtopicOfRelation ) ){
			return null;
		}
		POMSubtopicOfRelation relation = (POMSubtopicOfRelation)object;
		ObjectDocumentExplanation explanation = new ObjectDocumentExplanation( relation, this, change );
		// TODO
		return explanation;
	}

	protected List<ChangeRequest> getReferenceChanges() {
		return new ArrayList<ChangeRequest>();
	}
}