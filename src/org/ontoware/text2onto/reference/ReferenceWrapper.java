package org.ontoware.text2onto.reference;

import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractAlgorithm;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.ChangeableWrapper;

/**
 * @author Günter Ladwig
 */
public class ReferenceWrapper implements ChangeableWrapper {

    private AbstractReferenceStore m_store;
    private AbstractAlgorithm m_owner;
    private int m_id;
    private String m_name;

    protected ReferenceWrapper( AbstractReferenceStore store, AbstractAlgorithm owner, int id ) {
        m_store = store;
        m_owner = owner;
        m_id = id;
    }

    public AbstractReferenceStore getChangeable() {
        return m_store;
    }

	public int getId() {
		return m_id;
	}

	public String getName() {
		return m_name;
	}

	public void setName( String name ) {
		m_name = name;
	}

	public AbstractAlgorithm getOwner() {
		return m_owner;
	}

    public void processChangeRequest( ChangeRequest changeRequest ) {
        m_store.processChangeRequest(changeRequest);
    }
    
    public void processChangeRequests( List<ChangeRequest> changeRequests ) {
        m_store.processChangeRequests(changeRequests);
    }
    
    public boolean equals( Object o ) {
    	ReferenceWrapper wrapper = (ReferenceWrapper)o;
    	
    	return wrapper.getChangeable() == getChangeable() && wrapper.getId() == getId()
    		&& wrapper.getOwner() == getOwner();
    }
    
    public String toString() {
    	return "ReferenceWrapper( id=" + getId() + ", owner=" + getOwner() 
    		+ ", store=" + getChangeable().getClass().getSimpleName() + " )";
    }
}
