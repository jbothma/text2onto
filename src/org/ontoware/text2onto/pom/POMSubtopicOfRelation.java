package org.ontoware.text2onto.pom;

public class POMSubtopicOfRelation extends POMObject implements POMAbstractRelation {
	
	private POMConcept m_domain;
	 
	private POMConcept m_range; 

 
	protected POMSubtopicOfRelation(){
		m_sLabel = "subtopic-of";
	}
 
	public void setDomain( POMConcept domain ){
		m_domain = domain;
	}

	public void setRange( POMConcept range ){
		m_range = range;
	}  

	public POMConcept getDomain(){
		return m_domain;
	}

	public POMConcept getRange(){
		return m_range;
	}

	public String toString(){
		return m_sLabel +"( "+ m_domain.getLabel() +", "+ m_range.getLabel() +" )"; 
	}

	public Object clone(){
		POMSubtopicOfRelation rel = new POMSubtopicOfRelation();
		rel.setDomain( (POMConcept)m_domain.clone() );
		rel.setRange( (POMConcept)m_range.clone() );
		return rel;
	}

	public boolean equals( Object object ){
		return ( object instanceof POMSimilarityRelation ) 
			&& ((POMSubtopicOfRelation)object).getLabel().equals( m_sLabel )
			&& ((POMSubtopicOfRelation)object).getDomain().equals( m_domain )
			&& ((POMSubtopicOfRelation)object).getRange().equals( m_range );
	}

	public int hashCode(){
		return ( getClass().getName() + getLabel() 
			+ m_domain.getLabel() + m_range.getLabel() ).hashCode(); 
	}
}
