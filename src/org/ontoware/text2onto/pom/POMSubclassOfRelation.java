package org.ontoware.text2onto.pom;

public class POMSubclassOfRelation extends POMObject implements POMTaxonomicRelation { 

	private POMConcept m_domain;
	
	private POMConcept m_range;


	protected POMSubclassOfRelation(){
		m_sLabel = "subclass-of";
	}
 
	public void setDomain( POMConcept domain ){
		m_domain = domain;
	}

	public POMConcept getDomain(){
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
		POMSubclassOfRelation rel = new POMSubclassOfRelation();
		rel.setDomain( (POMConcept)m_domain.clone() );
		rel.setRange( (POMConcept)m_range.clone() );
		return rel;
	}

	public boolean equals( Object object ){
		return ( object instanceof POMSubclassOfRelation ) 
			&& ((POMSubclassOfRelation)object).getLabel().equals( m_sLabel )
			&& ((POMSubclassOfRelation)object).getDomain().equals( m_domain )
			&& ((POMSubclassOfRelation)object).getRange().equals( m_range );
	}

	public int hashCode(){
		return ( getClass().getName() + getLabel() 
			+ m_domain.getLabel() + m_range.getLabel() ).hashCode(); 
	}
}

