package org.ontoware.text2onto.algorithm.auxiliary.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractAuxiliaryAlgorithm;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.EvidenceChange;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.evidence.context.ContextVectorStore;
import org.ontoware.text2onto.evidence.context.ContextVector;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMInstance;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMAbstractObject;
import org.ontoware.text2onto.reference.document.DocumentReference;

/**
 * @author Günter Ladwig
 */
public abstract class AbstractContextExtraction extends AbstractAuxiliaryAlgorithm {

	protected void initialize() {
		m_algorithmController.requestLocalEvidenceStore( this, ContextVectorStore.class );
		m_algorithmController.requestGlobalEvidenceStore( this, ReferenceStore.class );
	}
 
	protected List<ChangeRequest> getEvidenceChanges() {
		List<ChangeRequest> contextChanges = new ArrayList<ChangeRequest>();
		List<Change> corpusChanges = m_corpus.getChangesFor( this );
		HashMap<POMEntity,ContextVector> hmEntity2Context = new HashMap<POMEntity,ContextVector>();
		for( Change change : corpusChanges ) 
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument) change.getObject();
			System.out.println( "AbstractContextExtraction: "+ doc );

			HashMap<POMEntity,List<String>> hmEntity2Features = getEntityFeatures( doc );
			for( POMEntity entity : hmEntity2Features.keySet() ) 
			{
				List<String> features = hmEntity2Features.get( entity );
				ContextVector cv = (ContextVector) hmEntity2Context.get( entity );
				if( cv == null ) 
				{
					cv = new ContextVector();
					hmEntity2Context.put( entity, cv );
				}
				for( String sFeature : features ) 
				{
					if( iChange == Change.Type.ADD ) {
						cv.addFeature( sFeature );
					} 
					else if ( iChange == Change.Type.REMOVE ) {
						cv.removeFeature( sFeature );
					}
				}
			}
			for( POMEntity entity : hmEntity2Features.keySet() ) 
			{
				ContextVector cv = (ContextVector) hmEntity2Context.get( entity );
				if( cv.size() == 0 ) 
				{
					contextChanges.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE, this, 
						getLocalEvidenceStore( ContextVectorStore.class ), entity, cv, change ) ) );
				} 
				else {
					contextChanges.add( new ChangeRequest( new EvidenceChange( Change.Type.MODIFY, this, 
						getLocalEvidenceStore( ContextVectorStore.class ), entity, cv, change ) ) );
				}
			}
		}
		// System.out.println( contextChanges );
		return contextChanges;
	}

	private HashMap<POMEntity,List<String>> getEntityFeatures( AbstractDocument doc ) {
		HashMap<POMEntity,List<String>> hmEntity2Features = new HashMap<POMEntity,List<String>>();
		List<Change> changes = m_pom.getChangesFor( this ); 
		for( Change c: changes )
		{ 
			POMObject entity = (POMObject)c.getObject();
			ArrayList alFeatures = new ArrayList();
			List references = new ArrayList();
			if( entity instanceof POMConcept || entity instanceof POMInstance ) 
			{
				references = ((ReferenceStore) getGlobalEvidenceStore( ReferenceStore.class )).getReferences( (POMAbstractObject) entity, doc );
			} 
			else {
				continue;
			}
			Iterator referenceIter = references.iterator();
			while( referenceIter.hasNext() ) 
			{
				DocumentReference reference = (DocumentReference) referenceIter.next();
				alFeatures.addAll( getFeatures( reference ) );
			}
			hmEntity2Features.put( (POMEntity)entity, alFeatures );
		}
		return hmEntity2Features;
	}

	protected abstract List<String> getFeatures( DocumentReference entityReference );
}
