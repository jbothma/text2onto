package org.ontoware.text2onto.algorithm.instance;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.pom.POMObject;
import org.ontoware.text2onto.pom.POMEntity; 
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;


/*
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class ExampleInstanceExtraction extends AbstractInstanceExtraction {
 
	protected double getProbability( POMObject object, int iReferences ){
		int iFreq = (getEvidenceStore( ReferenceStore.class )).getReferences( object ).size();
		return (double)iFreq / Math.max( 1.0, (double)iReferences );
	}

	protected List<ChangeRequest> normalize( List<ChangeRequest> list ){
		return list;
	}

	protected void prepareNormalization( Double dProb ){
		// TODO, if normalization required
	}
}

