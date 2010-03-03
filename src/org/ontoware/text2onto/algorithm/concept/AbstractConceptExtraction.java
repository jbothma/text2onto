package org.ontoware.text2onto.algorithm.concept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
 
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMSubclassOfRelation;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.util.Settings;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.linguistic.SpanishLinguisticAnalyser;
import org.ontoware.text2onto.algorithm.AbstractEntityExtraction;
import org.ontoware.text2onto.algorithm.taxonomic.subclassOf.SpanishVerticalRelationsConceptClassification;

// import com.sun.media.sound.AlawCodec;

/**
 * @author Günter Ladwig
 */
public abstract class AbstractConceptExtraction extends AbstractEntityExtraction {

	protected HashMap<POMEntity,List<DocumentReference>> getEntity2References( AbstractDocument doc ){
		HashMap<POMEntity,List<DocumentReference>> hmEntity2References = new HashMap<POMEntity,List<DocumentReference>>();
		List allReferences =  m_analyser.getDocumentReferences( doc, LinguisticAnalyser.CONCEPT );
		for( Iterator pIter = allReferences.iterator(); pIter.hasNext(); )
		{
			DocumentReference entityReference = (DocumentReference)pIter.next();
			String sEntity = getEntity( entityReference );
			
			/* 
			 * 
			 * Spanish specific code
			 * 
			 */
			if ( Settings.get( Settings.LANGUAGE ).compareTo( Settings.SPANISH ) == 0 ) {
				try {
					sEntity = m_analyser.getStemmedText( entityReference );
				} catch ( AnalyserException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			/*
			 * 
			 * End of Spanish specific code
			 * 
			 */
			
			if( sEntity == null ){
				continue;
			}
			POMEntity entity = m_pom.newConcept( sEntity );
			List<DocumentReference> references = hmEntity2References.get( entity );
			if( references == null )
			{
				references = new ArrayList<DocumentReference>();
				hmEntity2References.put( entity, references );
			}
			references.add( entityReference ); 
		}
		/* 
		 * 
		 * Spanish specific code
		 * 
		 */
		if ( Settings.get( Settings.LANGUAGE ).compareTo( Settings.SPANISH ) == 0 ) {
			List<DocumentReference> npReferences =m_analyser.getDocumentReferences( doc, LinguisticAnalyser.NOUN_PHRASE );
			try {
			} catch ( Exception e1 ) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int i = 0;
			for( DocumentReference npReference: npReferences ) 
			{ 
				i++;
				try {
					ArrayList alConceptReference = getVerticalConcept( npReference );
					if ( alConceptReference.size() != 0 ) {
						POMEntity entity = (POMEntity) alConceptReference.get( 0 );
						List<DocumentReference> references = hmEntity2References.get( entity );
						if( references == null )
						{
							references = new ArrayList<DocumentReference>();
							hmEntity2References.put( entity, references );
						}
						references.add( (DocumentReference) alConceptReference.get( 1 ) ); 
					}					
				} catch ( AnalyserException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		/*
		 * 
		 * End of Spanish specific code
		 * 
		 */
		return hmEntity2References;
	}
	
	/*
	 * 
	 * Spanish specific functions
	 * 
	 */
	
	private ArrayList getVerticalConcept( DocumentReference reference ) throws AnalyserException {
		//SpanishVerticalRelationsConceptClassification algorithm = new SpanishVerticalRelationsConceptClassification();
		ArrayList alReturn = new ArrayList();
		List<DocumentReference> headReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.HEAD );
		long lEndOffsetHeads = -1;
		long lStartOffsetHead;
		long lStart = 0;
		ArrayList<String> alHeads = new ArrayList();
		ArrayList<String> alRelationStrings;
		DocumentReference tokenReference = null;
		if ( headReferences.size() > 0 ){
			for ( int iIndex = 0; iIndex < headReferences.size(); iIndex++ ) {
				DocumentReference headReference = headReferences.get( iIndex );
				if ( iIndex > 0 ) {
					lStartOffsetHead = headReference.getStartOffset();
					if ( lStartOffsetHead == lEndOffsetHeads + 1 ) {
						alHeads.add( m_analyser.getStemmedText( headReference ) );
						alHeads.add( headReference.getText() );
						lEndOffsetHeads = headReference.getEndOffset();
					}
				}
				else {
					lStart =  headReference.getStartOffset();
					alHeads.add( m_analyser.getStemmedText( headReference ) );
					alHeads.add( headReference.getText() );
					lEndOffsetHeads = headReference.getEndOffset();
				}				
			}
		}
		ArrayList alPP =  getPrepositionalPhrase( reference, lEndOffsetHeads );
		ArrayList alPadj = getAdjectives ( reference, lEndOffsetHeads );
		String sPrepositionalPhraseLemma = null;
		String sAdjectivesLemma = null;
		String sPrepositionalPhrase = null;
		String sAdjectives = null;
		if ( alPP.size() == 3 ) {
			sPrepositionalPhraseLemma = (String) alPP.get( 0 );
			sPrepositionalPhrase = (String) alPP.get( 2 );
			Long lEnd = (Long) alPP.get( 1 );
			POMConcept cConcept = getConcept( alHeads, sPrepositionalPhraseLemma, sAdjectivesLemma );
			alReturn.add(cConcept);
			alReturn.add (((SpanishLinguisticAnalyser) m_analyser).getDocumentReferences( reference.getDocument(), lStart, lEnd ,getWords( alHeads, sPrepositionalPhrase, sAdjectives ) ) );
		}
		else if ( alPadj.size() == 3 ) {
			sAdjectivesLemma = (String) alPadj.get( 0 );
			sAdjectives = (String) alPadj.get( 2 );
			Long lEnd = (Long) alPadj.get( 1 );
			POMConcept cConcept = getConcept( alHeads, sPrepositionalPhraseLemma, sAdjectivesLemma );
			alReturn.add(cConcept);
			alReturn.add ( ( (SpanishLinguisticAnalyser) m_analyser ).getDocumentReferences( reference.getDocument(), lStart, lEnd , getWords( alHeads, sPrepositionalPhrase, sAdjectives ) ) );
		}
		return alReturn;
	}
	
	private ArrayList getPrepositionalPhrase( DocumentReference reference, long endOffsetReference ) {
		ArrayList alResults = new ArrayList();
		List<DocumentReference> lPrepositionalPhraseReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.PREPOSITIONAL_PHRASE );
		DocumentReference reference1 = null;
		try {
			if ( lPrepositionalPhraseReferences.size() > 0 ) {
				for( DocumentReference prepositionalPhraseReference: lPrepositionalPhraseReferences ) {
					if ( prepositionalPhraseReference.getStartOffset() == endOffsetReference + 1 ) {
						reference1 = prepositionalPhraseReference;
						List<DocumentReference> lTokenReferences = m_analyser.getTokenReference(reference1);
						List tokenPOS = ((SpanishLinguisticAnalyser) m_analyser).getTokenPOS( reference1 );
						String[] sSplit = reference1.getText().trim().split(" ");
						if ( ( sSplit.length > 1 )  && ( sSplit[0].compareTo( "de" ) == 0 || sSplit[0].compareTo( "del" ) == 0 ) 
								&& ((String)tokenPOS.get( 1 )).compareTo( "NC" ) == 0 ) {
							alResults.add( sSplit[0] + " " + m_analyser.getStemmedText( lTokenReferences.get(1) ) );
							alResults.add( lTokenReferences.get( 1 ).getEndOffset() );
							alResults.add( sSplit[0] + " " + sSplit[1] );
							return alResults;
						}
					}
				}
			}
		} catch ( AnalyserException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return alResults;
	}
	
	private String getWords( ArrayList<String> heads, String prepositionalPhrase, String adjectives ) throws AnalyserException {
		String sDomain = "";
		for ( int iIndex = 1; iIndex < heads.size(); iIndex += 2 ) {
			sDomain = sDomain + heads.get( iIndex ) + " ";
		}
		if ( prepositionalPhrase != null ) {
			sDomain = sDomain + prepositionalPhrase;
		}
		else if ( adjectives != null ) {
			sDomain = sDomain + adjectives;
		}
		else {
			return null;
		}
		return sDomain;
	}
	
	private POMConcept getConcept( ArrayList<String> heads, String prepositionalPhrase, String adjectives ) throws AnalyserException {
		String sDomain = "";
		for ( int iIndex = 0; iIndex < heads.size(); iIndex += 2 ) {
			sDomain = sDomain + heads.get( iIndex ) + " ";
		}
		if ( prepositionalPhrase != null ) {
			sDomain = sDomain + prepositionalPhrase;
		}
		else if ( adjectives != null ) {
			sDomain = sDomain + adjectives;
		}
		else {
			return null;
		}
		POMConcept domain = m_pom.newConcept( sDomain );
		return domain;
	}
	
	private ArrayList getAdjectives(DocumentReference reference, long endOffsetHeads) {
		ArrayList alResults = new ArrayList();
		List<DocumentReference> lAdjectiveReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.ADJECTIVE );
		String sAdjectivesLemma = null;
		String sAdjectives = null;
		long lAdjectiveEndOffset = -1;
		try {
			if ( lAdjectiveReferences.size() > 0 ) {
				for( DocumentReference adjectiveReference: lAdjectiveReferences ) {
					if ( adjectiveReference.getStartOffset() == endOffsetHeads + 1 ) {
						sAdjectivesLemma = m_analyser.getStemmedText( adjectiveReference );
						sAdjectives = adjectiveReference.getText();
						lAdjectiveEndOffset = adjectiveReference.getEndOffset();
					}
					else if ( adjectiveReference.getStartOffset() == lAdjectiveEndOffset + 1 ) {
						sAdjectivesLemma = sAdjectivesLemma + " " + m_analyser.getStemmedText( adjectiveReference );
						sAdjectives = sAdjectivesLemma + " " + adjectiveReference.getText();
						lAdjectiveEndOffset = adjectiveReference.getEndOffset();
					}
					else {
						return alResults;
					}
				}
				alResults.add( sAdjectivesLemma );
				alResults.add( lAdjectiveEndOffset );			
				alResults.add( sAdjectives );
			
				//alResults.add
			}
		} catch ( AnalyserException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return alResults;
	}

	
}
