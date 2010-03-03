package org.ontoware.text2onto.explanation.context;

import org.ontoware.text2onto.evidence.context.ContextVector;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.pom.POMAbstractObject;


public class ContextRelationExplanation extends AbstractExplanation {

	private ContextVector m_domainContext;
	
	private ContextVector m_rangeContext;
	
	
	public ContextRelationExplanation( POMAbstractObject object, Object source ){
		m_object = object;
		m_source = source;
		m_timestamp = System.currentTimeMillis();
	}

	public void setDomainContext( ContextVector domainContext ){
		m_domainContext = domainContext;
	}
	
	public void setRangeContext( ContextVector rangeContext ){
		m_rangeContext = rangeContext;
	}
	
	public ContextVector getDomainContext(){
		return m_domainContext;
	}
	
	public ContextVector getRangeContext(){
		return m_rangeContext;
	}
	
	public boolean equals( Object object ){
		if( !( object instanceof ContextRelationExplanation )
			|| !super.equals( object ) )
		{
			return false;
		}
		ContextRelationExplanation Explanation = (ContextRelationExplanation)object;
		return ( super.equals( object )
			&& Explanation.getDomainContext().equals( getDomainContext() )
			&& Explanation.getRangeContext().equals( getRangeContext() ) );
	} 
}