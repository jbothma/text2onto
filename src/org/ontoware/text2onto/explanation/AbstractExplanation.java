package org.ontoware.text2onto.explanation;
 
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.pom.POMAbstractObject;

 
public abstract class AbstractExplanation {

	protected POMAbstractObject m_object;

	protected double m_dConfidence;
	
	protected double m_dRelevance;
	
	protected Object m_source;
	
	protected Long m_timestamp;
	
	protected POMChange m_change;
	 
	
	public double getConfidence(){
		return m_dConfidence;
	}
	
	public void setConfidence( double dConfidence ){
		m_dConfidence = dConfidence;
	}
	
	public double getRelevance(){
		return m_dRelevance;
	}
	
	public void setRelevance( double dRelevance ){
		m_dRelevance = dRelevance;
	}
	
	public POMAbstractObject getObject(){
		return m_object;
	}
	
	public Object getSource(){
		return m_source;
	}
	
	public Long getTimestamp(){
		return m_timestamp;
	}
	
	public POMChange getChange(){
		return m_change;
	}
	
	public boolean equals( Object object ){
		if( !object.getClass().equals( this.getClass() ) ){
			return false;
		}
		AbstractExplanation evidence = (AbstractExplanation)object;
		return ( evidence.getObject().equals( getObject() )
			&& evidence.getSource().equals( getSource() ) );
	}
	
	public String getText(){
		return this.toString();
	} 
}

