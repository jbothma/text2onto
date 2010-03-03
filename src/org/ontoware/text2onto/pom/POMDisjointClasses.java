package org.ontoware.text2onto.pom;

import java.util.Set;
import java.util.HashSet; 


public class POMDisjointClasses extends POMObject {

	private Set<POMConcept> m_concepts;
	

	protected POMDisjointClasses(){
		this( new HashSet<POMConcept>() );
	}
	
	protected POMDisjointClasses( Set<POMConcept> concepts ){
		m_sLabel = "disjoint";
		m_concepts = concepts;
	}

	public void addConcept( POMConcept concept ){
		m_concepts.add( concept );
	}
	
	public Set<POMConcept> getConcepts(){
		return m_concepts;
	}
	
	public String toString(){
		return m_sLabel + m_concepts; 
	}

	public Object clone(){
		POMDisjointClasses disjoint = new POMDisjointClasses();
		for( POMConcept concept: m_concepts ){
			disjoint.addConcept( (POMConcept)concept.clone() );
		} 
		return disjoint;
	}
	
	public boolean equals( Object object ){ 
		if( !( object instanceof POMDisjointClasses ) ){
			return false;
		}
		POMDisjointClasses other = (POMDisjointClasses)object;
		if( !( other.getConcepts().containsAll( m_concepts ) 
			&& m_concepts.containsAll( other.getConcepts() ) ) )
		{
			return false;
		}
		return true;
	}

	public int hashCode(){
		return ( getClass().getName() + getLabel() + getConcepts() ).hashCode(); 
	}
}