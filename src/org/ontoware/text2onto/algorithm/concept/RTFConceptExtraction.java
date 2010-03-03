package org.ontoware.text2onto.algorithm.concept;

import java.util.List;
import java.lang.Math;
 
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.change.ChangeRequest; 
import org.ontoware.text2onto.pom.POMObject; 


/* 
 * @author Matthias Hartung (hartung@urz.uni-heidelberg.de)
 */
public class RTFConceptExtraction extends AbstractConceptExtraction {
 
	protected static Double lg( double x ){
		return new Double( Math.log( x ) / Math.log( 10 ) );	// log_10
	}

	protected double getProbability( POMObject object, int iReferences ){
		// iReferences: total number of concept candidates within document collection
		// docReferences: occurences of concept candidate with whole collection of documents

		ReferenceStore refStore = getEvidenceStore( ReferenceStore.class );
		List lDocReferences = refStore.getReferences(object);
		return (double)lDocReferences.size() / (double)iReferences; 
	}

	protected List<ChangeRequest> normalize( List<ChangeRequest> list ){
		// RTF is already normalized !
		return list;
	}

	protected void prepareNormalization( Double dProb ){
		// TODO
	} 
}

