package org.ontoware.text2onto.algorithm.taxonomic.subclassOf;

import gate.creole.ResourceInstantiationException;
import gate.creole.ir.IndexException;
import gate.creole.ir.SearchException;
import gate.persist.PersistenceException;
import gate.security.SecurityException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.EvidenceChange;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.corpus.TextDocument;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.explanation.reference.ReferenceExplanation;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.linguistic.LinguisticException;
import org.ontoware.text2onto.linguistic.SpanishLinguisticAnalyser;
import org.ontoware.text2onto.linguistic.WordCounter;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMSubclassOfRelation;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.reference.document.DocumentReferenceStore;

public class SpanishVerticalRelationsConceptClassification extends AbstractSimpleAlgorithm implements AbstractConceptClassification {

	
	private WordCounter m_WordCounter;
	
	public void initialize() throws Exception {
		m_algorithmController.requestLocalEvidenceStore( this, ReferenceStore.class );
		m_algorithmController.requestLocalReferenceStore( this, DocumentReferenceStore.class );
		m_WordCounter = new WordCounter ();
		List<AbstractDocument> lDocuments = m_corpus.getDocuments();
		for ( AbstractDocument document: lDocuments) {
			m_WordCounter.addDocument( document );
		}	
	}
	
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		List<ChangeRequest> evidenceChangeRequests= new ArrayList<ChangeRequest>();
		List<Change> corpusChanges = m_corpus.getChangesFor(this); 
		for( Change change: corpusChanges )
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			System.out.println( "SpanishVerticalRelationsConceptClassification.getEvidenceChanges: "+ doc );
			
			List<DocumentReference> npReferences = getNounPhrasePointers( doc ); 
			//List pRef = m_analyser.getDocumentReferences( doc, LinguisticAnalyser.PREPOSITIONAL_PHRASE);
			for( DocumentReference npReference: npReferences ) 
			{ 
				POMSubclassOfRelation relation = getVerticalPhrase( npReference );
				if( relation == null ){
					continue;
				}
				if( iChange == Change.Type.ADD ){
					evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.ADD, this,
						getLocalEvidenceStore( ReferenceStore.class ), relation, npReference, change ) ) );
				}
				else {
					evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE, this,
						getLocalEvidenceStore( ReferenceStore.class ), relation, npReference, change ) ) );
				}
			}
		} 
		return evidenceChangeRequests;
	}
	
	protected AbstractExplanation getExplanation( POMChange change ){ 
		Object object = change.getObject();
		if( !( object instanceof POMSubclassOfRelation ) )
		{
			return null;
		}
		POMSubclassOfRelation relation = (POMSubclassOfRelation)object;
		ReferenceExplanation explanation = new ReferenceExplanation( relation, this, change );
		List<Change> causes = change.getCauses();
		int iCausedByThis = 0;
		for( Change cause: causes )
		{
			if( cause.getSource().equals( this ) )
			{
				DocumentReference reference = (DocumentReference)cause.getValue();
				explanation.addDocumentReference( reference );
				iCausedByThis++;
			}
		}
		if( iCausedByThis == 0 ){
			return null;
		}
		return explanation;
	}
 
	protected List<ChangeRequest> getReferenceChanges() {
		return new ArrayList<ChangeRequest>();
	}
 
	protected List<ChangeRequest> getPOMChanges() throws Exception {
		//m_LuceneWordCounter.initialize( m_corpus.getDocuments() );
		List<ChangeRequest> pomChangeRequests = new ArrayList<ChangeRequest>();
		ReferenceStore referenceStore = (ReferenceStore)getLocalEvidenceStore( ReferenceStore.class );
		List<Object> objects = referenceStore.getChangedObjectsFor( this );  		
		for( Object object: objects )
		{
			POMSubclassOfRelation relation = (POMSubclassOfRelation)object;
			List<Change> changes = referenceStore.getChangesFor( this, object );  
			double dProb = getProbability( relation );   
			Double prob = new Double (dProb);
			relation.setProbability( prob ); 
			if( dProb == 0.0 ){
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, relation, dProb, changes ) ) );
			}
			else {
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, relation, dProb, changes ) ) ); 
			}  
		}
		//m_LuceneWordCounter.close();
		return pomChangeRequests;
	}
  
	private double getProbability( POMSubclassOfRelation relation ) throws Exception{ 
		double iOccurrencesDomain = m_WordCounter.countOcurrences( relation.getDomain().getLabel() );
		double iOccurrencesRange = m_WordCounter.countOcurrences( relation.getRange().getLabel() );
		
		List lPOMObjects = m_pom.getObjects( POMConcept.class, relation.getRange().getLabel() );
		POMConcept pomConcept = (POMConcept) lPOMObjects.get(0);
		List c =m_pom.getChanges(pomConcept);
		double dRelevance = pomConcept.getProbability();
		double prob = dRelevance * (iOccurrencesDomain / iOccurrencesRange);
		if ( prob > 1 ) {
			prob = 1;
		}
		return prob; 
	}

	private POMSubclassOfRelation getVerticalPhrase( DocumentReference reference ) throws AnalyserException {
		List<DocumentReference> headReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.HEAD );
		long lEndOffsetHeads = -1;
		long lStartOffsetHead;
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
					alHeads.add( m_analyser.getStemmedText( headReference ) );
					alHeads.add( headReference.getText() );
					lEndOffsetHeads = headReference.getEndOffset();
				}				
			}
		}
		ArrayList alPP =  getPrepositionalPhrase( reference, lEndOffsetHeads );
		ArrayList alPadj = getAdjectives ( reference, lEndOffsetHeads );
		String sPrepositionalPhrase = null;
		String sAdjectives = null;
		if ( alPP.size() == 3 ) {
			 sPrepositionalPhrase = (String) alPP.get( 0 );
		}
		else if ( alPadj.size() == 3 ) {
			sAdjectives = (String) alPadj.get( 0 );
		}
		return getRelation( alHeads, sPrepositionalPhrase, sAdjectives );
	}
	
	public ArrayList getVerticalConcept( DocumentReference reference ) throws AnalyserException {
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
				if ( iIndex == 0 ) {
					lStart = headReference.getStartOffset();
				}
				if ( iIndex > 0 ) {
					lStartOffsetHead = headReference.getStartOffset();
					if ( lStartOffsetHead != lEndOffsetHeads + 1 ) {
						return null;
					}
				}				
				alHeads.add( m_analyser.getStemmedText( headReference ) );
				alHeads.add( headReference.getText() );
				lEndOffsetHeads = headReference.getEndOffset();
			}
		}
		String sPrepositionalPhrase = (String) getPrepositionalPhrase( reference, lEndOffsetHeads ).get( 0 );
		String sAdjectives = (String) getAdjectives ( reference, lEndOffsetHeads ).get( 0 );
		POMConcept cConcept = getConcept( alHeads, sPrepositionalPhrase, sAdjectives );
		alReturn.add(cConcept);
		if ( sPrepositionalPhrase.length() > 0 ) {
			Long lEnd = (Long) getPrepositionalPhrase( reference, lEndOffsetHeads ).get( 1 );
			alReturn.add (((SpanishLinguisticAnalyser) m_analyser).getDocumentReferences( reference.getDocument(), lStart, lEnd , cConcept.getLabel()));
		}
		else if ( sPrepositionalPhrase.length() > 0 ) {
			Long lEnd = (Long) getPrepositionalPhrase( reference, lEndOffsetHeads ).get( 1 );
			alReturn.add (((SpanishLinguisticAnalyser) m_analyser).getDocumentReferences( reference.getDocument(), lStart, lEnd , cConcept.getLabel()));
		}
		return alReturn;
	}

	public ArrayList getAdjectives(DocumentReference reference, long endOffsetHeads) {
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

	public ArrayList getPrepositionalPhrase( DocumentReference reference, long endOffsetReference ) {
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
						if ( ( sSplit[0].compareTo( "de" ) == 0 || sSplit[0].compareTo( "del" ) == 0 ) 
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
	


	private List<DocumentReference> getNounPhrasePointers( AbstractDocument doc ) {
		return m_analyser.getDocumentReferences( doc, LinguisticAnalyser.NOUN_PHRASE );
	}

	private POMSubclassOfRelation getRelation( ArrayList<String> heads, String prepositionalPhrase, String adjectives ) throws AnalyserException {
		String sDomain = "";
		String sRange = "";
		for ( int iIndex = 0; iIndex < heads.size(); iIndex += 2 ) {
			sRange = sRange + heads.get( iIndex ) + " ";
		}
		for ( int iIndex = 0; iIndex < heads.size(); iIndex += 2 ) {
			sDomain = sDomain + heads.get( iIndex ) + " ";
		}
		sRange = sRange.substring( 0, sRange.lastIndexOf(" ") );
		
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
		POMConcept range = m_pom.newConcept( sRange );
		POMSubclassOfRelation subclassOfRelation = m_pom.newSubclassOfRelation( domain, range );
		return subclassOfRelation;
	}
	
	public POMConcept getConcept( ArrayList<String> heads, String prepositionalPhrase, String adjectives ) throws AnalyserException {
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
}