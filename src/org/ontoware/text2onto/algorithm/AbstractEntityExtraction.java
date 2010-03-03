package org.ontoware.text2onto.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.EvidenceChange;
import org.ontoware.text2onto.change.ReferenceChange;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.reference.document.DocumentReferenceStore;

/**
 * @author Günter Ladwig
 */
public abstract class AbstractEntityExtraction extends AbstractSimpleAlgorithm {

	protected void initialize() {
		// m_algorithmController.requestLocalEvidenceStore( this, ReferenceStore.class );
		m_algorithmController.requestGlobalEvidenceStore( this, ReferenceStore.class );
		m_algorithmController.requestLocalReferenceStore( this, DocumentReferenceStore.class ); 
	}
	 
	protected List<ChangeRequest> getEvidenceChanges() {
		List<ChangeRequest> evidenceChangeRequests = new ArrayList<ChangeRequest>();
		List<Change> corpusChanges = m_corpus.getChangesFor( this ); 
		
		for( Change change: corpusChanges )
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			System.out.println( "AbstractEntityExtraction.getEvidenceChanges: "+ doc + ", type: " + iChange );
			
			HashMap<POMEntity,List<DocumentReference>> hmEntity2References = new HashMap<POMEntity,List<DocumentReference>>(); 
			if( iChange == Change.Type.ADD ){
				hmEntity2References = getEntity2References( doc );
			}
			else {
				ReferenceStore referenceStore = getEvidenceStore( ReferenceStore.class );
				List<POMObject> objects = referenceStore.getObjects( doc );
				for( Object object: objects )
				{
					List<DocumentReference> references = referenceStore.getReferences( (POMEntity)object, doc );
					hmEntity2References.put( (POMEntity)object, references );
				}					 
			}
			for( POMEntity entity: hmEntity2References.keySet() ) 
			{ 
				for( DocumentReference reference: hmEntity2References.get( entity ) )
				{
					if( iChange == Change.Type.ADD ) 
					{
						evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.ADD, 
							this, getEvidenceStore( ReferenceStore.class ), entity, reference, change ) ) );
					} 
					else if( iChange == Change.Type.REMOVE ) 
					{ 
						evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE, 
							this, getEvidenceStore( ReferenceStore.class ), entity, reference, change ) ) );
					}
				}
			}
		}
		return evidenceChangeRequests;
	}
    
	protected AbstractExplanation getExplanation( POMChange change ) {
		return null;
	}
 
	protected List<ChangeRequest> getReferenceChanges() {
		List<ChangeRequest> referenceChangeRequests= new ArrayList<ChangeRequest>();
		/* List<Change> corpusChanges = m_corpus.getChangesFor( this ); 
		
		for( Change change: corpusChanges )
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			System.out.println( "AbstractEntityExtraction.getReferenceChanges: "+ doc + ", type: " + iChange );
			
			HashMap<POMEntity,List<DocumentReference>> hmEntity2References = new HashMap<POMEntity,List<DocumentReference>>(); 
			if( iChange == Change.Type.ADD ){
				hmEntity2References = getEntity2References( doc );
			}
			else {
				DocumentReferenceStore referenceStore = (DocumentReferenceStore)getLocalReferenceStore( DocumentReferenceStore.class );
				List<POMObject> objects = referenceStore.getObjects( doc );
				for( Object object: objects )
				{
					List<DocumentReference> references = referenceStore.getReferences( (POMEntity)object, doc );
					hmEntity2References.put( (POMEntity)object, references );
				}					 
			}
			for( POMEntity entity: hmEntity2References.keySet() ) 
			{ 
				for( DocumentReference reference: hmEntity2References.get( entity ) )
				{
					if( iChange == Change.Type.ADD ) 
					{
						referenceChangeRequests.add( new ChangeRequest( new ReferenceChange( Change.Type.ADD, 
							this, getLocalReferenceStore( DocumentReferenceStore.class ), entity, reference, change ) ) );
					} 
					else if( iChange == Change.Type.REMOVE ) 
					{ 
						referenceChangeRequests.add( new ChangeRequest( new ReferenceChange( Change.Type.REMOVE, 
							this, getLocalReferenceStore( DocumentReferenceStore.class ), entity, reference, change ) ) );
					}
				}
			}
		} */
		return referenceChangeRequests;
	}
 
	protected List<ChangeRequest> getPOMChanges() {
		List<ChangeRequest> pomChangeRequests = new ArrayList<ChangeRequest>();
		ReferenceStore referenceStore = getEvidenceStore( ReferenceStore.class );
		List<Object> objects = referenceStore.getChangedObjectsFor( this );
		int iReferences = countAllDocumentReferences();
		for( Object obj: objects )
		{
			POMObject object = (POMObject)obj;
			List<Change> changes = referenceStore.getChangesFor( this, object );
			if (object.getLabel().compareTo( "pensión" ) == 0 ) {
				System.out.print( "borrar esto en abstractentityextraction");
			}
			double dProb = getProbability( object, iReferences );
			object.setProbability( dProb );
			prepareNormalization( dProb );
			if( dProb == 0.0 ){
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, object, dProb, changes ) ) );
			}
			else {
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, object, dProb, changes ) ) );
			}
		}
		return normalize( pomChangeRequests );
	}

	private int countAllDocumentReferences(){
		ReferenceStore rStore = getEvidenceStore( ReferenceStore.class ); 
		List objects = rStore.getObjects();
		int iReferences = 0;
		for (Iterator i = objects.iterator(); i.hasNext(); )
		{
			POMObject object = (POMObject)i.next();
			iReferences += rStore.getReferences( object ).size();
		}
		return iReferences;
	}
	
	protected String getEntity( DocumentReference reference ){
		return m_analyser.getObjectLabel( reference );
	}
	
	protected ReferenceStore getEvidenceStore( Class storeClass ){
		return (ReferenceStore)getGlobalEvidenceStore( storeClass );
	}
	
	protected abstract HashMap<POMEntity,List<DocumentReference>> getEntity2References( AbstractDocument doc );
	
	protected abstract double getProbability( POMObject object, int iReferences );

	protected abstract void prepareNormalization ( Double dProb );

	protected abstract List<ChangeRequest> normalize( List<ChangeRequest> alList ); 
}
