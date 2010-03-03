package org.ontoware.text2onto.pom;

public class POMInstanceOfRelation extends POMObject implements POMTaxonomicRelation { 

	private POMInstance m_domain;
	
	private POMConcept m_range;


	protected POMInstanceOfRelation(){
		m_sLabel = "instance-of";
	}

	public void setDomain( POMInstance domain ){
		m_domain = domain;
	}

	public POMInstance getDomain(){
		return m_domain;
	}
 
	public void setRange( POMConcept range ){
		m_range = range;
	} 
	
	public POMConcept getRange(){
		return m_range;
	}
 
	public String toString(){
		return m_sLabel +"( "+ m_domain.getLabel() +", "+ m_range.getLabel() +" )"; 
	}

	public Object clone(){
		POMInstanceOfRelation rel = new POMInstanceOfRelation();
		rel.setDomain( (POMInstance)m_domain.clone() );
		rel.setRange( (POMConcept)m_range.clone() );
		return rel;
	}

	public boolean equals( Object object ){
		return ( object instanceof POMInstanceOfRelation ) 
			&& ((POMInstanceOfRelation)object).getLabel().equals( m_sLabel )
			&& ((POMInstanceOfRelation)object).getDomain().equals( m_domain )
			&& ((POMInstanceOfRelation)object).getRange().equals( m_range );
	}

	public int hashCode(){
		return ( getClass().getName() + getLabel() 
			+ m_domain.getLabel() + m_range.getLabel() ).hashCode(); 
	}
}

