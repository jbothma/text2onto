
package org.ontoware.text2onto.ontology;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.io.*;
import java.net.*;
import java.util.*;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.change.*;
import org.ontoware.text2onto.util.ProbabilityComparator;
import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.reference.ReferenceManager;
import org.ontoware.text2onto.reference.ReferenceWrapper;
import org.ontoware.text2onto.reference.AbstractReferenceStore;
import org.ontoware.text2onto.reference.document.DocumentReference;


public class PelletWriter implements OntologyWriter {
	
	private IRI m_logicalIRI;
	
	private IRI m_physicalIRI;
	
	OWLOntologyManager m_manager;
	
	OWLOntology m_ontology;
	
	OWLDataFactory m_factory;
	
	PelletReasoner m_reasoner;
		
	private POM m_pom;
	
	
    public PelletWriter( POM pom ) throws Exception {
		m_manager = OWLManager.createOWLOntologyManager();
		m_factory = m_manager.getOWLDataFactory();
		m_pom = pom;
	}
	
	public void setEvidenceManager( EvidenceManager em ){
		// TODO
	}
	
	public void setReferenceManager( ReferenceManager rm ){
		// TODO
	}
	
	public void write( URI physicalURI ) throws Exception {
		create( new File( physicalURI ) );
		addSubclassOf( m_pom.getObjects( POMSubclassOfRelation.class ) );
		addInstanceOf( m_pom.getObjects( POMInstanceOfRelation.class ) ); 
		addRelations( m_pom.getObjects( POMRelation.class ) );
		// saveXML( new File( physicalURI ) );
		save( physicalURI.toString() );
	} 
	
	private String getLabel( POMObject object ){
		return object.getLabel().replaceAll( " ", "_" );
	}
	
	private void addSubclassOf( List relations ) throws Exception {
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMSubclassOfRelation rel = (POMSubclassOfRelation)iter.next();
			POMConcept domain = (POMConcept)rel.getDomain(); // returns subclass
			POMConcept range = (POMConcept)rel.getRange();  // return superclass
			addAxiom( subclassAxiom( getLabel( domain ), getLabel( range ) ) );
		} 
	}
	
	private void addInstanceOf( List relations ) throws Exception {
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMInstanceOfRelation rel = (POMInstanceOfRelation)iter.next();
			POMInstance domain = (POMInstance)rel.getDomain();
			POMConcept range = (POMConcept)rel.getRange();
			addAxiom( instanceofAxiom( getLabel( domain ), getLabel( range ) ) );
		} 
	}
	
	private void addRelations( List relations ) throws Exception {
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMRelation rel = (POMRelation)iter.next();
			POMConcept domain = (POMConcept)rel.getDomain();
			POMConcept range = (POMConcept)rel.getRange();
			addAxiom( domainAxiom( getLabel( rel ), getLabel( domain ) ) );
			addAxiom( rangeAxiom( getLabel( rel ), getLabel( range ) ) );
		} 
	}
	
	public void create( File file ) throws Exception {
		m_logicalIRI = IRI.create( "http://text2onto.org/ontology" );
		m_physicalIRI = IRI.create( file.toURI().toString() );
		// OWLOntologyIRIMapper mapper = new OWLOntologyIRIMapper( m_logicalIRI, m_physicalIRI );
		// m_manager.addIRIMapper( mapper );
		m_ontology = m_manager.createOntology( m_physicalIRI );
		m_reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner( m_ontology );
	}
	
	public void save( String sFile ) throws Exception {
		System.out.println( "Ontology.save: "+ sFile );
		m_manager.saveOntology( m_ontology, IRI.create( sFile ) );
	}
	
	/* public void load( File file ) throws Exception {
		System.out.println( "Ontology.load: "+ file );
		m_logicalIRI = IRI.create( "http://dbpedia.org/ontology" );
		m_physicalIRI = IRI.create( file.toURI().toString() );
		m_ontology = m_manager.loadOntologyFromOntologyDocument( m_physicalIRI );
		m_reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner( m_ontology );
	} */
	
	public boolean isCoherent() {
		Set<OWLClass> classes = m_ontology.getClassesInSignature();
		for( OWLClass c: classes )
		{
    		if( !isSatisfiable(c) )
			{
    			System.out.println( "unsatisfiable: " + c.toString()  );
				// printExplanation(c);
    			return false;
    		}
    	}
    	return true;
	}
	
	public boolean isSatisfiable( OWLClass c ){
		// OWLAxiom axiom = m_factory.getOWLSubClassOfAxiom( c, m_factory.getOWLNothing() );
		// return m_reasoner.isEntailed( axiom );
		return m_reasoner.isSatisfiable(c);
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for( OWLAxiom axiom: m_ontology.getAxioms() ){
			sb.append( axiom.toString() +"\n" );
		}
		return sb.toString();
	}
	
	public boolean entails( OWLAxiom axiom ) throws Exception {
		return m_reasoner.isEntailed( axiom );
	}
	
	public void addAxiom( OWLAxiom axiom ) throws Exception {
		AddAxiom addAxiom = new AddAxiom( m_ontology, axiom );
		m_manager.applyChange( addAxiom );
    }
	
	public void removeAxiom( OWLAxiom axiom ) throws Exception {
		RemoveAxiom removeAxiom = new RemoveAxiom( m_ontology, axiom );
		m_manager.applyChange( removeAxiom );
	}
	
	/* public OWLAnnotation annotation( String sAnnotation, double dValue ){
		OWLAnnotationProperty prop = m_factory.getOWLAnnotationProperty( IRI.create( m_logicalIRI +"#"+ sAnnotation ) );
		OWLAnnotation annotation = m_factory.getOWLAnnotation( prop, m_factory.getOWLLiteral( dValue ) );
		return annotation;
	} */

	public OWLAxiom subclassAxiom( String sName1, String sName2 ){
		OWLClass c1 = m_factory.getOWLClass( IRI.create( m_logicalIRI +"#"+ sName1 ) );
		OWLClass c2 = m_factory.getOWLClass( IRI.create( m_logicalIRI +"#"+ sName2 ) );
		return m_factory.getOWLSubClassOfAxiom(c1, c2);
	}
	
	public OWLAxiom instanceofAxiom( String sName1, String sName2 ){
		OWLIndividual ind = m_factory.getOWLNamedIndividual( IRI.create( m_logicalIRI +"#"+ sName1 ) );
		OWLClass c = m_factory.getOWLClass( IRI.create( m_logicalIRI +"#"+ sName2 ) );
		return m_factory.getOWLClassAssertionAxiom(c, ind);
	}
	
	public OWLAxiom domainAxiom( String sName1, String sName2 ){
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create( m_logicalIRI +"#"+ sName1 ) );
		OWLClass c = m_factory.getOWLClass( IRI.create( m_logicalIRI +"#"+ sName2 ) );
		return m_factory.getOWLObjectPropertyDomainAxiom(prop, c);
	}
	
	public OWLAxiom rangeAxiom( String sName1, String sName2 ){
		OWLObjectProperty prop = m_factory.getOWLObjectProperty( IRI.create( m_logicalIRI +"#"+ sName1 ) );
		OWLClass c = m_factory.getOWLClass( IRI.create( m_logicalIRI +"#"+ sName2 ) );
		return m_factory.getOWLObjectPropertyRangeAxiom(prop, c);
	}
}
