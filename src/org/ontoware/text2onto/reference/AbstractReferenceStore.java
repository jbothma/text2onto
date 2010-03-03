package org.ontoware.text2onto.reference;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.Changeable;


public abstract class AbstractReferenceStore extends Changeable {
 
	private HashMap m_hmObject2Source2Reference;
	
	
	protected AbstractReferenceStore() { 
		m_hmObject2Source2Reference = new HashMap();
	}
    
	protected void processChangeRequests( List<ChangeRequest> changeRequests ) {
		super.processChangeRequests( changeRequests );
	}

	protected void processChangeRequest( ChangeRequest changeRequest ) {
		super.processChangeRequest( changeRequest );
	}
	
	protected Change createChange( ChangeRequest changeRequest ) {
		// TODO dummy
		return changeRequest.createChangeWithType( changeRequest.getType() );
	}
	 
	public List getReferences( POMObject object ){
		HashMap hmSource2Reference = (HashMap)m_hmObject2Source2Reference.get( object );
		if( hmSource2Reference == null ){
			return new ArrayList();
		}
		ArrayList al = new ArrayList();
		Iterator iter = hmSource2Reference.values().iterator();
		while( iter.hasNext() ){ 
			al.add( (AbstractReference)iter.next() );
		}
		return al;
	}
	
	/* public AbstractReference getReferences( POMObject object, Object source ){
		HashMap hmSource2Reference = (HashMap)m_hmObject2Source2Reference.get( object );
		if( hmSource2Reference == null ){
			return null;
		}
		return (AbstractReference)hmSource2Reference.get( source );
	} */
	
	private void addReference( POMObject object, AbstractReference reference ){
		Object source = reference.getSource();
		HashMap hmSource2Reference = (HashMap)m_hmObject2Source2Reference.get( object );
		if( hmSource2Reference == null )
		{
			hmSource2Reference = new HashMap();
			m_hmObject2Source2Reference.put( object, hmSource2Reference );
		}
		hmSource2Reference.put( source, reference );
	} 
}

