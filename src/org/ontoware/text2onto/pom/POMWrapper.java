package org.ontoware.text2onto.pom;

import java.util.List;

import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.ChangeableWrapper;

public class POMWrapper implements ChangeableWrapper {
	private POM m_pom;
	
	private int m_id;
	private static int m_pomIds = 0;
	
	private String m_name;
	
	public POMWrapper( POM pom ) {
		this( pom, ++m_pomIds, "POM" );
	}
	
	public POMWrapper( POM pom, int id, String name ) {
		m_pom = pom;
		m_id = id;
		m_name = name;
	}
	
	public POM getChangeable() {
		return m_pom;
	}
	
	public int getId() {
		return m_id;
	}

	public String getName() {
		return m_name;
	}

	public void setName( String name ) {
		m_name = name;
	}

	public void processChangeRequest(ChangeRequest changeRequest) {
		m_pom.processChangeRequest(changeRequest);
	}

	public void processChangeRequests(List<ChangeRequest> changeRequests) {
		m_pom.processChangeRequests(changeRequests);
	}
	
	public void printInfo(){
		System.out.println( "Number of Concepts: "+m_pom.getObjects(POMConcept.class).size());
		System.out.println( "Number of Instances: "+m_pom.getObjects(POMInstance.class).size());
		System.out.println( "Number of SubclassOfRelations Spanish "+m_pom.getObjects(POMSubclassOfRelationSpanish.class).size());
		System.out.println( "Number of SubclassOfRelation:  "+m_pom.getObjects(POMSubclassOfRelation.class).size());
		System.out.println( "Number of SimilarityRelations:  "+m_pom.getObjects(POMSimilarityRelation.class).size());
		System.out.println( "Number of SubtopicOfRelations:  "+m_pom.getObjects(POMSubtopicOfRelation.class).size());
		System.out.println( "Number of InstanceOfRelations:  "+m_pom.getObjects(POMInstanceOfRelation.class).size());
		System.out.println( "Number of RelationInstance:  "+m_pom.getObjects(POMRelationInstance.class).size());
		System.out.println( "Number of Relation:  "+m_pom.getObjects(POMRelation.class).size());
	}
}
