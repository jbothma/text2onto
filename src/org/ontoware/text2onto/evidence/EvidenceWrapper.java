package org.ontoware.text2onto.evidence;

import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractAlgorithm;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.ChangeableWrapper;

/**
 * @author Günter Ladwig
 */
public class EvidenceWrapper implements ChangeableWrapper {

	private AbstractEvidenceStore m_store;

	private AbstractAlgorithm m_owner;

	private int m_id;

	private String m_name;

	protected EvidenceWrapper( AbstractEvidenceStore store, AbstractAlgorithm owner, int id ) {
		this( store, owner, id, null );
	}

	protected EvidenceWrapper( AbstractEvidenceStore store, AbstractAlgorithm owner, int id, String name ) {
		m_store = store;
		m_owner = owner;
		m_id = id;
		m_name = name;
	}

	public AbstractEvidenceStore getChangeable() {
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
		m_store.processChangeRequest( changeRequest );
	}

	public void processChangeRequests( List<ChangeRequest> changeRequests ) {
		m_store.processChangeRequests( changeRequests );
	}

	public boolean equals( Object o ) {
		EvidenceWrapper ew = (EvidenceWrapper) o;

		return ew.getChangeable() == getChangeable() && ew.getId() == getId()
				&& ew.getOwner() == getOwner();
	}

	public String toString() {
		return "EvidenceWrapper( id=" + getId() + ", owner=" + getOwner()
				+ ", store=" + getChangeable().getClass().getSimpleName()
				+ " )";
	}
}
