package org.ontoware.text2onto.evidence.reference;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.evidence.AbstractEvidenceStore;
import org.ontoware.text2onto.pom.POMAbstractObject;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.util.ProbabilityComparator;


public class ObjectDocumentStore extends AbstractEvidenceStore {

	private ArrayList<AbstractDocument> m_documents;
	
	private HashMap<POMAbstractObject,DocumentVector> m_hmObject2Docs; 
	
	
	public ObjectDocumentStore(){
		m_documents = new ArrayList<AbstractDocument>();
		m_hmObject2Docs = new HashMap<POMAbstractObject,DocumentVector>(); 
	}
	
	public List<AbstractDocument> getDocuments( POMAbstractObject object ){
		return getDocumentVector( object ).getDocuments();
	}
	
	private void addDocument( POMAbstractObject object, AbstractDocument doc ){
		getDocumentVector( object ).addDocument( doc ); 
		m_documents.add( doc );
	}
	
	private void removeDocument( POMAbstractObject object, AbstractDocument doc ){
		getDocumentVector( object ).removeDocument( doc );
		m_documents.remove( doc );
	}
	
	public List<AbstractDocument> getDocuments(){
		return m_documents;
	}
	
	public List<POMAbstractObject> getSubsetObjects( POMAbstractObject object, int iMax ){
		ArrayList<POMAbstractObject> result = new ArrayList<POMAbstractObject>(); 
		DocumentVector dv = getDocumentVector( object );
		
		List<POMAbstractObject> objects = new ArrayList<POMAbstractObject>( m_hmObject2Docs.keySet() );
		Collections.sort( objects, new ProbabilityComparator() );
		int iObjects = 0;
		for( POMAbstractObject other: objects )
		{
			if( iObjects > iMax ){
				break;
			}
			DocumentVector dvOther = getDocumentVector( other );
			if( dv.includes( dvOther ) ){
				result.add( other );
				iObjects++;
			}
		}
		return result;
	}
	
	public List<POMAbstractObject> getSupersetObjects( POMAbstractObject object, int iMax ){
		ArrayList<POMAbstractObject> result = new ArrayList<POMAbstractObject>();
		DocumentVector dv = getDocumentVector( object );
		
		List<POMAbstractObject> objects = new ArrayList<POMAbstractObject>( m_hmObject2Docs.keySet() );
		Collections.sort( objects, new ProbabilityComparator() );
		int iObjects = 0;
		for( POMAbstractObject other: objects )
		{
			if( iObjects > iMax ){
				break;
			}
			DocumentVector dvOther = getDocumentVector( other );
			if( dvOther.includes( dv ) ){
				result.add( other );
				iObjects++;
			}
		}
		return result;
	}
	
	private DocumentVector getDocumentVector( POMAbstractObject object ){
		DocumentVector dv = (DocumentVector)m_hmObject2Docs.get( object );
		if( dv == null ){
			dv = new DocumentVector( object );
			m_hmObject2Docs.put( object, dv );
		}
		return dv;
	}
	
	/* Changeable */
	
	protected void executeChange( Change change ){
		switch( change.getType() ){
			case Change.Type.ADD: 
				executeAdd( change ); 
				break;
			case Change.Type.REMOVE: 
				executeRemove( change ); 
				break; 
		}
	}

	protected void executeAdd( Change change ){  
		addDocument( (POMAbstractObject)change.getObject(), (AbstractDocument)change.getValue() );
	} 

	protected void executeRemove( Change change ){
		removeDocument( (POMAbstractObject)change.getObject(), (AbstractDocument)change.getValue() );
	}  
	
	private class DocumentVector {
		
		private ArrayList<AbstractDocument> m_documents;
		
		private POMAbstractObject m_object;
	
		public DocumentVector( POMAbstractObject object ){
			m_object = object;
			m_documents = new ArrayList<AbstractDocument>();
		}
		public POMAbstractObject getObject(){
			return m_object;
		}
		public ArrayList<AbstractDocument> getDocuments(){
			return m_documents;
		}
		public void addDocument( AbstractDocument doc ){
			m_documents.add( doc );			
		}
		public void removeDocument( AbstractDocument doc ){
			m_documents.remove( doc );
		}
		public boolean includes( DocumentVector dv ){ 
			for( AbstractDocument doc: dv.getDocuments() ){
				if( !m_documents.contains( doc ) ){
					return false;
				}
			}
			return true;
		} 
	}	
}
