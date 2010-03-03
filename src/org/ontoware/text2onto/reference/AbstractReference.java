package org.ontoware.text2onto.reference;

import java.io.Serializable;

import org.ontoware.text2onto.pom.POMAbstractObject;


public abstract class AbstractReference implements Serializable {

	protected POMAbstractObject m_object;
	
	protected Object m_source;
	
	protected Long m_timestamp;
	 
	
	public POMAbstractObject getObject(){
		return m_object;
	}
	 
	public Object getSource(){
		return m_source;
	}
	
	public Long getTimestamp(){
		return m_timestamp;
	}
	
	public boolean equals( Object object ){
		if( !object.getClass().equals( this.getClass() ) ){
			return false;
		}
		AbstractReference reference = (AbstractReference)object;
		return reference.getObject() == getObject() && reference.getSource() == getSource();
		// TODO ...
//        if (reference.getObject() != null && reference.getSource() != null)
//            return ( reference.getObject().equals( getObject() )
//                    && reference.getSource().equals( getSource() ) );
//        else if (reference.getObject() != null && reference.getSource() == null)
//       		return reference.getObject().equals(getObject());
//        else if (reference.getObject() == null && reference.getSource() != null)
//    		return reference.getSource().equals(getSource());
//        else
//            return false;
	}
}

