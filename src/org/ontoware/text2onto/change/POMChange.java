package org.ontoware.text2onto.change;
 
import java.util.List;
import java.util.ArrayList;

import org.ontoware.text2onto.pom.POMAbstractObject;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class POMChange extends Change { 
   
	public POMChange( int iType, Object source, POMAbstractObject object, Change cause ){
		this( iType, source, object, null, new ArrayList() ); 
		addCause( cause );
	}
	
	public POMChange( int iType, Object source, POMAbstractObject object, Object value, Change cause ){
		this( iType, source, object, value, new ArrayList() );
		addCause( cause ); 
	}
	
	public POMChange( int iType, Object source, POMAbstractObject object, List causes ){
		this( iType, source, object, null, causes ); 
	}

	public POMChange( int iType, Object source, POMAbstractObject object, Object value, List causes ){
		m_iType = iType;
		m_source = source;
		m_target = null;
		m_object = object;
		m_value = value;
		m_causes = new ArrayList( causes ); 
	}
  
	public POMAbstractObject getPOMAbstractObject(){
		return (POMAbstractObject)m_object;
	}
	 
	public String toString(){ 
		return "POM"+ super.toString();
	} 
}

