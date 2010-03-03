package org.ontoware.text2onto.pom;


public class POMConcept extends POMEntity implements POMSingleEntity { 

	protected POMConcept( String sLabel ){
		m_sLabel = sLabel;
	}

	public Object clone(){
		return new POMConcept( m_sLabel );
	}

	public boolean equals( Object object ){ 
		return ( object instanceof POMConcept ) 
			&& ((POMConcept)object).getLabel().equals( m_sLabel );
	} 
	
	public int hashCode(){
		return ( getClass().getName() + getLabel() ).hashCode();
	}
	
	public String toString(){
		return getLabel();
	}
}