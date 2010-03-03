package org.ontoware.text2onto.algorithm.auxiliary.context;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.reference.document.DocumentReference;

/**
 * @author Günter Ladwig
 */
public class ContextExtractionWithFrame extends AbstractContextExtraction {

	int m_iFrame = 15;
 
	protected List<String> getFeatures( DocumentReference entityReference ) {
		List<String> alFeatures = new ArrayList<String>();
		List lSentReferences = m_analyser.getSentenceReferences( entityReference );
		if( lSentReferences.size() > 0 ) 
		{
			DocumentReference sentReference = (DocumentReference) lSentReferences.get( 0 );
			List lNounPhrasesHeadTokens = m_analyser.getDocumentReferences( sentReference, LinguisticAnalyser.HEAD );
			int iHeadIndex = lNounPhrasesHeadTokens.indexOf( entityReference );
			DocumentReference ref = (DocumentReference) lNounPhrasesHeadTokens.get( iHeadIndex );

			lNounPhrasesHeadTokens = m_analyser.getTokenReference( ref );
			DocumentReference referenceFirstToken = (DocumentReference) lNounPhrasesHeadTokens.get( 0 );
			List lSentenceTokens = m_analyser.getTokenReference( sentReference );

			int iMiddle = lSentenceTokens.indexOf( referenceFirstToken );
			int iCountTokens = lNounPhrasesHeadTokens.size();
			int iStartLeft = iMiddle - 1;
			int iStartRight = iMiddle + iCountTokens;

			List lFrameTokens = getTokensWithinFrame( m_iFrame, iStartLeft, true, lSentenceTokens );
			lFrameTokens.addAll( getTokensWithinFrame( m_iFrame, iStartRight,	false, lSentenceTokens ) );
			for( int i = 0; i < lFrameTokens.size(); i++ ) 
			{
				DocumentReference reference = (DocumentReference) lFrameTokens.get( i );
				// lTokens contains only one element
				List lTokens = (List) m_analyser.removeStopwords( reference );
				for( int j = 0; j < lTokens.size(); j++ ) 
				{
					String sToken = (String)lTokens.get( j );
					List lCleanTokens = (List) m_analyser.removeSpecialCharacters( sToken );
					for( int k = 0; k < lCleanTokens.size(); k++ ) 
					{
						String sCleanToken = (String) lCleanTokens.get( k );
						if( sCleanToken.length() > 1 ) {
							alFeatures.addAll( lCleanTokens );
						}
					}
				}
			}
		}
		return alFeatures;
	}

	private List getTokensWithinFrame( int iFrame, int iStartIndex, boolean bLeft, List lReferences ) {
		List lReturn = new ArrayList();
		if ( bLeft ){
			for( int i = 0; i < iFrame && iStartIndex >= 0
					&& lReferences.get( iStartIndex ) != null; i++ ) 
			{
				lReturn.add( lReferences.get( iStartIndex ) );
				iStartIndex--;
			}
		} 
		else {
			for ( int i = 0; i < iFrame && iStartIndex < lReferences.size()
					&& lReferences.get( iStartIndex ) != null; i++ ) 
			{
				lReturn.add( lReferences.get( iStartIndex ) );
				iStartIndex++;
			}
		}
		return lReturn;
	}
}
