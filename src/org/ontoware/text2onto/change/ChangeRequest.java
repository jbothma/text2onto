package org.ontoware.text2onto.change;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;


public class ChangeRequest implements Serializable {

	private Change m_change;
	
	private List<ChangeRequest> m_causes;
	
	private Long m_timestamp;
	

	public ChangeRequest( Change change ) {
		this( change, new ArrayList<ChangeRequest>() );
	}
	
	public ChangeRequest( Change change, List<ChangeRequest> causes ){
		m_change = change;
		if( causes != null ){
			m_causes = causes;
		}
		m_timestamp = System.currentTimeMillis();
	}
	
	public ChangeRequest( Change change, ChangeRequest cause ){
		this( change, new ArrayList<ChangeRequest>() );
		if( cause != null ){
			m_causes.add( cause );
		}
	}

	public Changeable getTarget() {
		return m_change.getTarget();
	}

	public void setTarget( Changeable target ) {
		m_change.setTarget( target );
	}

	public int getType() {
		return m_change.getType();
	}

	public Object getSource() {
		return m_change.getSource();
	}

	public List<Change> getCauses() {
		return m_change.getCauses();
	}
	
	public Object getObject() {
		return m_change.getObject();
	}

	public Object getValue() {
		return m_change.getValue();
	}

	public String historyToString() {
		return m_change.historyToString();
	}
	
	public List<ChangeRequest> getRequestCauses() {
		return m_causes;
	}

	public String requestHistoryToString() {
		String s = this.toString();
		for( ChangeRequest cause: m_causes )
		{
			s += " <- [ "+ cause.requestHistoryToString() +" ]"; 
		} 
		return s;
	}

	public Change createChangeWithType( int iType ) {
		m_change.setType( iType ); 
		return m_change; 
	}
    
	public String toString(){
		return "ChangeRequest( change="+ m_change +", causes="+ m_causes +" )";
	}
}
