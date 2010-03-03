package org.ontoware.text2onto.change;

import java.util.List; 
 
/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public interface AbstractChangeable {
	 
	/* strategies */

	public void setChangeStrategy( Class objectClass, ChangeStrategy strategy );

	public ChangeStrategy getChangeStrategy( Class objectClass );
 
	/* observers */

	public void addChangeObserver( ChangeObserver co );
	
	public List getChangesFor( ChangeObserver co );

	public boolean hasChangesFor( ChangeObserver co );

	public void resetChangesFor( ChangeObserver co );

	public void resetChanges();
	
	public List getChanges( Object object );
	
	public List<Change> getChangesFor( ChangeObserver co, Object object );
	 
	public List<Object> getChangedObjectsFor( ChangeObserver co );
	 
	/* change application */
  
	// protected abstract Change createChange( ChangeRequest changeRequest );
	
	// protected abstract void executeChange( Change change );
}