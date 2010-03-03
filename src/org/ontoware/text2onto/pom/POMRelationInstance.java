package org.ontoware.text2onto.pom;
	

public class POMRelationInstance extends POMObject implements POMAbstractRelation {
 
	protected POMInstance m_domain;
 
	protected POMInstance m_range;
 
	
	protected POMRelationInstance(){
		m_sLabel = "related-to";
	}

	public POMRelationInstance( String sLabel ){
		m_sLabel = sLabel;
	}
 
	public void setDomain( POMInstance domain ){
		m_domain = domain;
	}

	public void setRange( POMInstance range ){
		m_range = range;
	}  

	public POMInstance getDomain(){
		return m_domain;
	}

	public POMInstance getRange(){
		return m_range;
	}
 
	public String toString(){
		return m_sLabel +"( "+ m_domain.getLabel() +", "+ m_range.getLabel() +" )";  
	}

	public Object clone(){
		POMRelationInstance rel = new POMRelationInstance();
		rel.setDomain( (POMInstance)m_domain.clone() );
		rel.setRange( (POMInstance)m_range.clone() );
		return rel;
	}

	public boolean equals( Object object ){
		return ( object instanceof POMRelationInstance ) 
			&& ((POMRelationInstance)object).getLabel().equals( m_sLabel )
			&& ((POMRelationInstance)object).getDomain().equals( m_domain )
			&& ((POMRelationInstance)object).getRange().equals( m_range );
	}

	public int hashCode(){
		return ( getClass().getName() + getLabel() 
			+ m_domain.getLabel() + m_range.getLabel() ).hashCode(); 
	}
}

