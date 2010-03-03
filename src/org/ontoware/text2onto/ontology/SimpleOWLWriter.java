package org.ontoware.text2onto.ontology;

import java.util.*; 
import java.net.URI; 
import java.io.File;

import org.semanticweb.kaon2.api.*;
import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
import org.semanticweb.kaon2.api.owl.axioms.*;
import org.semanticweb.kaon2.api.owl.elements.*; 

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.change.*; 
import org.ontoware.text2onto.util.ProbabilityComparator;

import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.reference.ReferenceManager;
import org.ontoware.text2onto.reference.ReferenceWrapper;
import org.ontoware.text2onto.reference.AbstractReferenceStore;
import org.ontoware.text2onto.reference.document.DocumentReference;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class SimpleOWLWriter implements OntologyWriter { 

	private POM m_pom;

	private KAON2Connection m_connection;

	private KAON2Factory m_factory;

	private Ontology m_ontology;
	
	
	private final static String m_sLogicalURI = "http://www.text2onto.org/ontology";
	
	private static String m_sPhysicalURI;
	
	
	private HashMap m_classes = new HashMap();

	private HashMap m_individuals = new HashMap();
	
	private HashMap m_relations = new HashMap();
	
	
	private final static boolean ALL_ENTITIES = true;



	public SimpleOWLWriter( POM pom ) throws KAON2Exception { 
		m_pom = pom;
	}
	
	public void setEvidenceManager( EvidenceManager em ){
		// TODO
	}
	
	public void setReferenceManager( ReferenceManager rm ){
		// TODO
	}
	
	private String checkForInvalidChars( String uri ){
		// returns filtered uri... 
		return uri.replaceAll( "[()\\[\\]\\^\\%\\>\\<\\&\\;]", "" );
	}

	public void write( URI physicalURI ) throws Exception {
		m_sPhysicalURI = physicalURI.toString();
		m_connection = KAON2Manager.newConnection(); 
		DefaultOntologyResolver resolver = new DefaultOntologyResolver();
		resolver.registerReplacement( m_sLogicalURI, m_sPhysicalURI );
		m_connection.setOntologyResolver( resolver ); 
		try {
			m_ontology = m_connection.createOntology( m_sLogicalURI, new HashMap<String,Object>() );
		}			
		catch( KAON2Exception e )
		{
			System.err.println( "Could not create new ontology " + m_sLogicalURI );
			e.printStackTrace();
		}
		finally {
			m_connection.close();
		}  
		m_factory = KAON2Manager.factory();  
		m_ontology.setDuplicateAxiomsThrowException( false );
		addSubclassOf( m_pom.getObjects( POMSubclassOfRelation.class ) );
		addInstanceOf( m_pom.getObjects( POMInstanceOfRelation.class ) ); 
		addRelations( m_pom.getObjects( POMRelation.class ) );
		addDisjointClasses( m_pom.getObjects( POMDisjointClasses.class ) );
		if( ALL_ENTITIES ){
			addInstances( m_pom.getObjects( POMInstance.class ) );
			addConcepts( m_pom.getObjects( POMConcept.class ) );
		}
		// saveXML( new File( physicalURI ) );
		saveRDF( new File( physicalURI ) );
	} 
	
	private void addInstances( List instances ) throws KAON2Exception {
		Iterator iter = instances.iterator();
		while( iter.hasNext() )
		{ 
			POMInstance instance = (POMInstance)iter.next(); 
			createIndividual( instance );
		}
	}
	
	private void addConcepts( List concepts ) throws KAON2Exception {
		Iterator iter = concepts.iterator();
		while( iter.hasNext() )
		{ 
			POMConcept concept = (POMConcept)iter.next(); 
			createClass( concept );
		} 
	}
	
	private String checkString( String s ){
		if( s.contains( "[" ) || s.contains( "]" ) || s.contains( "%" ) 
			|| s.contains( ">" ) || s.contains( "<" ) || s.contains( "^" ) 
			|| s.contains( "&lt;" ) || s.contains( "&gt;" ) || s.contains( "~" ) )
		{
			s = s.replace( "[", "" ); // "sq_brackets_open" );
			s = s.replace( "]", "" ); // "sq_brackets_close" );
			s = s.replace( "%", "" ); // "percent" );	
			s = s.replace( ">", "" ); // "greater_than" );	
			s = s.replace( "<", "" ); // "less_than" );
			s = s.replace( "^", "" ); // "caret" );
			s = s.replace( "&gt;", "" ); // "greater_than" );	
			s = s.replace( "&lt;", "" ); // "less_than" );
			s = s.replace( "~", "" ); // "tilde" );
		}
		if( s.length() == 0 ){
			return null;
		}
		return s;
	}

	private OWLClass createClass( POMConcept concept ){
		String uri = checkForInvalidChars(concept.getLabel().replaceAll( " ", "_" ));
		// check if uri still valid or 'empty'
		if( uri.length() < 1 ){
			return null;
		}
		uri = m_sLogicalURI +"#"+ uri +"_c";
		if( m_classes.containsKey( uri ) ){
			return (OWLClass)m_classes.get( uri );
		}
		OWLClass c = m_factory.owlClass( uri );
		m_classes.put( uri, c );
		try {
		   SubClassOf subclassof = m_factory.subClassOf( c, m_factory.thing() );
		   EntityAnnotation conceptLabel = createAnnotation( c, concept.getLabel() );
		   List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		   addAxiom( conceptLabel, changes );
		   if( ALL_ENTITIES ){
		   	addAxiom( subclassof, changes );
		   }
		   m_ontology.applyChanges( changes );
		}
		catch( Exception e ){ 
			e.printStackTrace(); 
		}
		return c;
	}
	
	private Individual createIndividual( POMInstance instance ){
		String uri = checkForInvalidChars( instance.getLabel().replaceAll( " ", "_" ) );
		// check if uri still valid or 'empty'
		if( uri.length() < 1 ){
			return null;
		}
		uri = m_sLogicalURI +"#"+ uri +"_i";
		if( m_individuals.containsKey( uri ) ){ 
			return (Individual)m_individuals.get( uri );
		}
		Individual i = m_factory.individual( uri );
		m_individuals.put( uri, i );
		try {
		   ClassMember member = m_factory.classMember( m_factory.thing(), i );
		   EntityAnnotation instanceLabel = createAnnotation( i, instance.getLabel() );
		   List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		   addAxiom( instanceLabel, changes );
			if( ALL_ENTITIES ){
				addAxiom( member, changes );
			}
		   m_ontology.applyChanges( changes );
		}
		catch( Exception e ){
			e.printStackTrace(); 
		}
		return i;
	}	
	
	private ObjectProperty createProperty( POMRelation relation ){
		String uri = checkForInvalidChars( relation.getLabel().replaceAll( " ", "_" ) );
		// check if uri still valid or 'empty'
		if( uri.length() < 1 ){
			return null;
		}
		uri = m_sLogicalURI +"#"+ uri +"_r";
		if( m_relations.containsKey( uri ) ){
			return (ObjectProperty)m_relations.get( uri );
		}
		ObjectProperty r = m_factory.objectProperty( uri );
		m_relations.put( uri, r );
		try {
		   EntityAnnotation relationLabel = createAnnotation( r, relation.getLabel() );
		   List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		   addAxiom( relationLabel, changes );
		   m_ontology.applyChanges( changes );
		}
		catch( Exception e ){ 
			e.printStackTrace(); 
		}
		return r;
	}
	
	private void addSubclassOf( List relations ) throws KAON2Exception {
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMSubclassOfRelation rel = (POMSubclassOfRelation)iter.next();
			POMConcept domain = (POMConcept)rel.getDomain(); // returns subclass
			POMConcept range = (POMConcept)rel.getRange();  // return superclass
			if( domain.equals( range ) ){
				return;
			}
			String sRel = rel.getLabel().replaceAll( " ", "_" );
			try {
				OWLClass domainClass = createClass( domain );
				OWLClass rangeClass = createClass( range );
				if( domainClass == null || rangeClass == null ){
					System.out.println( "OWLWriter: abandoned sub/superclass: " + sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )");
				}
				else {
					SubClassOf subclassof = m_factory.subClassOf( domainClass, rangeClass );
					List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
					addAxiom( subclassof, changes );
					m_ontology.applyChanges( changes );  
				} 
			}
			catch( Exception e ){
				System.out.println( "OWLWriter: cannot create relation "+ sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )" );
				e.printStackTrace();
			}
		} 
	}
		
	private void addInstanceOf( List relations ) throws KAON2Exception {
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMInstanceOfRelation rel = (POMInstanceOfRelation)iter.next();
			POMInstance domain = (POMInstance)rel.getDomain();
			POMConcept range = (POMConcept)rel.getRange();
			String sRel = rel.getLabel().replaceAll( " ", "_" );
			try {
				Individual domainIndividual = createIndividual( domain );
				OWLClass rangeClass = createClass( range );
				if ( domainIndividual == null || rangeClass == null ){
					System.out.println( "OWLWriter: abandoned instance: " + sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )" );
				}
				else {
					ClassMember member = m_factory.classMember( rangeClass, domainIndividual );
					List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
					addAxiom( member, changes );
					m_ontology.applyChanges( changes ); 
				}
			}
			catch( Exception e ){
				System.out.println( "OWLWriter: cannot create relation "+ sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )" );
				e.printStackTrace();
			}
		} 
	}
	
	private void addRelations( List relations ) throws KAON2Exception {
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMRelation rel = (POMRelation)iter.next();
			POMConcept domain = (POMConcept)rel.getDomain();
			POMConcept range = (POMConcept)rel.getRange();
			String sRel = rel.getLabel().replaceAll( " ", "_" );
			try {
				OWLClass domainClass = createClass( domain );
				OWLClass rangeClass = createClass( range );
				ObjectProperty relation = createProperty( rel );
				if( domainClass == null || rangeClass == null ){
					System.out.println( "OWLWriter: incomplete relation: " + sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )");
				}
				else {
					ObjectPropertyDomain propertyDomain = m_factory.objectPropertyDomain( relation, domainClass ); 
					ObjectPropertyRange propertyRange = m_factory.objectPropertyRange( relation, rangeClass ); 
					List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
					addAxiom( propertyDomain, changes ); 
					addAxiom( propertyRange, changes ); 
					m_ontology.applyChanges( changes );  
				}
			}
			catch( Exception e ){
				System.out.println( "OWLWriter: cannot create relation "+ sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )" );
				e.printStackTrace();
			}
		} 
	}
	
	private void addDisjointClasses( List<POMDisjointClasses> disjoints ) throws KAON2Exception {	
		Iterator iter = disjoints.iterator();
		while( iter.hasNext() )
		{
			Collection<Description> classes = new HashSet<Description>();
			POMDisjointClasses disjoint = (POMDisjointClasses)iter.next();
			try {
				for( POMConcept concept: disjoint.getConcepts() )
				{
					OWLClass c = createClass( concept );
					if( !classes.contains(c) ){
						classes.add(c);
					}
				}
				DisjointClasses dc = m_factory.disjointClasses( classes );
				List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
				addAxiom( dc, changes );
				m_ontology.applyChanges( changes ); 
			}
			catch( Exception e ){
				e.printStackTrace();
			}
		}
	}
	
 	private void addAxiom( Axiom a, List <OntologyChangeEvent> changes ) throws KAON2Exception {
	   if( !m_ontology.containsAxiom( a, false ) ){
	   	changes.add( new OntologyChangeEvent( a, OntologyChangeEvent.ChangeType.ADD ) ); 
		}
	}
	
	private EntityAnnotation createAnnotation( OWLEntity entity, String sLabel ) throws KAON2Exception { 	 
		AnnotationProperty annProp = m_factory.annotationProperty( "http://www.w3.org/2003/05/owl-xml#Label" );
		EntityAnnotation ann = m_factory.entityAnnotation( annProp, entity, m_factory.constant( sLabel ) );
		return ann;
	}
	 
	private void saveXML( File file ) throws Exception { 
		file.createNewFile();
		// OWL2XMLExporter.export( file, "ISO-8859-1" , m_ontology );
		m_ontology.saveOntology( OntologyFileFormat.OWL_XML, file, "ISO-8859-1" );
	}
	
	private void saveRDF( File file ) throws Exception {	
		file.createNewFile();
		// OWL2RDFExporter.export( file, "ISO-8859-1", m_ontology );
		m_ontology.saveOntology( OntologyFileFormat.OWL_RDF, file, "ISO-8859-1" );
	}
}



