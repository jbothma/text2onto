package org.ontoware.text2onto.linguistic;

import java.util.List;

import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.reference.document.DocumentReference;

public class SpanishLinguisticAnalyser extends LinguisticAnalyser{

	
	public SpanishLinguisticAnalyser() throws AnalyserException {
		super();
	}

	public List getTokenPOS( DocumentReference reference ) {
		return getTokenFeatureValues( "pos", reference );
	}

	public List getTokenLemma( DocumentReference reference ) {
		return getTokenFeatureValues( "lemma", reference );
	}
	
	public String getStemmedText( DocumentReference reference ) {
		List<DocumentReference> lTokenReferences = getTokenReference( reference );
		List<DocumentReference> lProperNounReference = getDocumentReferences( reference, LinguisticAnalyser.PROPER_NOUN );
		String sStemmedText = "";
		if ( lProperNounReference.size() > 0 ) {
			DocumentReference referenceProperNoun = lProperNounReference.get( 0 );
			if ( referenceProperNoun.getStartOffset() == reference.getStartOffset() 
					&& referenceProperNoun.getEndOffset() == reference.getEndOffset() ) {
				return referenceProperNoun.getText();
			}
		}		
		if ( lTokenReferences.size() > 0 ) {
			for ( DocumentReference reference1: lTokenReferences ) {
				sStemmedText = sStemmedText + (String) getTokenFeatureValues( "lemma", reference1 ).get( 0 ) + " ";
			}
			sStemmedText = sStemmedText.substring( 0, sStemmedText.lastIndexOf( " " ) );
		}
		return sStemmedText;
	}
	
	public DocumentReference getDocumentReferences ( AbstractDocument doc, long lStartOffset, long lEndOffset, String sText ) {
		return new DocumentReference( this, doc, lStartOffset, lEndOffset, sText ); 
	}

}