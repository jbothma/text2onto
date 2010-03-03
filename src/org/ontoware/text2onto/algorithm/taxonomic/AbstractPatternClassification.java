package org.ontoware.text2onto.algorithm.taxonomic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ontoware.text2onto.change.*;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser; 
import org.ontoware.text2onto.pom.POMObject; 
import org.ontoware.text2onto.pom.POMEntity; 
import org.ontoware.text2onto.pom.POMTaxonomicRelation;
import org.ontoware.text2onto.reference.document.*;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.explanation.pattern.PatternRelationExplanation;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.evidence.pattern.PatternStore; 
import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;

/** 
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 * @author Simon Sparn (sparn@ontoprise.de)
 */
public abstract class AbstractPatternClassification extends AbstractSimpleAlgorithm implements AbstractClassification {
  
	protected void initialize() {
		m_algorithmController.requestLocalEvidenceStore( this, PatternStore.class );
		m_algorithmController.requestLocalEvidenceStore( this, ReferenceStore.class );
		m_algorithmController.requestLocalReferenceStore( this, DocumentReferenceStore.class );
	}
	 
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		List<ChangeRequest> evidenceChangeRequests= new ArrayList<ChangeRequest>();
		List<Change> corpusChanges = m_corpus.getChangesFor(this);
		
		for( Change change: corpusChanges )
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			System.out.println( "AbstractPatternClassification.getEvidenceChanges: "+ doc );
			
			List<DocumentReference> relationReferences = getRelationReferences( doc );  
			for( DocumentReference reference: relationReferences )
			{ 
				List<POMTaxonomicRelation> relations = getRelations( reference );
				for( POMTaxonomicRelation relation: relations )
				{ 
					if( iChange == Change.Type.ADD ) 
					{
						evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.ADD, 
							this, getLocalEvidenceStore( ReferenceStore.class ), relation, reference, change ) ) );
						evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.ADD,
							this, getLocalEvidenceStore( PatternStore.class ), relation, change ) ) );
					} 
					else if( iChange == Change.Type.REMOVE ) 
					{
						evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE, 
							this, getLocalEvidenceStore( ReferenceStore.class ), relation, reference, change ) ) ); 
						evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE,
							this, getLocalEvidenceStore( PatternStore.class ), relation, change ) ) );
					} 
				} 
			}  
		} 
		return evidenceChangeRequests;
	}
	
	protected AbstractExplanation getExplanation( POMChange change ) {
		Object object = change.getObject();
		if( !( object instanceof POMTaxonomicRelation ) ){
			return null;
		}
		POMTaxonomicRelation relation = (POMTaxonomicRelation)object;
		PatternRelationExplanation explanation = new PatternRelationExplanation( relation, this, change );
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
	
	protected List<ChangeRequest> getPOMChanges(){
		List<ChangeRequest> pomChangeRequests = new ArrayList<ChangeRequest>();
		ReferenceStore referenceStore = (ReferenceStore)getLocalEvidenceStore( ReferenceStore.class );
		List<Object> objects = new ArrayList<Object>();
		objects.addAll( referenceStore.getChangedObjectsFor( this ) );
		
		List<POMEntity> domains = getDomains( objects );		
		List<POMTaxonomicRelation> relations = getRelations( domains );
		
		for( POMTaxonomicRelation relation: relations )
		{
			if( !objects.contains( relation ) ){
				objects.add( relation );
			}
		}		
		for( Object object: objects )
		{
			POMTaxonomicRelation relation = (POMTaxonomicRelation)object;
			int iAllFreq = countDomainRelationReferences( relation );
			List<Change> changes = referenceStore.getChangesFor( this, object );
			
			int iFreq = ((ReferenceStore)getLocalEvidenceStore( ReferenceStore.class )).getReferences( relation ).size(); 
			Double dProb = getProbability( iFreq, iAllFreq );
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
	
	private List<POMEntity> getDomains( List<Object> relations ){
		List<POMEntity> domains = new ArrayList<POMEntity>();
		for( Object relation: relations )
		{
			POMEntity domain = ((POMTaxonomicRelation)relation).getDomain();
			if( !domains.contains( domain ) ){
				domains.add( domain );
			}
		}
		return domains;
	}
	
	protected abstract List<POMTaxonomicRelation> getRelations( List<POMEntity> domains );
	
	protected int countDomainRelationReferences( POMTaxonomicRelation relation ) {
		int iReferences = 0; 
		List<POMTaxonomicRelation> domainRelations = getDomainRelations( relation );
		for( POMTaxonomicRelation domainRelation: domainRelations )
		{  
			iReferences += ((ReferenceStore)getLocalEvidenceStore( ReferenceStore.class )).getReferences( domainRelation ).size();
		}
		return iReferences;
	}
 
	private void addChanges( ArrayList alSource, ArrayList alTarget ) {
		for ( int i = 0; i < alSource.size(); i++ )
		{
			Change change = (Change)alSource.get( i );
			if ( !alTarget.contains( change ) )
			{
				alTarget.add( (Change)alSource.get( i ) );
			}
		}
	}
 
	private List<String> getStemmedHeads( List<DocumentReference> references ) throws AnalyserException {
		ArrayList stemmedHeads = new ArrayList();
		for( DocumentReference reference: references )
		{ 
			List<DocumentReference> headReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.HEAD );
			if( headReferences.size() > 0 ){
				reference = (DocumentReference)headReferences.get(0);
			} 
			String sStem = m_analyser.getStemmedText( reference );
			stemmedHeads.add( sStem );
		}
		return stemmedHeads;
	}

	protected List<String> getDomainStrings( DocumentReference reference ) throws AnalyserException {
		List<DocumentReference> domainReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.DOMAIN );
		return getStemmedHeads( domainReferences );
	}

	protected List<String> getRangeStrings( DocumentReference reference ) throws AnalyserException {
		List<DocumentReference> rangeReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.RANGE );
		return getStemmedHeads( rangeReferences );
	}	
	
	protected abstract Double getProbability( int iFreq, int iAllFreq );
	
	protected abstract List getRelationReferences( AbstractDocument doc );

	protected abstract List getRelations( DocumentReference reference ) throws AnalyserException;
 
	protected abstract ArrayList getDomainRelations( POMTaxonomicRelation relation );
}