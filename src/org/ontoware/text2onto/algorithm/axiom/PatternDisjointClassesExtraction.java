package org.ontoware.text2onto.algorithm.axiom;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ontoware.text2onto.change.*;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser; 
import org.ontoware.text2onto.pom.POMObject; 
import org.ontoware.text2onto.pom.POMEntity; 
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMDisjointClasses;
import org.ontoware.text2onto.reference.document.*;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.explanation.pattern.PatternRelationExplanation;
import org.ontoware.text2onto.evidence.reference.ReferenceStore; 
import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
import org.ontoware.text2onto.util.MyInteger;
import org.ontoware.text2onto.util.MyDouble;

/** 
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de) 
 */
public class PatternDisjointClassesExtraction extends AbstractSimpleAlgorithm {
  
	protected void initialize() { 
		m_algorithmController.requestLocalEvidenceStore( this, ReferenceStore.class ); 
	}
	 
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		List<ChangeRequest> evidenceChangeRequests= new ArrayList<ChangeRequest>();
		List<Change> corpusChanges = m_corpus.getChangesFor(this);
		
		for( Change change: corpusChanges )
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			System.out.println( "PatternDisjointClassesExtraction.getEvidenceChanges: "+ doc );
			
			List<DocumentReference> disjointReferences = m_analyser.getDocumentReferences( doc, LinguisticAnalyser.DISJOINT_CLASSES );  
			for( DocumentReference reference: disjointReferences )
			{ 
				List<POMDisjointClasses> disjoints = getDisjointClasses( reference );
				for( POMDisjointClasses disjoint: disjoints )
				{ 
					if( iChange == Change.Type.ADD ) 
					{
						evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.ADD, 
					 		this, getLocalEvidenceStore( ReferenceStore.class ), disjoint, reference, change ) ) ); 
					} 
					else if( iChange == Change.Type.REMOVE ) 
					{
						evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE, 
							this, getLocalEvidenceStore( ReferenceStore.class ), disjoint, reference, change ) ) ); 
					}	
				}  
			}  
		} 
		return evidenceChangeRequests;
	}
	
	protected AbstractExplanation getExplanation( POMChange change ) {
		Object object = change.getObject();
		if( !( object instanceof POMDisjointClasses ) ){
			return null;
		}
		POMDisjointClasses disjoint = (POMDisjointClasses)object;
		PatternRelationExplanation explanation = new PatternRelationExplanation( disjoint, this, change );
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
		
		HashMap<POMDisjointClasses,MyDouble> hmDisjoint2Prob = getProbabilities( objects );
		for( Object object: hmDisjoint2Prob.keySet() )
		{
			POMDisjointClasses disjoint = (POMDisjointClasses)object; 
			List<Change> changes = referenceStore.getChangesFor( this, object ); 
			MyDouble dProbability = (MyDouble)hmDisjoint2Prob.get( disjoint );
			double dProb = 0.0;
			if( dProbability != null ){
				dProb = dProbability.getValue();
			}
			disjoint.setProbability( dProb );
			if( dProb == 0.0 ){
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, disjoint, dProb, changes ) ) );
			}
			else {
				pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, disjoint, dProb, changes ) ) );
			} 			 
		}
		return pomChangeRequests;
	}
	
	/* protected HashMap<POMDisjointClasses,MyDouble> getProbabilities( List<Object> objects ){
		HashMap<POMDisjointClasses,MyDouble> hmDisjoint2Prob = new HashMap<POMDisjointClasses,MyDouble>();
		HashMap<POMDisjointClasses,List<Tuple>> hmDisjoint2Tuples = new HashMap<POMDisjointClasses,List<Tuple>>(); 
		HashMap<Tuple,MyDouble> hmTuple2Freq = new HashMap<Tuple,MyDouble>();
		// compute tuple frequencies
		for( Object object: objects )
		{
			POMDisjointClasses disjoint = (POMDisjointClasses)object; 
			List<Tuple> tuples = getTuples( disjoint );	
			hmDisjoint2Tuples.put( disjoint, tuples );		
			for( Tuple tuple: tuples )
			{ 
				MyDouble dFreq = (MyDouble)hmTuple2Freq.get( tuple );
				if( dFreq == null ){
					dFreq = new MyDouble(0);
					hmTuple2Freq.put( tuple, dFreq );
				}
				dFreq.increase(); 
			}
		}  
		// normalize tuple frequencies
		for( Tuple tuple: hmTuple2Freq.keySet() )
		{
			double dFreq = ((MyDouble)hmTuple2Freq.get( tuple )).getValue();
			hmTuple2Freq.put( tuple, new MyDouble( dFreq / (double)hmTuple2Freq.size() ) );
		} 
		// compute probabilities (sum of normalized tuple frequencies)
		for( Object object: objects )
		{
			POMDisjointClasses disjoint = (POMDisjointClasses)object; 
			List<Tuple> tuples = hmDisjoint2Tuples.get( disjoint );
			for( Tuple tuple: tuples )
			{  
				MyDouble dFreq = (MyDouble)hmTuple2Freq.get( tuple );
				if( dFreq != null )
				{
					MyDouble dProb = (MyDouble)hmDisjoint2Prob.get( disjoint );
					if( dProb == null ){
						dProb = new MyDouble(0);
						hmDisjoint2Prob.put( disjoint, dProb );
					}
					dProb.increase( (double)dFreq.getValue() ); 
				}
			}
		} 
		// normalize probabilities
		for( Object object: objects )
		{
			POMDisjointClasses disjoint = (POMDisjointClasses)object; 
			int iTuples = getTuples( disjoint ).size();
			double dProb = ((MyDouble)hmDisjoint2Prob.get( disjoint )).getValue();
			hmDisjoint2Prob.put( disjoint, new MyDouble( dProb / (double)iTuples ) );
		} 
		return hmDisjoint2Prob;
	} */
	
	protected HashMap<POMDisjointClasses,MyDouble> getProbabilities( List<Object> objects ){
		HashMap<POMDisjointClasses,MyDouble> hmDisjoint2Prob = new HashMap<POMDisjointClasses,MyDouble>(); 
		HashMap<Tuple,MyDouble> hmTuple2Freq = new HashMap<Tuple,MyDouble>();
		// compute tuple frequencies 
		int iNorm = 0;
		for( Object object: objects )
		{
			POMDisjointClasses disjoint = (POMDisjointClasses)object; 
			List<Tuple> tuples = getTuples( disjoint );
			iNorm += tuples.size();	 	
			for( Tuple tuple: tuples )
			{ 
				MyDouble dFreq = (MyDouble)hmTuple2Freq.get( tuple );
				if( dFreq == null ){
					dFreq = new MyDouble(0);
					hmTuple2Freq.put( tuple, dFreq );
				}
				dFreq.increase(); 
			}
		}  
		// normalize tuple frequencies
		for( Tuple tuple: hmTuple2Freq.keySet() )
		{
			double dFreq = ((MyDouble)hmTuple2Freq.get( tuple )).getValue();
			hmTuple2Freq.put( tuple, new MyDouble( dFreq / (double)iNorm ) );
		}  
		// compute probabilities
		for( Tuple tuple: hmTuple2Freq.keySet() )
		{
			MyDouble dFreq = (MyDouble)hmTuple2Freq.get( tuple );
			List<POMConcept> classes = new ArrayList<POMConcept>();
			classes.add( tuple.getFirst() );
			classes.add( tuple.getSecond() );
			POMDisjointClasses disjoint = m_pom.newDisjointClasses( classes );
			hmDisjoint2Prob.put( disjoint, dFreq );		
		}  
		return hmDisjoint2Prob;
	}
	
	private List<Tuple> getTuples( POMDisjointClasses disjoint ){
		List<Tuple> tuples = new ArrayList<Tuple>();
		Set<POMConcept> concepts = disjoint.getConcepts();
		for( POMConcept concept1: concepts ){
			for( POMConcept concept2: concepts ){
				if( !concept1.equals( concept2 ) )
				{
					Tuple tuple = new Tuple( concept1, concept2 );
					if( !tuples.contains( tuple ) ){
						tuples.add( tuple ); 
					}
				}
			}
		}
		return tuples;
	}
	 
	private void addChanges( ArrayList alSource, ArrayList alTarget ) {
		for ( int i = 0; i < alSource.size(); i++ )
		{
			Change change = (Change)alSource.get( i );
			if ( !alTarget.contains( change ) ){
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

	protected List<POMConcept> getConcepts( DocumentReference reference ) throws AnalyserException {
		List<POMConcept> concepts = new ArrayList<POMConcept>();
		List<DocumentReference> conceptReferences = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.CONCEPT );
		List<String> heads = getStemmedHeads( conceptReferences );
		for( String sHead: heads )
		{
			POMConcept concept = m_pom.newConcept( sHead );
			if( !concepts.contains( concept ) ){
				concepts.add( concept );
			}
		}
		return concepts;
	} 
	
	protected List<POMDisjointClasses> getDisjointClasses( DocumentReference reference ) throws AnalyserException {
		List<POMDisjointClasses> disjoints = new ArrayList<POMDisjointClasses>();
		List<POMConcept> concepts = getConcepts( reference );
		if( concepts.size() > 1 ){ 
			disjoints.add( m_pom.newDisjointClasses( concepts ) );
		}
		return disjoints; 
	}
	
	/* protected List<POMDisjointClasses> getDisjointClasses( DocumentReference reference ) throws AnalyserException {
		List<POMDisjointClasses> disjoints = new ArrayList<POMDisjointClasses>();
		List<POMConcept> concepts = getConcepts( reference );
		if( concepts.size() < 2 ){
			return disjoints;
		}
		for( POMConcept concept1: concepts ){
			for( POMConcept concept2: concepts ){
				if( !concept1.equals( concept2 ) )
				{
					List<POMConcept> classes = new ArrayList<POMConcept>();
					classes.add( concept1 );
					classes.add( concept2 );
					POMDisjointClasses disjoint = m_pom.newDisjointClasses( classes );
					disjoints.add( disjoint );
				}
			}
		}
		return disjoints;
	} */
	 
	private class Tuple {
		private POMConcept m_concept1;
		private POMConcept m_concept2;
		
		public Tuple( POMConcept concept1, POMConcept concept2 ){
			m_concept1 = concept1;
			m_concept2 = concept2;
		}
		public POMConcept getFirst(){
			return m_concept1;
		}
		public POMConcept getSecond(){
			return m_concept2;
		}
		public boolean equals( Object object ){
			if( object instanceof Tuple ){
				return ( ((Tuple)object).contains( m_concept1 ) 
					&& ((Tuple)object).contains( m_concept2 ) );
			}
			return false;
		}
		public boolean contains( POMConcept concept ){
			return( m_concept1.equals( concept ) 
				|| m_concept2.equals( concept ) );
		}
		public int hashCode(){
			return ( m_concept1.hashCode() + m_concept2.hashCode() );
		}
		public String toString(){
			return "("+ m_concept1 +","+ m_concept2 +")";
		}
	}
}