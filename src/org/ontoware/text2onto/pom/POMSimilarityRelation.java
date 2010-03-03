package org.ontoware.text2onto.pom;
	

public class POMSimilarityRelation extends POMObject implements POMAbstractRelation {
	
	private POMEntity m_domain;
 
	private POMEntity m_range; 

 
	protected POMSimilarityRelation(){
		m_sLabel = "similar-to";
	}
 
	public void setDomain( POMEntity domain ){
		m_domain = domain;
	}

	public void setRange( POMEntity range ){
		m_range = range;
	}  

	public POMEntity getDomain(){
		return m_domain;
	}

	public POMEntity getRange(){
		return m_range;
	}

	public String toString(){
		return m_sLabel +"( "+ m_domain.getLabel() +", "+ m_range.getLabel() +" )"; 
	}

	public Object clone(){
		POMSimilarityRelation rel = new POMSimilarityRelation();
		rel.setDomain( (POMEntity)m_domain.clone() );
		rel.setRange( (POMEntity)m_range.clone() );
		return rel;
	}

	public boolean equals( Object object ){
		return ( object instanceof POMSimilarityRelation ) 
			&& ((POMSimilarityRelation)object).getLabel().equals( m_sLabel )
			&& ((POMSimilarityRelation)object).getDomain().equals( m_domain )
			&& ((POMSimilarityRelation)object).getRange().equals( m_range );
	}

	public int hashCode(){
		return ( getClass().getName() + getLabel() 
			+ m_domain.getLabel() + m_range.getLabel() ).hashCode(); 
	}
}

