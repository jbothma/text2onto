package org.ontoware.text2onto.algorithm.taxonomic.subclassOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm; 
import org.ontoware.text2onto.change.Change; 
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.change.EvidenceChange;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.pom.POMConcept; 
import org.ontoware.text2onto.pom.POMSubclassOfRelation;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.reference.document.DocumentReferenceStore;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.explanation.reference.ReferenceExplanation;
import org.ontoware.text2onto.evidence.EvidenceManager;

/*
 * @author Simon Sparn (sparn@ontoprise.de)
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class VerticalRelationsConceptClassification extends AbstractSimpleAlgorithm implements AbstractConceptClassification {
 
	protected void initialize() {
		m_algorithmController.requestLocalEvidenceStore( this, ReferenceStore.class );
		m_algorithmController.requestLocalReferenceStore( this, DocumentReferenceStore.class );
	}
	
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		List<ChangeRequest> evidenceChangeRequests= new ArrayList<ChangeRequest>();
		List<Change> corpusChanges = m_corpus.getChangesFor( this ); 
		for( Change change: corpusChanges )
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			System.out.println( "VerticalRelationsConceptClassification.getEvidenceChanges: " + doc );
			
			List<DocumentReference> npReferences = getNounPhrasePointers( doc ); 
			for( DocumentReference npReference: npReferences ) 
			{ 
				List<DocumentReference> stemmedHeads = this.getStemmedHeads( npReference );
				POMSubclassOfRelation relation = getRelation( stemmedHeads );
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
 
	protected List<ChangeRequest> getPOMChanges() {
		List<ChangeRequest> pomChangeRequests = new ArrayList<ChangeRequest>();
		ReferenceStore referenceStore = (ReferenceStore)getLocalEvidenceStore( ReferenceStore.class );
		List<Object> objects = referenceStore.getChangedObjectsFor( this );  		
		for( Object object: objects )
		{
			POMSubclassOfRelation relation = (POMSubclassOfRelation)object;
			List<Change> changes = referenceStore.getChangesFor( this, object );  
			Double dProb = getProbability( relation );   
			relation.setProbability( dProb.doubleValue() ); 
			if( dProb == 0.0 ){
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, relation, dProb, changes ) ) );
			}
			else {
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, relation, dProb, changes ) ) ); 
			}  
		}
		return pomChangeRequests;
	}
  
	private Double getProbability( POMSubclassOfRelation relation ){ 
		ReferenceStore refStore = (ReferenceStore)getLocalEvidenceStore( ReferenceStore.class );
		int iX = refStore.getReferences( relation.getDomain() ).size();
		int iXY = refStore.getReferences( relation.getRange() ).size(); 
		int iDiff = Math.abs( iXY - iX );
		iDiff = ( iDiff == 0 )? 1 : iDiff;
		double dProb = 1.0 / (double)iDiff;
		return new Double( dProb ); 
	}

	private List getStemmedHeads( DocumentReference reference ) throws AnalyserException {
		ArrayList stemmedHeads = new ArrayList();
		List headReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.HEAD );
		if ( headReferences.size() > 0 ){
			reference = (DocumentReference)headReferences.get( 0 );
		}
		String sStem = m_analyser.getStemmedText( reference );
		stemmedHeads.add( sStem );
		return stemmedHeads;
	}

	private List<DocumentReference> getNounPhrasePointers( AbstractDocument doc ) {
		return m_analyser.getDocumentReferences( doc, LinguisticAnalyser.NOUN_PHRASE );
	}

	private POMSubclassOfRelation getRelation( List heads ) throws AnalyserException {
		String sDomain = heads.toString();
		int iEnd = sDomain.length() - 1;
		sDomain = sDomain.substring( 1, iEnd );
		int iIndexLastWord = sDomain.lastIndexOf( " " ) + 1;
		if ( iIndexLastWord == -1 ) {
			return null;
		}
		String sRange = sDomain.substring( iIndexLastWord );
		if ( !sRange.equals( sDomain ) ) 
		{
			POMConcept domain = m_pom.newConcept( sDomain );
			POMConcept range = m_pom.newConcept( sRange );
			POMSubclassOfRelation subclassOfRelation = m_pom.newSubclassOfRelation( domain, range );
			return subclassOfRelation;
		}
		return null;
	}
}