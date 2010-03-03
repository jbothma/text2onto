package org.ontoware.text2onto.change;
 
import java.util.List;
import java.util.ArrayList;
 
/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class ReferenceChange extends Change { 
  
	public ReferenceChange( int iType, Object source, Changeable target, Object object, Change cause ){
		this( iType, source, target, object, null, new ArrayList() ); 
		addCause( cause );
	}
	
	public ReferenceChange( int iType, Object source, Changeable target, Object object, Object value, Change cause ){
		this( iType, source, target, object, value, new ArrayList() );
		addCause( cause ); 
	}
	
	public ReferenceChange( int iType, Object source, Changeable target, Object object, List causes ){
		this( iType, source, target, object, null, causes ); 
	}

	public ReferenceChange( int iType, Object source, Changeable target, Object object, Object value, List causes ){
		m_iType = iType;
		m_source = source;
		m_target = target;
		m_target = null;
		m_object = object;
		m_value = value;
		m_causes = new ArrayList( causes ); 
	}
  
	public Object getObject(){
		return m_object;
	}
	 
	public String toString(){
		return "Reference"+ super.toString(); 
	} 
}

