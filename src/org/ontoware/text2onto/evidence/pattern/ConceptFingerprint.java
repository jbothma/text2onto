package org.ontoware.text2onto.evidence.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.reference.document.DocumentReference;
 
 
public class ConceptFingerprint extends Fingerprint {

	private ArrayList<POMInstance> m_alInstances;

	private HashMap<String,ArrayList<POMConcept>> m_hmLabel2RelationDomains;

	private HashMap<String,ArrayList<POMConcept>> m_hmLabel2RelationRanges;

	private ArrayList<POMConcept> m_alSubConcepts;

	private ArrayList<POMConcept> m_alSuperConcepts;


	public ConceptFingerprint(){
		m_alSubConcepts = new ArrayList<POMConcept>();
		m_alSuperConcepts = new ArrayList<POMConcept>();
		m_alInstances = new ArrayList<POMInstance>();
		m_hmLabel2RelationDomains = new HashMap<String,ArrayList<POMConcept>>();
		m_hmLabel2RelationRanges = new HashMap<String,ArrayList<POMConcept>>();
	}

	public void addRelationDomain( String sRelationLabel, POMConcept concept ){
		ArrayList alDomains = (ArrayList)m_hmLabel2RelationDomains.get( sRelationLabel );
		if( alDomains == null )
		{
			alDomains = new ArrayList();
			m_hmLabel2RelationDomains.put( sRelationLabel, alDomains );
		}
		if( !alDomains.contains( concept ) ){
			alDomains.add( concept );
		}
	}

	public void addRelationRange( String sRelationLabel, POMConcept concept ){
		ArrayList alRanges = (ArrayList)m_hmLabel2RelationRanges.get( sRelationLabel );
		if( alRanges == null )
		{
			alRanges = new ArrayList();
			m_hmLabel2RelationRanges.put( sRelationLabel, alRanges );
		}
		if ( !alRanges.contains( concept ) ){
			alRanges.add( concept );
		}
    }

	public void addInstance( POMInstance instance ){
		if( !m_alInstances.contains( instance ) ){
			m_alInstances.add( instance );
		}
	}

	public void addSubConcept( POMConcept concept ){
		if( !m_alSubConcepts.contains( concept ) ){
			m_alSubConcepts.add( concept );
		}
	}

	public void addSuperConcept( POMConcept concept  ){
		if( !m_alSuperConcepts.contains( concept ) ){
			m_alSuperConcepts.add( concept );
		}
	}

	public List getRelationDomains( String sRelation ){
		return (List)m_hmLabel2RelationDomains.get( sRelation );
	}

	public List getRelationRanges( String sRelation ){
		return (List)m_hmLabel2RelationRanges.get( sRelation );
	}

	public ArrayList getInstances(){
		return m_alInstances;
	}

	public ArrayList getSubConcepts(){
		return m_alSubConcepts;
	}

	public ArrayList getSuperConcepts(){
		return m_alSuperConcepts;
	}

	public void removeRelationDomain( String sRelation, POMConcept concept ){
		getRelationDomains( sRelation ).remove( concept );
	}

	public void removeRelationRange( String sRelation, POMConcept concept ){
		getRelationRanges( sRelation ).remove( concept );
	}

	public void removeInstance( POMInstance instance ){
		m_alInstances.remove( instance );
	}

	public void removeSubConcept( POMConcept concept ){
		m_alSubConcepts.remove( concept );
	}

	public void removeSuperConcept( POMConcept concept ){
		m_alSuperConcepts.remove( concept );
	}

	public String toString(){
		String s = "superConcepts: "+ m_alSuperConcepts;
		s += "\nsubConcepts: "+ m_alSubConcepts;
		s += "\ninstances: "+ m_alInstances;
		s += "\nrelationDomains: "+ m_hmLabel2RelationDomains;
		s += "\nrelationRanges: "+ m_hmLabel2RelationRanges;
		return s;
	}
}