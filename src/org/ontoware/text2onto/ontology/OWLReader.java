package org.ontoware.text2onto.ontology;

import java.util.*; 
import java.net.URI; 

import org.semanticweb.kaon2.api.*;
import org.semanticweb.kaon2.api.owl.axioms.*;
import org.semanticweb.kaon2.api.owl.elements.*;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.change.*;


public class OWLReader implements OntologyReader { 

	private POMWrapper m_pomWrapper;

	private KAON2Connection m_connection;

	private Ontology m_ontology;
	
	
	public static void main( String args[] ){
		try {
			OWLReader reader = new OWLReader( new URI( args[0] ) );
			System.out.println( reader.read() );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public OWLReader( URI physicalURI ) throws InterruptedException, KAON2Exception { 
		m_pomWrapper = new POMWrapper( POMFactory.newPOM() ); 
		m_connection = KAON2Manager.newConnection(); 
		try {
			m_ontology = m_connection.openOntology( physicalURI.toString(), new HashMap() );
		}			
		catch( KAON2Exception e )
		{
			System.err.println( "Could not open ontology " + physicalURI );
			e.printStackTrace();
		}
		finally {
			m_connection.close();
		}  
	}

	public POM read() throws Exception { 
		Set classes = m_ontology.createEntityRequest( OWLClass.class ).get(); 
		Iterator iter = classes.iterator();
		while( iter.hasNext() )
		{ 
			OWLClass owlClass = (OWLClass)iter.next();
			List objects = createPOMObjects( owlClass ); 
			List<ChangeRequest> changes = new ArrayList<ChangeRequest>();
			Iterator objIter = objects.iterator();
			while( objIter.hasNext() )
			{
				POMObject object = (POMObject)objIter.next(); 
				changes.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, object, (Change)null ) ) );
			} 
			m_pomWrapper.processChangeRequests( changes );
		}
		return m_pomWrapper.getChangeable();
	} 
			
			
	private List createPOMObjects( OWLClass owlClass ) throws KAON2Exception {	
		ArrayList al = new ArrayList();		
		if( !owlClass.equals( KAON2Manager.factory().thing() ) )
		{
			al.add( createPOMConcept( owlClass ) );
		}
		Set subSet = owlClass.getSubDescriptions( m_ontology );
		Iterator iter = subSet.iterator();
		while( iter.hasNext() )
		{
			OWLClass subClass = (OWLClass)iter.next();
			al.add( createSubclassOfRelation( subClass, owlClass ) );
		}
		Set individuals = owlClass.getMemberIndividuals( m_ontology );
		iter = individuals.iterator();
		while( iter.hasNext() )
		{
			Individual individual = (Individual)iter.next();
			al.add( createPOMInstance( individual ) );
			al.add( createInstanceOfRelation( individual, owlClass ) );
		} 
		return al;
	}
	
	private POMConcept createPOMConcept( OWLClass owlClass ) throws KAON2Exception {
		POMConcept concept = m_pomWrapper.getChangeable().newConcept( getLabel( owlClass ) );
		concept.setProbability( 1.0 );
		concept.setUserEvidence( true );
		return concept;		
	}
	
	private POMInstance createPOMInstance( Individual individual ) throws KAON2Exception {
		POMInstance instance = m_pomWrapper.getChangeable().newInstance( getLabel( individual ) );
		instance.setProbability( 1.0 );
		instance.setUserEvidence( true );
		return instance;		
	}
	
	private POMSubclassOfRelation createSubclassOfRelation( OWLClass subClass, OWLClass superClass ) throws KAON2Exception {		
		POMConcept domain = createPOMConcept( subClass );
		POMConcept range = createPOMConcept( superClass );
		POMSubclassOfRelation rel = m_pomWrapper.getChangeable().newSubclassOfRelation( domain, range );
		rel.setProbability( 1.0 );
		rel.setUserEvidence( true );
		return rel; 
	}
	
	private POMInstanceOfRelation createInstanceOfRelation( Individual individual, OWLClass superClass ) throws KAON2Exception {		
		POMInstance domain = createPOMInstance( individual );
		POMConcept range = createPOMConcept( superClass );
		POMInstanceOfRelation rel = m_pomWrapper.getChangeable().newInstanceOfRelation( domain, range );
		rel.setProbability( 1.0 );
		rel.setUserEvidence( true );
		return rel; 
	}
	 
	private String getLabel( OWLEntity entity ) throws KAON2Exception {
		String sLabel = null;	
		AnnotationProperty labelAnnotation = KAON2Manager.factory().annotationProperty( "http://www.w3.org/2000/01/rdf-schema#label" );
		Object label = entity.getEntityAnnotationValue( m_ontology, labelAnnotation ); 
		if( label == null ){
			sLabel = entity.toString();
		}
		else {
			sLabel = label.toString();
		}
		int i = sLabel.indexOf( "#" );
		if( i != -1 ){
			sLabel = sLabel.substring( i+1 );
		}
		sLabel.replaceAll( "_", " " );
		sLabel = sLabel.toLowerCase();
		return sLabel;
	}
}



