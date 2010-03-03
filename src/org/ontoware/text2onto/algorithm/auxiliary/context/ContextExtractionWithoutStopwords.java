package org.ontoware.text2onto.algorithm.auxiliary.context;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.reference.document.DocumentReference;

public class ContextExtractionWithoutStopwords extends AbstractContextExtraction {
 
	protected List<String> getFeatures( DocumentReference entityReference ) {
		List<String> alFeatures = new ArrayList<String>();
		List sentReferences = m_analyser.getSentenceReferences( entityReference );
		if ( sentReferences.size() > 0 ) 
		{
			DocumentReference sentReference = (DocumentReference) sentReferences.get( 0 );
			List lReferences = m_analyser.getTokenReference( sentReference );
			for ( int i = 0; i < lReferences.size(); i++ ) 
			{
				DocumentReference reference = (DocumentReference) lReferences.get( i );
				List lTokens = (List) m_analyser.removeStopwords( reference );
				for ( int j = 0; j < lTokens.size(); j++ ) 
				{
					String sToken = (String) lTokens.get( j );
					List<String> lCleanTokens = m_analyser.removeSpecialCharacters( sToken );
					alFeatures.addAll( lCleanTokens );
				}
			}
		}
		return alFeatures;
	}
}
