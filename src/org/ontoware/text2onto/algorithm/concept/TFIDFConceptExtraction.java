package org.ontoware.text2onto.algorithm.concept;

import java.util.List;
import java.util.Iterator;
import java.lang.Math;
 
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.change.ChangeRequest; 
import org.ontoware.text2onto.pom.POMObject; 

/* 
 * @author Matthias Hartung (hartung@urz.uni-heidelberg.de)
 */
public class TFIDFConceptExtraction extends AbstractConceptExtraction {

	protected double m_dProbSum = 0.0;
 
 
	protected static double lg( double x ){
		return ( Math.log( x ) / Math.log( 10 ) );	// log_10
	}

	protected double getProbability( POMObject object, int iReferences ){
		// numDocs: number of documents within the document collection
		// lDf_i: list of documents containing specific concept candidate "i"
		// dTfidf_ij: tfidf-value for certain concept candidate "i", based upon document "j"
		// tfidf_i: tfidf-value for certain concept candidate "i", normalized over whole document collection

		ReferenceStore refStore = getEvidenceStore( ReferenceStore.class );
		List lDf_i = refStore.getDocuments( object );
		int iNumDocs = refStore.getDocuments().size(); // m_corpus.getDocuments().size();
		Iterator docsIter = lDf_i.iterator();
		double dSum = 0.0;
		while( docsIter.hasNext() )
		{
			AbstractDocument doc = (AbstractDocument)docsIter.next();
			Double dTfidf_ij = new Double( refStore.getReferences( object, doc ).size() * lg( (double)iNumDocs / (double)lDf_i.size() )  );
			dSum += dTfidf_ij.doubleValue();
		}
		return dSum / (double)iNumDocs;  // tfidf_i
	}

	protected List<ChangeRequest> normalize( List<ChangeRequest> list ){
		for ( ChangeRequest changeRequest: list )
		{
			POMObject obj = (POMObject)changeRequest.getObject();
			obj.setProbability( obj.getProbability() / Math.sqrt( m_dProbSum ) );
		}
		return list;
	}

	protected void prepareNormalization( Double dProb ){
		m_dProbSum = m_dProbSum + Math.pow( dProb.doubleValue(), 2 );
	} 
}

