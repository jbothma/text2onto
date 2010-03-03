package org.ontoware.text2onto.algorithm.auxiliary.context;

import java.util.ArrayList;
import java.util.List;
import org.ontoware.text2onto.linguistic.*;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.util.Settings;

public class ContextFeaturesExtraction extends AbstractContextExtraction {
	
	protected List<String> getFeatures( DocumentReference entityReference ) {
		if ( Settings.get( Settings.LANGUAGE ).compareTo( Settings.SPANISH) == 0 ) {
			return getFeaturesSpanish ( entityReference );
		}
		else {
			return getFeaturesEnglish ( entityReference );
		}	
	}
	
	private List<String> getFeaturesEnglish(DocumentReference entityReference) {
		List<DocumentReference> lVerbPhraseReferencesDocument = m_analyser.getDocumentReferences( entityReference.getDocument() , "TransitiveVerbPhrase" );
		List<DocumentReference> lNounPhraseReferencesDocument = m_analyser.getDocumentReferences( entityReference.getDocument() , "NounPhrase" );
		List<String> alFeatures = new ArrayList<String>();
		List<DocumentReference> lTransitiveVerbPhraseReferences = m_analyser.getDocumentReferences( entityReference, "TransitiveVerbPhrase" );
		List<DocumentReference> lIntransitivePPVerbPhraseReferences = m_analyser.getDocumentReferences( entityReference, "IntransitivePPVerbPhrase" );
		List<DocumentReference> lTransitivePPVerbPhraseReferences = m_analyser.getDocumentReferences( entityReference, "TransitivePPVerbPhrase" );
		List<DocumentReference> lSubjectPhraseReferences = m_analyser.getDocumentReferences( entityReference, "Subject" );
		List<DocumentReference> lObjectPhraseReferences = m_analyser.getDocumentReferences( entityReference, "Object" );
		List<DocumentReference> lPObjectPhraseReferences = m_analyser.getDocumentReferences( entityReference, "PObject" );
		if ( lTransitiveVerbPhraseReferences.size() > 0 ) {
			DocumentReference verbPhraseReference = (DocumentReference)lTransitiveVerbPhraseReferences.get( 0 );
			List<DocumentReference> verbReferences = m_analyser.getDocumentReferences( verbPhraseReference, "Verb" );
			if ( verbReferences.size() > 0 ) {
				DocumentReference verbReference = verbReferences.get( 0 );
				String sVerb = verbReference.getText();
				if ( lSubjectPhraseReferences.size() > 0 ) {
					alFeatures.add( sVerb + "_subj" );
				}
				else if ( lObjectPhraseReferences.size() > 0 ) {
					alFeatures.add( sVerb + "_obj" );
				}
			}
		}
		if ( lIntransitivePPVerbPhraseReferences.size() > 0 ) {
			DocumentReference verbPhraseReference = (DocumentReference)lIntransitivePPVerbPhraseReferences.get( 0 );
			List<DocumentReference> verbReferences = m_analyser.getDocumentReferences( verbPhraseReference, "Verb" );
			List<DocumentReference> prepositionReferences = m_analyser.getDocumentReferences( verbPhraseReference, "Preposition" );
			if ( verbReferences.size() > 0 ) {
				DocumentReference verbReference = verbReferences.get( 0 );
				String sVerb = verbReference.getText();
				if ( prepositionReferences.size() > 0 ) {
					DocumentReference prepositionalReference = prepositionReferences.get( 0 );
					String sPreposition = prepositionalReference.getText();
					if ( lSubjectPhraseReferences.size() > 0 ) {
						alFeatures.add( sVerb + "_" + sPreposition + "_subj" );
					}
					else if ( lPObjectPhraseReferences.size() > 0 ) {
						alFeatures.add( sVerb + "_" + sPreposition + "_pobj" );
					}
				}				
			}
		}
		if ( lTransitivePPVerbPhraseReferences.size() > 0 ) {
			DocumentReference verbPhraseReference = (DocumentReference)lTransitivePPVerbPhraseReferences.get( 0 );
			List<DocumentReference> verbReferences = m_analyser.getDocumentReferences( verbPhraseReference, "Verb" );
			List<DocumentReference> prepositionReferences = m_analyser.getDocumentReferences( verbPhraseReference, "Preposition" );
			if ( verbReferences.size() > 0 ) {
				DocumentReference verbReference = verbReferences.get( 0 );
				String sVerb = verbReference.getText();
				if ( prepositionReferences.size() > 0 ) {
					DocumentReference prepositionalReference = prepositionReferences.get( 0 );
					String sPreposition = prepositionalReference.getText();
					if ( lSubjectPhraseReferences.size() > 0 ) {
						alFeatures.add( sVerb + "_subj" );
					}
					else if ( lPObjectPhraseReferences.size() > 0 ) {
						alFeatures.add( sVerb + "_" + sPreposition + "_pobj" );
					}
					else if ( lObjectPhraseReferences.size() > 0 ) {
						alFeatures.add( sVerb + "_obj" );
					}
				}				
			}
		}		
		return alFeatures;
	}
	
	private List<String> getFeaturesSpanish( DocumentReference entityReference ) {
		//List<DocumentReference> lVerbPhraseReferencesDocument = m_analyser.getDocumentReferences( entityReference.getDocument() , "TransitiveVerbPhrase" );
		//List<DocumentReference> lNounPhraseReferencesDocument = m_analyser.getDocumentReferences( entityReference.getDocument() , "NounPhrase" );
		List<String> alFeatures = new ArrayList<String>();
		List<DocumentReference> lVerbPhraseReferences = m_analyser.getDocumentReferences( entityReference, "TransitiveVerbPhrase" );
		if ( lVerbPhraseReferences.size() > 0 ) {
			DocumentReference verbPhraseReference = (DocumentReference)lVerbPhraseReferences.get( 0 );
			List<DocumentReference> verbReferences = m_analyser.getDocumentReferences( verbPhraseReference, "Verb" );
			if ( verbReferences.size() > 0 ) {
				int iLastVerbPosition = verbReferences.size() - 1;
				DocumentReference verbReference = verbReferences.get( iLastVerbPosition );
				List lLema = ( (SpanishLinguisticAnalyser)m_analyser ).getTokenLemma( verbReference );
				if ( ((String)lLema.get( 0 )).compareTo( "ser" ) == 0 ) {
					return alFeatures;
				}
				long lStartOffsetVerbReference = verbReferences.get( 0 ).getStartOffset();				
				long lEndOffsetVerbReference = verbReference.getEndOffset();
				List<DocumentReference> lNounPhraseReferences = m_analyser.getDocumentReferences( entityReference, "NounPhrase" );
				if ( lNounPhraseReferences.size() > 0 ) {
					DocumentReference nounPhraseReference = lNounPhraseReferences.get( 0 );
					long lStartOffsetNounPhraseReference = nounPhraseReference.getStartOffset();
					long lEndOffsetNounPhraseReference = nounPhraseReference.getEndOffset();
					if ( lEndOffsetVerbReference + 1 == lStartOffsetNounPhraseReference ) {		
						List lLemma = ( (SpanishLinguisticAnalyser)m_analyser ).getTokenLemma( verbReference );
						String sFeature = (String) lLemma.get( 0 );
						sFeature = sFeature.concat( "_obj" );
						alFeatures.add( sFeature );
					}
					else if ( lEndOffsetNounPhraseReference + 1 == lStartOffsetVerbReference ) {	
						List lLemma = ( (SpanishLinguisticAnalyser)m_analyser ).getTokenLemma( verbReference );
						String sFeature = lLemma.get( 0 ) + "_subj";
						alFeatures.add( sFeature );
					}
				}
				List<DocumentReference> lProperNounPhraseReferences = m_analyser.getDocumentReferences( entityReference, "ProperNounPhrase" );
				if ( lProperNounPhraseReferences.size() > 0 ) {
					DocumentReference properNounPhraseReference = lProperNounPhraseReferences.get( 0 );
					long lStartOffsetProperNounPhraseReference = properNounPhraseReference.getStartOffset();
					long lEndOffsetProperNounPhraseReference = properNounPhraseReference.getEndOffset();
					if ( lEndOffsetVerbReference + 1 == lStartOffsetProperNounPhraseReference ) {		
						List lLemma = ( (SpanishLinguisticAnalyser)m_analyser ).getTokenLemma( verbReference );
						String sFeature = (String) lLemma.get( 0 );
						sFeature = sFeature.concat( "_obj" );
						alFeatures.add( sFeature );
					}
					else if ( lEndOffsetProperNounPhraseReference + 1 == lStartOffsetVerbReference ) {	
						List lLemma = ( (SpanishLinguisticAnalyser)m_analyser ).getTokenLemma( verbReference );
						String sFeature = lLemma.get( 0 ) + "_subj";
						alFeatures.add( sFeature );
					}
				}
				List<DocumentReference> lPrepositionalPhraseReferences = m_analyser.getDocumentReferences( entityReference, "PrepositionalPhrase" );
				if ( lPrepositionalPhraseReferences.size() > 0 ) {
					DocumentReference prepositionalPhraseReference = lPrepositionalPhraseReferences.get( 0 );
					long lStartOffsetPrepositionalPhraseReference = prepositionalPhraseReference.getStartOffset();
					if ( lEndOffsetVerbReference + 1 == lStartOffsetPrepositionalPhraseReference ) {		
						List lLemma = ( (SpanishLinguisticAnalyser)m_analyser ).getTokenLemma( verbReference );
						List lPrepositionalPhraseStrings = m_analyser.getTokenStrings( prepositionalPhraseReference );
						String sFeature = (String) lLemma.get( 0 );
						sFeature = sFeature.concat( "_" + lPrepositionalPhraseStrings.get( 0 ) );
						alFeatures.add( sFeature );
					} 
				}
			}
		}
		List<DocumentReference> lNounPhraseReferences = m_analyser.getDocumentReferences( entityReference, "NounPhrase" );
		if ( lNounPhraseReferences.size() > 0 ) {
			DocumentReference nounPhraseReference = lNounPhraseReferences.get( 0 );
			long lStartOffsetEntityReference = entityReference.getStartOffset();
			long lEndOffsetEntityReference = entityReference.getEndOffset();
			List<DocumentReference> lTokenReferences = m_analyser.getTokenReferences( nounPhraseReference );
			for ( DocumentReference reference: lTokenReferences ) {
				List lPOSReference = ( (SpanishLinguisticAnalyser)m_analyser ).getTokenPOS( reference );
				String sPOSReference = (String) lPOSReference.get(0);
				if ( reference.getEndOffset() + 1 == lStartOffsetEntityReference 
						&& sPOSReference.compareTo( "ADJ" ) == 0 ) {
					alFeatures.add( reference.getText() + "_adj" );
				}
				if ( reference.getStartOffset() - 1 == lEndOffsetEntityReference 
					&& sPOSReference.compareTo( "ADJ" ) == 0 ) {
					alFeatures.add( reference.getText() + "_adj" );
				}				
			}
		}
		List<DocumentReference> lProperNounPhraseReferences = m_analyser.getDocumentReferences( entityReference, "ProperNounPhrase" );
		if ( lProperNounPhraseReferences.size() > 0 ) {
			DocumentReference properNounPhraseReference = lProperNounPhraseReferences.get( 0 );
			long lStartOffsetEntityReference = entityReference.getStartOffset();
			long lEndOffsetEntityReference = entityReference.getEndOffset();
			List<DocumentReference> lTokenReferences = m_analyser.getTokenReferences( properNounPhraseReference );
			for ( DocumentReference reference: lTokenReferences ) {
				List lPOSReference = ( (SpanishLinguisticAnalyser)m_analyser ).getTokenPOS( reference );
				String sPOSReference = (String) lPOSReference.get(0);
				if ( reference.getEndOffset() + 1 == lStartOffsetEntityReference 
						&& sPOSReference.compareTo( "ADJ" ) == 0 ) {
					alFeatures.add( reference.getText() + "_adj" );
				}
				if ( reference.getStartOffset() - 1 == lEndOffsetEntityReference 
					&& sPOSReference.compareTo( "ADJ" ) == 0 ) {
					alFeatures.add( reference.getText() + "_adj" );
				}				
			}
		}
		
		return alFeatures;
	}
}
		
