package org.ontoware.text2onto.change;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.ontoware.text2onto.util.MyInteger;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public abstract class Changeable implements AbstractChangeable, Serializable {
	 
	protected ArrayList<Change> m_alChanges;

	protected HashMap m_hmObserver2Index;
 
	protected HashMap m_hmClass2Strategy;
	
	protected ChangeStrategy m_defaultStrategy; 


	protected Changeable(){
		m_alChanges = new ArrayList();
		m_hmObserver2Index = new HashMap();
	}
	
	/* strategies */
	
	public void setChangeStrategy( Class objectClass, ChangeStrategy strategy ){
		m_hmClass2Strategy.put( objectClass, strategy );
	}

	public ChangeStrategy getChangeStrategy( Class objectClass ){
		ChangeStrategy strategy = (ChangeStrategy)m_hmClass2Strategy.get( objectClass );
		if( strategy == null ){
			strategy = (ChangeStrategy)m_defaultStrategy.clone();
		}
		return strategy;
	}
 
	/* observers */

	public void addChangeObserver( ChangeObserver co ){
		if( !m_hmObserver2Index.containsKey( co ) ){ 
			m_hmObserver2Index.put( co, new MyInteger(0) );
		}
	}
	
	public List<Change> getChangesFor( ChangeObserver co ){
		ArrayList<Change> al = new ArrayList<Change>();
		MyInteger myInt = (MyInteger)m_hmObserver2Index.get( co );
		if( myInt != null )
		{ 
			for( int i=myInt.getValue(); i<m_alChanges.size(); i++ ){
				al.add( m_alChanges.get(i) );
			}   
		} 
		return al;
	}

	public boolean hasChangesFor( ChangeObserver co ){
		MyInteger myInt = (MyInteger)m_hmObserver2Index.get( co );
		return myInt.getValue() < m_alChanges.size();
	}

	public void resetChangesFor( ChangeObserver co ){
		MyInteger myInt = (MyInteger)m_hmObserver2Index.get( co );
        if (myInt != null)
            myInt.setValue( m_alChanges.size() );
	} 

	public void resetChanges(){
		m_alChanges = new ArrayList();
		Iterator iter = m_hmObserver2Index.keySet().iterator();
		while( iter.hasNext() )
		{
			ChangeObserver observer = (ChangeObserver)iter.next();
			m_hmObserver2Index.put( observer, new MyInteger(0) );
		}
	}
	
	public List<Change> getChanges( Object object ){
		ArrayList alChanges = new ArrayList();
		Iterator iter = m_alChanges.iterator();
		while( iter.hasNext() )
		{
			Change change = (Change)iter.next();
			if( change.getObject().equals( object ) ){
				alChanges.add( change );
			}
		}
		return alChanges;
	}
	
	public List<Change> getChangesFor( ChangeObserver co, Object object ){
		ArrayList alChanges = new ArrayList();
		Iterator iter = getChangesFor( co ).iterator();
		while( iter.hasNext() )
		{
			Change change = (Change)iter.next();
			if( change.getObject().equals( object ) ){
				alChanges.add( change );
			}
		}
		return alChanges;
	}
	
	public List<Change> getChangesFor( ChangeObserver co, Class c ){
		ArrayList alChanges = new ArrayList();
		Iterator iter = getChangesFor( co ).iterator();
		while( iter.hasNext() )
		{
			Change change = (Change)iter.next();
			Class objClass = change.getObject().getClass();
			if( objClass.equals( c ) ){
				alChanges.add( change );
			}
		}
		return alChanges;
	}
	
	public List<Object> getChangedObjectsFor( ChangeObserver co ){
		ArrayList alObjects = new ArrayList();
		Iterator iter = getChangesFor( co ).iterator();
		while( iter.hasNext() )
		{
			Object object = ((Change)iter.next()).getObject();
			if( !alObjects.contains( object ) ){
				alObjects.add( object );
			}
		}
		return alObjects;
	}
	
	public Change getLastChangeFor( ChangeObserver co, Object object, List<Integer> types ){
		List<Change> changes = getChangesFor( co, object );
		for( int i=changes.size()-1; i>=0; i-- )
		{
			Change change = (Change)changes.get(i);
			if( types.contains( new Integer( change.getType() ) ) ){
				return change;
			}
		}
		return null;
	}
	  
	/* change application */

	private void applyChanges( List<Change> changes ){
		Iterator iter = changes.iterator();
		while( iter.hasNext() )
		{
			Change change = (Change)iter.next();
			applyChange( change );
		}
	}

	private void applyChange( Change change ){ 
		executeChange( change );
		m_alChanges.add( change );
	}
	
	protected void processChangeRequests( List<ChangeRequest> changeRequests ) {
		for( ChangeRequest c: changeRequests )
			processChangeRequest(c);
	}
	
	protected void processChangeRequest( ChangeRequest changeRequest ) {
		Change c = createChange( changeRequest );
		if( c != null ){
			applyChange(c);
		}
	}
		 
	protected abstract Change createChange( ChangeRequest changeRequest );
	
	protected abstract void executeChange( Change change );
}