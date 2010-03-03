package org.ontoware.text2onto.algorithm.auxiliary.context;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.reference.document.DocumentReference;

/**
 * @author Günter Ladwig
 */
public class ContextExtractionWithFrameV2 extends AbstractContextExtraction {

	private int m_iMiddle;

	private int m_iFrame = 12;

	protected List<String> getFeatures( DocumentReference entityReference ) {
		int iFrameTemp = m_iFrame;
		List<String> alFeatures = new ArrayList<String>();
		List lSentReferences = m_analyser.getSentenceReferences( entityReference );
		if ( lSentReferences.size() > 0 ) 
		{
			DocumentReference sentReference = (DocumentReference) lSentReferences.get( 0 );
			List lNounPhrasesHeads = m_analyser.getDocumentReferences( sentReference, LinguisticAnalyser.HEAD );
			int iHeadIndex = lNounPhrasesHeads.indexOf( entityReference );
			DocumentReference ref = (DocumentReference) lNounPhrasesHeads.get( iHeadIndex );
			lNounPhrasesHeads = m_analyser.getTokenReference( ref );
			DocumentReference referenceFirstToken = (DocumentReference) lNounPhrasesHeads.get( 0 );

			List lReferences = m_analyser.getTokenReference( sentReference );
			int iMiddle = lReferences.indexOf( referenceFirstToken );
			int iCountTokens = lNounPhrasesHeads.size();
			int iStartLeft = iMiddle - 1;
			int iStartRight = iMiddle + iCountTokens;

			if ( iMiddle >= 0 ) 
			{
				int iMiddleTemp = iMiddle;

				// left side of middle
				while ( m_iFrame > 0 && iStartLeft > 0
						&& lReferences.get( iStartLeft ) != null ) 
				{
					DocumentReference reference = (DocumentReference) lReferences.get( iStartLeft );
					iStartLeft--;
					List lTokens = (List) m_analyser.removeStopwords( reference );
					for ( int k = 0; k < lTokens.size(); k++ ) 
					{
						String sToken = (String) lTokens.get( k );
						List lCleanTokens = (List) m_analyser.removeSpecialCharacters( sToken );
						if ( lCleanTokens != null ) 
						{
							alFeatures.addAll( lCleanTokens );
							m_iFrame--;
						}
					}
				}

				iMiddle = iMiddleTemp;
				m_iFrame = iFrameTemp;
				// right side of middle
				while ( m_iFrame > 0 && iStartRight < lReferences.size()
						&& lReferences.get( iStartRight ) != null ) 
				{
					DocumentReference reference = (DocumentReference) lReferences.get( iStartRight );
					iStartRight++;
					List lTokens = (List) m_analyser.removeStopwords( reference );
					for ( int k = 0; k < lTokens.size(); k++ ) 
					{
						String sToken = (String) lTokens.get( k );
						List lCleanTokens = (List) m_analyser.removeSpecialCharacters( sToken );
						if ( lCleanTokens != null ) 
						{
							alFeatures.addAll( lCleanTokens );
							m_iFrame--;
						}
					}
				}
			}
		}
		m_iFrame = iFrameTemp;
		return alFeatures;
	}
}
