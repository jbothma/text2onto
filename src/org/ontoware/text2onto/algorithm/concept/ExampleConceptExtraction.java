package org.ontoware.text2onto.algorithm.concept;

import java.util.List;
 
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.change.ChangeRequest; 
import org.ontoware.text2onto.pom.POMObject; 

/**
 */
public class ExampleConceptExtraction extends AbstractConceptExtraction {

	protected List<ChangeRequest> normalize( List<ChangeRequest> list ){ 
		return list;
	}
	
	protected void prepareNormalization( Double prob ){
		// TODO
	}
	 
	protected double getProbability( POMObject object, int iReferences ){
		int iFreq = (getEvidenceStore( ReferenceStore.class )).getReferences( object ).size();
		return new Double( (double)iFreq / Math.max( 1.0, (double)iReferences ) ); 
	}	
}
