package org.ontoware.text2onto.algorithm.concept;

import java.util.List;
import java.lang.Math;
 
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.change.ChangeRequest; 
import org.ontoware.text2onto.pom.POMObject; 


/* 
 * @author Matthias Hartung (hartung@urz.uni-heidelberg.de)
 */
public class EntropyConceptExtraction extends AbstractConceptExtraction {

	protected double m_dMinProb = 0.0;
	
	protected double m_dMaxProb = -(Double.MAX_VALUE);

 
	protected static double lg( double x ){
		return ( Math.log( x ) / Math.log( 10 ) );		// log_10
	}

	protected double getProbability( POMObject object, int iReferences ){
		// docReferences:	occurences of certain concept candidates within collection of documents
		// iReferences:	total number of concept candidates within collection of documents

		ReferenceStore refStore = getEvidenceStore( ReferenceStore.class );
		List lDocReferences = refStore.getReferences(object);
		double prob = ((double)lDocReferences.size()/(double)iReferences) * lg( (double)lDocReferences.size() / (double)iReferences );
        System.out.println("prob: " + prob + ", size: " + lDocReferences.size());
        return prob;
	}
 
	protected void prepareNormalization( Double dProb ){
		if ( dProb.doubleValue() > m_dMaxProb ) m_dMaxProb = dProb.doubleValue();
		if ( dProb.doubleValue() < m_dMinProb ) m_dMinProb = dProb.doubleValue();
        System.out.println("minProb: " + m_dMinProb + ", maxProb: " + m_dMaxProb);
	}

	protected List<ChangeRequest> normalize( List<ChangeRequest> changeRequests ){
        for ( ChangeRequest changeRequest: changeRequests)
		{
			POMObject obj = (POMObject)changeRequest.getObject();
			obj.setProbability( obj.getProbability() / ( m_dMinProb - m_dMaxProb ) );
		}
		return changeRequests;
	} 
}

