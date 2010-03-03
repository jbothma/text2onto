package org.ontoware.text2onto.evidence.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.Changeable;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.evidence.AbstractEvidenceStore;
import org.ontoware.text2onto.evidence.reference.ReferenceStore;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.pom.*;


public class PatternStore extends AbstractEvidenceStore {
 
	private HashMap<POMConcept,ConceptFingerprint> m_hmConcept2Fingerprint;
	
	private HashMap<POMInstance,InstanceFingerprint> m_hmInstance2Fingerprint;
	
	private ArrayList<POMAbstractRelation> m_alRelations;
	

	public PatternStore(){
		m_hmConcept2Fingerprint = new HashMap<POMConcept,ConceptFingerprint>();
		m_hmInstance2Fingerprint = new HashMap<POMInstance,InstanceFingerprint>();
		m_alRelations = new ArrayList<POMAbstractRelation>();
	}
	 
	public ConceptFingerprint getFingerprint( POMConcept concept ){
		return (ConceptFingerprint)m_hmConcept2Fingerprint.get( concept );
	}
	
	public InstanceFingerprint getFingerprint( POMInstance instance ){
		return (InstanceFingerprint)m_hmInstance2Fingerprint.get( instance );
	}
   
	private void add2Fingerprints( POMAbstractRelation relation ){ 
		POMEntity domain = relation.getDomain();
		POMEntity range = relation.getRange();
		String sRelation = relation.getLabel();
		if( relation instanceof POMSubclassOfRelation || relation instanceof POMRelation )
		{
			ConceptFingerprint fpDomain = getFingerprint( (POMConcept)domain );
			ConceptFingerprint fpRange = getFingerprint( (POMConcept)range );
			if( fpDomain == null )
			{
				fpDomain = new ConceptFingerprint();
				m_hmConcept2Fingerprint.put( (POMConcept)domain, fpDomain );
			}
			if( fpRange == null )
			{
				fpRange = new ConceptFingerprint();
				m_hmConcept2Fingerprint.put( (POMConcept)range, fpRange );	
			}
			if( relation instanceof POMSubclassOfRelation )
			{
				fpDomain.addSuperConcept( (POMConcept)range );
				fpRange.addSubConcept( (POMConcept)domain );
			}
			else if( relation instanceof POMRelation )
			{
				fpDomain.addRelationRange( sRelation, (POMConcept)range );
				fpRange.addRelationDomain( sRelation, (POMConcept)domain );
			}
		}
		else if( relation instanceof POMInstanceOfRelation )
		{
			InstanceFingerprint fpDomain = getFingerprint( (POMInstance)domain );
			ConceptFingerprint fpRange = getFingerprint( (POMConcept)range );
			if( fpDomain == null )
			{
				fpDomain = new InstanceFingerprint();
				m_hmInstance2Fingerprint.put( (POMInstance)domain, fpDomain );
			}
			if( fpRange == null )
			{
				fpRange = new ConceptFingerprint();
				m_hmConcept2Fingerprint.put( (POMConcept)range, fpRange );
			}
			fpDomain.addConcept( (POMConcept)range );
			fpRange.addInstance( (POMInstance)domain );
		}
		else if( relation instanceof POMRelationInstance )
		{
			InstanceFingerprint fpDomain = getFingerprint( (POMInstance)domain );
			InstanceFingerprint fpRange = getFingerprint( (POMInstance)range ); 
			if( fpDomain == null )
			{
				fpDomain = new InstanceFingerprint();
				m_hmInstance2Fingerprint.put( (POMInstance)domain, fpDomain );
			}
			if( fpRange == null )
			{
				fpRange = new InstanceFingerprint();
				m_hmInstance2Fingerprint.put( (POMInstance)range, fpRange );
			}
			fpDomain.addRelationRange( sRelation, (POMInstance)range );
			fpRange.addRelationDomain( sRelation, (POMInstance)domain );
		}
		// TODO: similarity
	}
 
	/* Changeable */

	protected Change createChange( ChangeRequest changeRequest ) {
		Change c;
		if( changeRequest.getType() != Change.Type.REMOVE )
		{
			POMAbstractRelation relation = (POMAbstractRelation)changeRequest.getObject();
			if( m_alRelations.contains( relation ) ){
				c = changeRequest.createChangeWithType( Change.Type.MODIFY );
			} 
			else {
				c = changeRequest.createChangeWithType( Change.Type.ADD );
			}
		} else {
			c = changeRequest.createChangeWithType( Change.Type.REMOVE );
		}
		return c;
	}

	protected void executeChange( Change change ){ 
		switch( change.getType() )
		{
			case Change.Type.ADD: 
				executeAdd( change ); 
				break;
			case Change.Type.REMOVE: 
				executeRemove( change ); 
				break;
			case Change.Type.MODIFY: 
				executeModify( change ); 
				break;
		}	
	} 

	protected void executeAdd( Change change ){
		POMAbstractRelation relation = (POMAbstractRelation)change.getObject(); 
		add2Fingerprints( relation ); 
	}
	
	protected void executeRemove( Change change ){
		// TODO
	}
	
	protected void executeModify( Change change ){
		// TODO
	}
}