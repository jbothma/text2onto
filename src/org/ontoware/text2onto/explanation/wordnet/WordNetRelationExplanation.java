package org.ontoware.text2onto.explanation.wordnet;

import java.util.List;

import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.evidence.context.ContextVector;
import org.ontoware.text2onto.pom.POMAbstractObject;


public class WordNetRelationExplanation extends AbstractExplanation {

	// context vectors of synsets where (according to WordNet) one is related to the other 
	
	private ContextVector m_domainContext;
	
	private ContextVector m_rangeContext;
	

	public WordNetRelationExplanation( POMAbstractObject object, Object source ){
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
		if( !( object instanceof WordNetRelationExplanation )
			|| !super.equals( object ) )
		{
			return false;
		}
		WordNetRelationExplanation explanation = (WordNetRelationExplanation)object;
		return ( explanation.getDomainContext().equals( getDomainContext() )
			&& explanation.getRangeContext().equals( getRangeContext() ) );
	}
}

