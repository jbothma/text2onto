package org.ontoware.text2onto.algorithm.taxonomic.instanceOf;

import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.pom.POMInstance; 
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMEntity;
import org.ontoware.text2onto.pom.POMAbstractRelation;
import org.ontoware.text2onto.pom.POMTaxonomicRelation;
import org.ontoware.text2onto.pom.POMInstanceOfRelation;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.evidence.pattern.InstanceFingerprint;
import org.ontoware.text2onto.evidence.pattern.PatternStore;
import org.ontoware.text2onto.algorithm.taxonomic.AbstractPatternClassification; 

/* 
 * @author Simon Sparn (sparn@ontoprise.de)
 */
public class PatternInstanceClassification extends AbstractPatternClassification implements AbstractInstanceClassification {
    
   /**
    * returns subclass-of relations with same domain as relation 
    */
	protected ArrayList getDomainRelations( POMTaxonomicRelation relation ) { 
		ArrayList alDomainRelations = new ArrayList();
		POMInstance domain = (POMInstance)relation.getDomain();
		InstanceFingerprint fingerprint = ((PatternStore)getLocalEvidenceStore( PatternStore.class )).getFingerprint( domain );
		List<POMConcept> concepts = (ArrayList)fingerprint.getConcepts();
		for( POMConcept concept: concepts )
		{ 
			POMInstanceOfRelation instanceOfRelation = m_pom.newInstanceOfRelation( domain, concept ); 
			alDomainRelations.add( instanceOfRelation );
		}
		return alDomainRelations;
	}
 
	protected List getRelationReferences( AbstractDocument doc ) {
		return m_analyser.getDocumentReferences( doc, LinguisticAnalyser.INSTANCE_OF );
	}
 
	protected List getRelations( DocumentReference reference ) throws AnalyserException { 
		ArrayList alRelations = new ArrayList();
		List alDomainStrings = getDomainStrings( reference );
		List alRangeStrings = getRangeStrings( reference );
		for ( int j = 0; j < alDomainStrings.size(); j++ )
		{
			POMInstance instance = m_pom.newInstance( (String)alDomainStrings.get( j ) );
			for( int k = 0; k < alRangeStrings.size(); k++ )
			{
				POMConcept range = m_pom.newConcept( (String)alRangeStrings.get( k ) );
				POMInstanceOfRelation instanceOfRelation = m_pom.newInstanceOfRelation( instance, range ); 
				alRelations.add( instanceOfRelation );
			}
		}
		return alRelations;
	}
	
	protected List<POMTaxonomicRelation> getRelations( List<POMEntity> domains ){
		List<POMTaxonomicRelation> relations = new ArrayList<POMTaxonomicRelation>();
		List<POMAbstractRelation> rels = m_pom.getObjects( POMInstanceOfRelation.class );
		for( POMAbstractRelation rel: rels )
		{
			if( domains.contains( rel.getDomain() ) ){
				relations.add( (POMInstanceOfRelation)rel );
			}
		}
		return relations;
	}
	
	protected Double getProbability( int iFreq, int iAllFreq ) { 
		return new Double( (double)iFreq / (double)iAllFreq );
	}
}