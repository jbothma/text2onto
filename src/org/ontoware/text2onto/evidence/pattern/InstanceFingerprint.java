package org.ontoware.text2onto.evidence.pattern;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.reference.document.DocumentReference;
 
 
public class InstanceFingerprint extends Fingerprint {

	private ArrayList<POMConcept> m_alConcepts;

	private HashMap<String,ArrayList<POMInstance>> m_hmLabel2RelationDomains;

	private HashMap<String,ArrayList<POMInstance>> m_hmLabel2RelationRanges;
 

	public InstanceFingerprint(){
		m_alConcepts = new ArrayList<POMConcept>(); 
		m_hmLabel2RelationDomains = new HashMap<String,ArrayList<POMInstance>>();
		m_hmLabel2RelationRanges = new HashMap<String,ArrayList<POMInstance>>();
	}

	public void addRelationDomain( String sRelationLabel, POMInstance instance ){
		ArrayList alDomains = (ArrayList)m_hmLabel2RelationDomains.get( sRelationLabel );
		if( alDomains == null )
		{
			alDomains = new ArrayList();
			m_hmLabel2RelationDomains.put( sRelationLabel, alDomains );
		}
		if( !alDomains.contains( instance ) ){
			alDomains.add( instance );
		}
	}

	public void addRelationRange( String sRelationLabel, POMInstance instance ){
		ArrayList alRanges = (ArrayList)m_hmLabel2RelationRanges.get( sRelationLabel );
		if( alRanges == null )
		{
			alRanges = new ArrayList();
			m_hmLabel2RelationRanges.put( sRelationLabel, alRanges );
		}
		if ( !alRanges.contains( instance ) ){
			alRanges.add( instance );
		}
    }

	public void addConcept( POMConcept concept ){
		if( !m_alConcepts.contains( concept ) ){
			m_alConcepts.add( concept );
		}
	}

	public List getRelationDomains( String sRelation ){
		return (List)m_hmLabel2RelationDomains.get( sRelation );
	}

	public List getRelationRanges( String sRelation ){
		return (List)m_hmLabel2RelationRanges.get( sRelation );
	}

	public ArrayList getConcepts(){
		return m_alConcepts;
	}
 
	public void removeRelationDomain( String sRelation, POMInstance instance ){
		getRelationDomains( sRelation ).remove( instance );
	}

	public void removeRelationRange( String sRelation, POMInstance instance ){
		getRelationRanges( sRelation ).remove( instance );
	}

	public void removeConcept( POMConcept concept ){
		m_alConcepts.remove( concept );
	}
 
	public String toString(){
		String s = "concepts: "+ m_alConcepts; 
		s += "\nrelationDomains: "+ m_hmLabel2RelationDomains;
		s += "\nrelationRanges: "+ m_hmLabel2RelationRanges;
		return s;
	}
}