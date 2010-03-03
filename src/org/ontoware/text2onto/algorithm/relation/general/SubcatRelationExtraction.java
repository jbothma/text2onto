package org.ontoware.text2onto.algorithm.relation.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
 
import org.ontoware.text2onto.change.*;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.pom.POMConcept;
import org.ontoware.text2onto.pom.POMRelation;
import org.ontoware.text2onto.reference.document.*;
import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.evidence.subcat.*;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.explanation.reference.ReferenceExplanation;
import org.ontoware.text2onto.linguistic.LinguisticAnalyser;
import org.ontoware.text2onto.linguistic.AnalyserException;
import org.ontoware.text2onto.algorithm.AbstractSimpleAlgorithm;
 

/* 
 * @author Philipp Cimiano (pci@aifb.uni-karlsruhe.de)
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class SubcatRelationExtraction extends AbstractSimpleAlgorithm implements AbstractGeneralRelationExtraction {  
 
	protected void initialize() {
		m_algorithmController.requestLocalEvidenceStore( this, SubcatStore.class );
		m_algorithmController.requestLocalReferenceStore( this, DocumentReferenceStore.class );
	}
	
	protected List<ChangeRequest> getEvidenceChanges() throws Exception {
		List<ChangeRequest> evidenceChangeRequests= new ArrayList<ChangeRequest>();
		List<Change> corpusChanges = m_corpus.getChangesFor( this ); 

		for( Change change: corpusChanges )
		{
			int iChange = change.getType();
			AbstractDocument doc = (AbstractDocument)change.getObject();
			System.out.println( "AbstractEntityExtraction.getEvidenceChanges: "+ doc );
			
			DocumentReference reference;
 			SubcatFrameInstance instance = null;
 			SubcatStore subcatStore = (SubcatStore)getLocalEvidenceStore( SubcatStore.class );
 			/*
 			 * Transitive Verb Phrases  
 			 */
 			List DocumentReferences = m_analyser.getDocumentReferences( doc, LinguisticAnalyser.VERB_PHRASE_TRANSITIVE );
 			Iterator references = DocumentReferences.iterator();
 			while( references.hasNext() )
 			{
 				reference = (DocumentReference) references.next();
 				// System.out.println( "Matched: "+ m_analyser.getText( reference ) ); 
		 
				// sVerb = "love";
				// sSubject = "man";
				// sObject = "woman";
				
				List verbs = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.VERB );
				List subjects = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.SUBJECT );
				List objects = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.OBJECT );
			 				 
				String sVerb = m_analyser.getStemmedText( (DocumentReference)verbs.get(0) );
				String sSubject = getStemmedHead( (DocumentReference)subjects.get(0) );
				String sObject = getStemmedHead( (DocumentReference)objects.get(0) );	
				
				// TODO
				if( sVerb.equals( "be" ) ){
					continue;
				}
				// System.out.println( "Verb="+ sVerb +", Subject="+ sSubject +", Object="+ sObject );		
				
				instance = new SubcatFrameInstance( SubcatFrameInstance.TRANSITIVE, sVerb );
				instance.setArgument( SubcatFrameInstance.SUBJECT, sSubject );
				instance.setArgument( SubcatFrameInstance.OBJECT, sObject );
				if( iChange == Change.Type.ADD )
				{ 
					evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.ADD, this, subcatStore, instance, reference, change ) ) ); 
				} 
				else if( iChange == Change.Type.REMOVE )
				{ 
					evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE, this, subcatStore, instance, reference, change ) ) ); 
				} 
 			}
 			/*
 			 * Intransitive+PP Verb Phrases 
			 */
			DocumentReferences = m_analyser.getDocumentReferences( doc, LinguisticAnalyser.VERB_PHRASE_INTRANSITIVE_PP );
			references = DocumentReferences.iterator();
			while( references.hasNext() ) 
			{
				reference = (DocumentReference) references.next();
				// System.out.println( "Matched: "+ reference ); 
			 
				// String sVerb = "go";
				// String sSubject = "man";
				// String sPObject = "bed";
				// String sPreposition = "to";
				
				List verbs = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.VERB );
				List subjects = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.SUBJECT );
				List pobjects = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.POBJECT );
				List preps = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.PREPOSITION );
			 	
			 	// System.out.println( "SRE: "+ verbs + subjects + pobjects + preps );			 
			 	
				String sVerb = m_analyser.getStemmedText( (DocumentReference)verbs.get(0) );
				String sSubject = getStemmedHead( (DocumentReference)subjects.get(0) );
				String sPObject = getStemmedHead( (DocumentReference)pobjects.get(0) );	
				String sPreposition = m_analyser.getText( (DocumentReference)preps.get(0) );
				
				// TODO
				if( sVerb.equals( "be" ) ){
					continue;
				}
				// System.out.println( "Verb="+ sVerb +", Subject="+ sSubject +", PObject="+ sPObject +", Preposition="+ sPreposition );					 							
				instance = new SubcatFrameInstance( SubcatFrameInstance.INTRANSITIVE_PP, sVerb );
				instance.setArgument( SubcatFrameInstance.SUBJECT, sSubject );
				instance.setArgument( SubcatFrameInstance.POBJECT, sPObject );
				instance.setArgument( SubcatFrameInstance.PREPOSITION, sPreposition );
				if( iChange == Change.Type.ADD )
				{ 
					evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.ADD, this, subcatStore, instance, reference, change ) ) ); 
				} 
				else if( iChange == Change.Type.REMOVE )
				{ 
					evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE, this, subcatStore, instance, reference, change ) ) ); 
				} 
			}
			/*
			 * Transitive+PP Verb Phrases
			 */
			DocumentReferences = m_analyser.getDocumentReferences( doc, LinguisticAnalyser.VERB_PHRASE_TRANSITIVE_PP );
			references = DocumentReferences.iterator();
			while( references.hasNext() )
			{
				reference = (DocumentReference) references.next();
				// System.out.println( "Matched: "+ reference ); 
				 
				// String sVerb ="surprise";
				// String sSubject = "man";
				// String sObject = "woman";
				// String sPObject = "present";
				// String sPreposition = "with";
				
				List verbs = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.VERB );
				List subjects = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.SUBJECT );
				List objects = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.OBJECT );
				List pobjects = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.POBJECT );
				List preps = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.PREPOSITION );
			 				 
				String sVerb = m_analyser.getStemmedText( (DocumentReference)verbs.get(0) );
				String sSubject = getStemmedHead( (DocumentReference)subjects.get(0) );
				String sObject = getStemmedHead( (DocumentReference)objects.get(0) );
				String sPObject = getStemmedHead( (DocumentReference)pobjects.get(0) );	
				String sPreposition = m_analyser.getText( (DocumentReference)preps.get(0) );
				
				// TODO
				if( sVerb.equals( "be" ) ){
					continue;
				}
				// System.out.println( "Verb="+ sVerb +", Subject="+ sSubject +", Object="+ sObject +", PObject="+ sPObject +", Preposition="+ sPreposition );
				
				instance = new SubcatFrameInstance( SubcatFrameInstance.TRANSITIVE_PP, sVerb );
				instance.setArgument( SubcatFrameInstance.SUBJECT, sSubject );
				instance.setArgument( SubcatFrameInstance.OBJECT, sObject );
				instance.setArgument( SubcatFrameInstance.POBJECT, sPObject );
				instance.setArgument( SubcatFrameInstance.PREPOSITION, sPreposition );
				if( iChange == Change.Type.ADD )
				{ 
					evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.ADD, this, subcatStore, instance, reference, change ) ) ); 
				} 
				else if( iChange == Change.Type.REMOVE )
				{ 
					evidenceChangeRequests.add( new ChangeRequest( new EvidenceChange( Change.Type.REMOVE, this, subcatStore, instance, reference, change ) ) ); 
				} 
			}
		}   
		return evidenceChangeRequests;
	}			

	protected AbstractExplanation getExplanation( POMChange change ) {
		Object object = change.getObject();
		if( !( object instanceof POMRelation ) )
		{
			return null;
		}
		POMRelation relation = (POMRelation)object;
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
		SubcatStore subcatStore = (SubcatStore)getLocalEvidenceStore( SubcatStore.class );
		List<Change> evidenceChanges = subcatStore.getChangesFor( this ); 
		for( Change change: evidenceChanges )
		{
			int iChange = change.getType();
			SubcatFrameInstance instance = (SubcatFrameInstance)change.getObject(); 
			List<SubcatFrame> frames = subcatStore.getSubcatFrames( instance );
			for( SubcatFrame frame: frames )
			{
				String sDomain = frame.getMostFrequent( SubcatFrame.DOMAIN );
				int iDomain = frame.getFrequency( SubcatFrame.DOMAIN, sDomain );
				String sRange = frame.getMostFrequent( SubcatFrame.RANGE );
				int iRange = frame.getFrequency( SubcatFrame.RANGE, sRange );
 				int iInstances = frame.getInstances();
 					
 				POMConcept domain = m_pom.newConcept( sDomain );
 				POMConcept range = m_pom.newConcept( sRange );
 				POMRelation relation = m_pom.newRelation( frame.getRelation(), domain, range ); 
 				double dProb = ( (double)iDomain / (double)iInstances ) * ( (double)iRange / (double)iInstances );
 				relation.setProbability( dProb ); 
 				
 				if( iChange == Change.Type.ADD ){
			 		pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, relation, new Double( dProb ), change ) ) );
				}
				else if( iChange == Change.Type.REMOVE ){
					pomChangeRequests.add( new ChangeRequest( new POMChange( Change.Type.REMOVE, this, relation, new Double( dProb ), change ) ) );
				} 
			}
		}
		return pomChangeRequests;
	}
   
	private String getStemmedHead( DocumentReference reference ) throws AnalyserException { 
		List headPointers = m_analyser.getDocumentReferences( reference, LinguisticAnalyser.HEAD );
		if( headPointers.size() > 0 ){
			reference = (DocumentReference)headPointers.get(0);
		} 
		return m_analyser.getStemmedText( reference ); 
	}
}
