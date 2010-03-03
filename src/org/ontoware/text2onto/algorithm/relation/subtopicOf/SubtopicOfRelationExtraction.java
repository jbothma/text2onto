package org.ontoware.text2onto.algorithm.relation.subtopicOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
import org.ontoware.text2onto.algorithm.relation.AbstractRelationExtraction;
import org.ontoware.text2onto.change.Change; 
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.change.EvidenceChange;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.pom.POMAbstractObject;
import org.ontoware.text2onto.pom.POMSubtopicOfRelation;
import org.ontoware.text2onto.pom.POMConcept;  
import org.ontoware.text2onto.reference.document.DocumentReferenceStore;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.evidence.reference.ObjectDocumentStore;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.explanation.reference.ObjectDocumentExplanation;
import org.ontoware.text2onto.util.ProbabilityComparator;

/* 
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class SubtopicOfRelationExtraction extends AbstractSimpleAlgorithm implements AbstractRelationExtraction {
 
	private final static int m_iMax = 100; 
 
	protected void initialize() {
		m_algorithmController.requestGlobalEvidenceStore( this, ReferenceStore.class );
		m_algorithmController.requestLocalEvidenceStore( this, ObjectDocumentStore.class );
		m_algorithmController.requestLocalReferenceStore( this, DocumentReferenceStore.class );
	}
	
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		List<ChangeRequest> evidenceChangeRequests = new ArrayList<ChangeRequest>();
		List<Change> corpusChanges = m_corpus.getChangesFor(this);
		ReferenceStore refStore = getEvidenceStore( ReferenceStore.class );
		// System.out.println( "SubtopicOfRelationExtraction: "+ refStore );
		  
		for( Change change: corpusChanges )
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			System.out.println( "SubtopicOfRelationExtraction.getEvidenceChanges: "+ doc );
			List<POMObject> objects = refStore.getObjects( doc );
			for( POMObject object: objects )
			{
				if( !( object instanceof POMConcept ) ){
					continue;
				}  
				evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( iChange, this, 
						getLocalEvidenceStore( ObjectDocumentStore.class ), object, doc, change ) ) ); 
			} 
		}
		return evidenceChangeRequests;
	}

	protected List<ChangeRequest> getPOMChanges(){
		List<ChangeRequest> pomChangeRequests = new ArrayList<ChangeRequest>();
		ObjectDocumentStore objDocStore = (ObjectDocumentStore)getLocalEvidenceStore( ObjectDocumentStore.class );
		List<Object> objects = new ArrayList<Object>();
		objects.addAll( objDocStore.getChangedObjectsFor( this ) ); 
		
		int iConcepts = 0;
		Collections.sort( objects, new ProbabilityComparator() );
		for( Object object: objects )
		{
			if( !( object instanceof POMConcept ) ){
				continue;
			}
			else if( iConcepts > m_iMax ){
				break;
			}
			iConcepts++;
			System.err.print( "." );
			
			POMConcept concept = (POMConcept)object;
			List<Change> changes = objDocStore.getChangesFor( this, concept );
			// System.out.println( "\nconcept="+ concept +" -> "+ objDocStore.getDocuments( concept ) );
			int iDocs = objDocStore.getDocuments().size();
			int iConceptDocs = objDocStore.getDocuments( concept ).size();
			List<POMAbstractObject> subObjects = objDocStore.getSubsetObjects( concept, m_iMax );
			// List<POMAbstractObject> superObjects = objDocStore.getSupersetObjects( concept, m_iMax );
			for( POMAbstractObject subObj: subObjects )
			{ 
				if( subObj.equals( concept ) 
					|| ( subObj.getLabel().indexOf( concept.getLabel() ) != -1 ) )
				{
					continue;
				}
				// System.out.println( "subset="+ subObj +" -> "+ objDocStore.getDocuments( subObj ) );
				int iSubDocs = objDocStore.getDocuments( subObj ).size();
				double dProb = Math.max( 0.0, (double)iSubDocs / (double)iConceptDocs );
				// System.out.println( "dProb="+ dProb +", iSubDocs="+ iSubDocs +", iConceptDocs="+ iConceptDocs +", iDocs="+ iDocs );
				POMSubtopicOfRelation relation = m_pom.newSubtopicOfRelation( (POMConcept)subObj, concept ); 
				relation.setProbability( dProb );
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.MODIFY, this, relation, dProb, changes ) ) );
			}
			/* for( POMAbstractObject superObj: superObjects ){
				int iSuperDocs = objDocStore.getDocuments( superObj ).size();
				POMSubtopicOfRelation relation = m_pom.newSubtopicOfRelation( concept, (POMConcept)superObj );
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.MODIFY, this, relation, 1.0, changes ) ) );
			} */
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
		ObjectDocumentStore objDocStore = (ObjectDocumentStore)getLocalEvidenceStore( ObjectDocumentStore.class );
		List<AbstractDocument> domainDocs = objDocStore.getDocuments( relation.getDomain() );
		List<AbstractDocument> rangeDocs = objDocStore.getDocuments( relation.getRange() );
		explanation.setDocuments( domainDocs, rangeDocs );
		return explanation;
	}

	protected List<ChangeRequest> getReferenceChanges() {
		return new ArrayList<ChangeRequest>();
	}
	
	private ReferenceStore getEvidenceStore( Class storeClass ){
		return (ReferenceStore)getGlobalEvidenceStore( storeClass );
	}
}