package org.ontoware.text2onto.evidence.context;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.change.Change; 
import org.ontoware.text2onto.evidence.AbstractEvidenceStore;


public class ContextVectorStore extends AbstractEvidenceStore { 

	private HashMap m_hmObj2Vector; 


	public ContextVectorStore(){
		m_alChanges = new ArrayList();
		m_hmObserver2Index = new HashMap();
		m_hmObj2Vector = new HashMap();
	}

	protected void addContextVector( POMObject object, ContextVector cv ){
		m_hmObj2Vector.put( object, cv ); 
	}

	protected void removeContextVector( POMObject object ){
		m_hmObj2Vector.remove( object );
	}

	public ContextVector getContextVector( POMObject object ){
		return (ContextVector)m_hmObj2Vector.get( object ); 
	}

	public List getContextVectors(){
		ArrayList al = new ArrayList();
		Iterator iter = m_hmObj2Vector.values().iterator();
		while( iter.hasNext() ){
			al.add( iter.next() );
		}
		return al;
	}
   
	public String toString(){
		String s = "[\n";
		Iterator iter = m_hmObj2Vector.keySet().iterator();
		while( iter.hasNext() )
		{
			POMObject object = (POMObject)iter.next();
			ContextVector cv = getContextVector( object );
			s += object.getLabel() +" = "+ cv;
			if( iter.hasNext() )
			{
				s += ",\n";
			}
		} 
		s += "\n]";
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
			case Change.Type.MODIFY:
				executeModify( change );
				break;
		}
	}

	protected void executeAdd( Change change ){  
		addContextVector( (POMObject)change.getObject(), (ContextVector)change.getValue() );
	} 

	protected void executeRemove( Change change ){
		removeContextVector( (POMObject)change.getObject() );
	}
 
	protected void executeModify( Change change ){ 
		addContextVector( (POMObject)change.getObject(), (ContextVector)change.getValue() ); 
	} 
}