package org.ontoware.text2onto.reference.document;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.ontoware.text2onto.pom.POMAbstractObject; 
import org.ontoware.text2onto.reference.AbstractReferenceStore; 
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.corpus.AbstractDocument; 
import org.ontoware.text2onto.change.Change;


public class DocumentReferenceStore extends AbstractReferenceStore {
 
	private HashMap m_hmObj2References; 

	private HashMap m_hmObj2Docs;

   private HashMap m_hmDoc2Objects;


	public DocumentReferenceStore(){ 
		m_hmObj2References = new HashMap();  
		m_hmObj2Docs = new HashMap();
		m_hmDoc2Objects = new HashMap();
	}
	
	protected void addReference( POMAbstractObject object, DocumentReference reference ){
		// m_hmObj2References
		ArrayList alReferences = (ArrayList)m_hmObj2References.get( object );
		if( alReferences == null ){
			alReferences = new ArrayList();
			m_hmObj2References.put( object, alReferences );
		}
		if( !alReferences.contains( reference ) ){
			alReferences.add( reference );
		}
		// m_hmObj2Docs
		AbstractDocument doc = reference.getDocument();
		ArrayList alDocs = (ArrayList)m_hmObj2Docs.get( object );
		if( alDocs == null ){
			alDocs = new ArrayList();
			m_hmObj2Docs.put( object, alDocs );
		}
		if( !alDocs.contains( doc ) ){
			alDocs.add( doc );
		}		
		// m_hmDoc2Objects
		ArrayList alObjects = (ArrayList)m_hmDoc2Objects.get( doc );
		if( alObjects == null ){
			alObjects = new ArrayList();
			m_hmDoc2Objects.put( doc, alObjects );
		}
		if( !alObjects.contains( object ) ){
			alObjects.add( object );
		}
	}

	protected void removeReference( POMAbstractObject object, DocumentReference reference ){ 
		// m_hmObj2References
		ArrayList alReferences = (ArrayList)m_hmObj2References.get( object );
		if( alReferences.contains( reference ) ){
			alReferences.remove( reference );  
		} 		
 		// m_hmObj2Docs
 		AbstractDocument doc = reference.getDocument();
 		// if is this the object's only reference to doc : remove doc
 		// reconstruct m_hmObj2Docs based on the updated information in m_hmObj2References
		ArrayList alDocs = new ArrayList();
		Iterator referenceIter = alReferences.iterator();
		while( referenceIter.hasNext() )
		{
			DocumentReference p = (DocumentReference)referenceIter.next();
			AbstractDocument d = p.getDocument();
			if( !alDocs.contains( d ) ){
				alDocs.add( d );
			}
		}
		m_hmObj2Docs.put( object, alDocs );
		if( alReferences.size() == 0 )
		{
		    m_hmObj2References.remove( object );
		}
		// m_hmDoc2Objects
		ArrayList alObjects = (ArrayList)m_hmDoc2Objects.get( doc );
		if( alObjects == null ){
			alObjects = new ArrayList();
			m_hmDoc2Objects.put( doc, alObjects );
		} 
		if( alObjects.contains( object ) ){
			alObjects.remove( object );
		}
	}

	protected void addReferences( POMAbstractObject object, List references ){
		Iterator iter = references.iterator();
		while( iter.hasNext() ){
			DocumentReference reference = (DocumentReference)iter.next();
			addReference( object, reference );
		}
	}

	protected void removeReferences( POMAbstractObject object, List references ){
		Iterator iter = references.iterator();
		while( iter.hasNext() ){
			DocumentReference reference = (DocumentReference)iter.next();
			removeReference( object, reference );
		}
	}
 
	public List getReferences( POMAbstractObject object ){
		List references = (List)m_hmObj2References.get( object );
		if( references == null ){
			references = new ArrayList();
		}
		return references;
	} 
	
	public List getReferences( AbstractDocument doc ){
		List al = new ArrayList();
		List objects = getObjects( doc );
		Iterator objIter = objects.iterator();
		while( objIter.hasNext() )
		{
			POMAbstractObject object = (POMAbstractObject)objIter.next();
			List references = getReferences( object );
			Iterator refIter = references.iterator();
			while( refIter.hasNext() )
			{
				DocumentReference reference = (DocumentReference)refIter.next();
				if( reference.getDocument().equals( doc ) ){
					al.add( reference );
				}
			}
		}
		return al;
	}

	public List getReferences( POMAbstractObject object, AbstractDocument doc ){
		ArrayList al = new ArrayList();
		List references = getReferences( object );
		Iterator iter = references.iterator();
		while( iter.hasNext() )
		{
			DocumentReference reference = (DocumentReference)iter.next();
			AbstractDocument document = reference.getDocument();
			if( document.equals( doc ) ){
				al.add( reference );
			}
		}
		return al;
	}

	public List getObjects(){
		ArrayList al = new ArrayList();
		Iterator iter = m_hmObj2References.keySet().iterator();
		while( iter.hasNext() ){
			al.add( (POMAbstractObject)iter.next() );
		}
		return al;
	}
	
	public List getDocuments(){
		ArrayList al = new ArrayList();
		Iterator iter = m_hmDoc2Objects.keySet().iterator();
		while( iter.hasNext() ){
			al.add( (AbstractDocument)iter.next() );
		}
		return al;
	}
 
	public List getDocuments( POMAbstractObject object ){
		List docs = (List)m_hmObj2Docs.get( object );
		if( docs == null ){
			docs = new ArrayList();
		}
		return docs;
	}
	
	public List getObjects( AbstractDocument doc ){
		List objects = (List)m_hmDoc2Objects.get( doc );
		if( objects == null ){
			objects = new ArrayList();
		}
		return objects;
	}

	public boolean contains( POMAbstractObject object ){
		return getReferences( object ).size() > 0;
	}

	public String toString(){ 
		int countAllConcepts = 0;
		String s = "[\n";
		Iterator iter = m_hmObj2References.keySet().iterator();
		while( iter.hasNext() )
		{
			POMAbstractObject object = (POMAbstractObject)iter.next();
			List references = getReferences( object );
			countAllConcepts += references.size();
			s += object.getLabel() + ": # = " + references.size() + "; Refs = " + references;
			if( iter.hasNext() )
			{
				s += ",\n";
			}
		} 
		s += "\n]\nall concepts: # = " + countAllConcepts;
		return s;
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
		addReference( (POMAbstractObject)change.getObject(), (DocumentReference)change.getValue() );
	} 

	protected void executeRemove( Change change ){
		removeReference( (POMAbstractObject)change.getObject(), (DocumentReference)change.getValue() );
	}  
}