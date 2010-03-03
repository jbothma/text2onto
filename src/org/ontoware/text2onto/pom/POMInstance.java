package org.ontoware.text2onto.pom;


public class POMInstance extends POMEntity implements POMSingleEntity { 
 
	protected POMInstance( String sLabel ){
		m_sLabel = sLabel;
	}
 
	public Object clone(){
		return new POMInstance( m_sLabel );
	}

	public boolean equals( Object object ){ 
		return ( object instanceof POMInstance ) 
			&& ((POMInstance)object).getLabel().equals( m_sLabel ); 
	} 
	
	public int hashCode(){
		return ( getClass().getName() + getLabel() ).hashCode();
	}
	
	public String toString(){
		return getLabel();
	}
}