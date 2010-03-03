package org.ontoware.text2onto.algorithm.auxiliary.context;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.reference.document.DocumentReference;

/**
 * @author Günter Ladwig
 */
public class ExampleContextExtraction extends AbstractContextExtraction {
 
	protected List<String> getFeatures( DocumentReference entityReference ) {
		List<String> alFeatures = new ArrayList<String>();
		List sentReferences = m_analyser.getSentenceReferences( entityReference );
		System.out.println( entityReference );
		if( sentReferences.size() > 0 ) 
		{
			DocumentReference sentReference = (DocumentReference) sentReferences.get( 0 );
			alFeatures.addAll( m_analyser.getTokenStems( sentReference ) );
		}
		return alFeatures;
	}
}
