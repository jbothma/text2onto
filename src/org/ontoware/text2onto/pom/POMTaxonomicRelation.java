package org.ontoware.text2onto.pom;
	

public interface POMTaxonomicRelation extends POMAbstractRelation {
  
	public POMEntity getDomain();
 
	public POMConcept getRange();
}

