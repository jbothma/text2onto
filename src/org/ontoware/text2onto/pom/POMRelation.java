package org.ontoware.text2onto.pom;
	
import java.util.List;
import java.util.ArrayList;


public class POMRelation extends POMEntity implements POMAbstractRelation {

	private POMConcept m_domain;
 
	// private POMConcept m_range;
	
	private ArrayList m_ranges;
 

	protected POMRelation(){
		this( "related-to" );
	}
 
	protected POMRelation( String sLabel ){
		m_sLabel = sLabel;
		m_ranges = new ArrayList();
	}
	
	public int getArity(){
		return m_ranges.size() + 1;
	}
	
	public void setLabel( String sLabel ){
		m_sLabel = sLabel;	
	}
 
	public void setDomain( POMConcept domain ){
		m_domain = domain;
	}

	public void setRange( POMConcept range ){
		m_ranges.add( range );
	}  

	public POMConcept getDomain(){
		return m_domain;
	}

	private POMConcept getRange( int iIndex ){
		if( m_ranges.size() > iIndex ){
			return (POMConcept)m_ranges.get( iIndex );
		}
		return null;
	}
	
	public POMConcept getRange(){
		return getRange(0);
	}
 
	public String toString(){
		return m_sLabel +"( "+ getDomain() +", "+ getRange() +" )"; 
	}

	public Object clone(){
		POMRelation rel = new POMRelation();
		rel.setDomain( (POMConcept)getDomain().clone() );
		rel.setRange( (POMConcept)getRange().clone() );
		return rel;
	}
	
	public boolean equals( Object object ){
		return ( object instanceof POMRelation ) 
			&& ((POMRelation)object).getLabel().equals( getLabel() )
			&& ((POMRelation)object).getDomain().equals( getDomain() )
			&& ((POMRelation)object).getRange().equals( getRange() );
	}

	public int hashCode(){
		return ( getClass().getName() + getLabel() + getDomain() + getRange() ).hashCode(); 
	}
}

