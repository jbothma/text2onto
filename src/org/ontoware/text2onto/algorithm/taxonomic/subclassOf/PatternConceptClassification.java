package org.ontoware.text2onto.algorithm.taxonomic.subclassOf;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMAbstractRelation;
import org.ontoware.text2onto.pom.POMConcept; 
import org.ontoware.text2onto.pom.POMTaxonomicRelation;
import org.ontoware.text2onto.pom.POMSubclassOfRelation;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.evidence.pattern.ConceptFingerprint;
import org.ontoware.text2onto.evidence.pattern.PatternStore;
import org.ontoware.text2onto.algorithm.taxonomic.AbstractPatternClassification; 

/* 
 * @author Simon Sparn (sparn@ontoprise.de)
 */
public class PatternConceptClassification extends AbstractPatternClassification implements AbstractConceptClassification {
    
   /**
    * returns subclass-of relations with same domain as relation 
    */
	protected ArrayList getDomainRelations( POMTaxonomicRelation relation ) { 
		ArrayList alDomainRelations = new ArrayList();
		POMConcept domain = (POMConcept)relation.getDomain();
		ConceptFingerprint fingerprint = ((PatternStore)getLocalEvidenceStore( PatternStore.class )).getFingerprint( domain );
		List<POMConcept> superConcepts = (ArrayList)fingerprint.getSuperConcepts();
		for( POMConcept superConcept: superConcepts )
		{ 
			POMSubclassOfRelation subclassOfRelation = m_pom.newSubclassOfRelation( domain, superConcept ); 
			alDomainRelations.add( subclassOfRelation );
		}
		return alDomainRelations;
	}
 
	protected List getRelationReferences( AbstractDocument doc ) {
		return m_analyser.getDocumentReferences( doc, LinguisticAnalyser.SUBCLASS_OF );
	}
 
	protected List getRelations( DocumentReference reference ) throws AnalyserException { 
		ArrayList alRelations = new ArrayList();
		List alDomainStrings = getDomainStrings( reference );
		List alRangeStrings = getRangeStrings( reference );
		for ( int j = 0; j < alDomainStrings.size(); j++ )
		{
			POMConcept concept = m_pom.newConcept( (String)alDomainStrings.get( j ) );
			for ( int k = 0; k < alRangeStrings.size(); k++ )
			{
				POMConcept range = m_pom.newConcept( (String)alRangeStrings.get( k ) );
				if( range.getLabel().equals( concept.getLabel() ) ){
					continue;
				}
				POMSubclassOfRelation subclassOfRelation = m_pom.newSubclassOfRelation( concept, range ); 
				alRelations.add( subclassOfRelation );
			}
		}
		return alRelations;
	}
	
	protected List<POMTaxonomicRelation> getRelations( List<POMEntity> domains ){
		List<POMTaxonomicRelation> relations = new ArrayList<POMTaxonomicRelation>();
		List<POMAbstractRelation> rels = m_pom.getObjects( POMSubclassOfRelation.class );
		for( POMAbstractRelation rel: rels )
		{
			if( domains.contains( rel.getDomain() ) ){
				relations.add( (POMSubclassOfRelation)rel );
			}
		}
		return relations;
	}
	
	protected Double getProbability( int iFreq, int iAllFreq ) { 
		return new Double( (double)iFreq / (double)iAllFreq );
	}
}