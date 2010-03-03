package org.ontoware.text2onto.change;
 
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class Change implements Serializable { 
 
	/** source of this event */
	protected Object m_source;
	
	/** target of this event */
	protected Changeable m_target;
 
	/** target object */
	protected Object m_object;

	/** changed value */
	protected Object m_value;

	/** causes of this change */
	protected ArrayList<Change> m_causes;
	
	/** type of this change */
	protected int m_iType = Type.UNKNOWN;
	
	
	protected Change(){
		m_causes = new ArrayList<Change>();
	}
	 
	public Object getSource(){
		return m_source;
	}
	
	public Changeable getTarget(){
		return m_target;
	}
	
	public void setTarget( Changeable target ){
		m_target = target;
	}
	
	public void addCause( Change cause ){
		if( cause != null ){
			m_causes.add( cause );
		}
	}

	public List getCauses(){
		return m_causes;
	}

	public Object getObject(){
		return m_object;
	}
	
	public Object getValue(){
		return m_value;
	} 
 
	public String historyToString(){
		String s = this.toString();
		for( Change cause: m_causes )
		{
			s += " <- [ "+ cause.historyToString() +" ]"; 
		} 
		return s;
	}
	
	protected void setType( int iType ){
		m_iType = iType;
	}
	
	public int getType(){
		return m_iType;
	}
 
	public boolean equals( Object object ){ 
		return ( ( object instanceof Change )
			&& ((Change)object).getType() == m_iType
			&& ((Change)object).getObject().equals( m_object )
			&& ((Change)object).getSource().equals( m_source )
			&& ((Change)object).getTarget().equals( m_target )
			&& ( m_value == null || m_value.equals( ((Change)object).getValue() ) ) );
	}
	
	public int hashCode(){
		return ( getClass().getName() + getSource() + getTarget() + getType() + getObject() + getValue() ).hashCode();
	}

	public String toString(){
		String s = "Change( type="+ Type.toString( m_iType );
		s += ", source=";
		if( m_source != null ){ 
			s += m_source.getClass().getSimpleName();
		}
		else {
			s += "?";
		}
		s += ", target=";
		if( m_target != null ){
			s += m_target.getClass().getSimpleName();
		}
		else {
			s += "?";
		}
		s += ", object="+ m_object;
		s += ", value="+ m_value +" )"; 
		return s;
	}
	
	public static class Type {	
		public final static int UNKNOWN = -1; 
		public final static int ADD = 0;
		public final static int REMOVE = 1;
		public final static int MODIFY = 2;
 
		public static String toString( int iType ){
			switch( iType ){
				case ADD: return "ADD";
				case REMOVE: return "REMOVE";
				case MODIFY: return "MODIFY";
				default: return "UNKNOWN";
			}
		}
	}
}

