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
public class OWLWriter implements OntologyWriter { 

	private POM m_pom;

	private KAON2Connection m_connection;

	private KAON2Factory m_factory;

	private Ontology m_ontology;
	
	private ProbabilityComparator m_comparator;
	
	
	private OWLClass m_conceptClass;
	
	private OWLClass m_instanceClass;
	
	private OWLClass m_subclassOfClass;
	
	private OWLClass m_instanceOfClass;
	
	private OWLClass m_disjointClass;
	
	private OWLClass m_relationClass;
	
	private OWLClass m_referenceClass;
	
	
	private ObjectProperty m_domainProperty;
	
	private ObjectProperty m_rangeProperty;
	
	private DataProperty m_pointsToProperty;
	
	private ObjectProperty m_refersToProperty;
	
	private DataProperty m_nameProperty;
	 
	
	private final static String m_sLogicalURI = "http://www.text2onto.org/ontology";
	
	private static String m_sPhysicalURI;
	
	private int m_objId = 1;
	
	private int m_refId = 1;
		

	private ReferenceManager m_referenceManager;
		
	private HashMap classes = new HashMap();

	private HashMap individuals = new HashMap();
	
	
	private final static boolean m_REFS = false;
	
	private final static boolean ALL_ENTITIES = true;

	
	public OWLWriter( POM pom ) throws KAON2Exception { 
		m_pom = pom; 
		m_comparator = new ProbabilityComparator();
	}
	
	public void setEvidenceManager( EvidenceManager em ){
		// TODO
	}
	
	public void setReferenceManager( ReferenceManager rm ){
		m_referenceManager = rm;
	}
	
	/* private List<DocumentReference> getReferences( POMObject object ){
		List<DocumentReference> references = new ArrayList<DocumentReference>();
		List<ReferenceWrapper> wrappers = m_referenceManager.getLocalStores();
		for( ReferenceWrapper wrapper: wrappers ){
			AbstractReferenceStore store = wrapper.getChangeable();
			references.addAll( store.getReferences( object ) );
		}
		return references;
	} */
	
	/* DEBUG */
	private List<DocumentReference> getReferences( POMObject object ){
		ArrayList<DocumentReference> references = new ArrayList<DocumentReference>();
		List<Change> changes = m_pom.getChanges( object );  
		for( Change change: changes )
		{
			int iChange = change.getType();
			List<Change> evidences = new ArrayList<Change>();
			List<Change> causes = change.getCauses();   
			for( Change cause: causes )
			{
				if( cause instanceof POMChange ){
					evidences.addAll( cause.getCauses() );
				}
				else if( cause instanceof EvidenceChange ){
					evidences.add( cause );
				}
			}
			for( Change cause: evidences )
			{
				if( cause instanceof EvidenceChange )
				{
					Object value = cause.getValue();
					if( value instanceof DocumentReference )
					{
						DocumentReference reference = (DocumentReference)value; 
						if( !references.contains( reference ) ){
							references.add( reference );
						}
					}
				}				
			}
		}
		return references;
	}

	private String checkForInvalidChars(String uri) {
		// returns filtered uri... 
		return uri.replaceAll("[()\\[\\]\\^\\%\\>\\<\\&\\;]", "");
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
		init();	
		addSubclassOf( m_pom.getObjects( POMSubclassOfRelation.class ) );
	    
		addInstanceOf( m_pom.getObjects( POMInstanceOfRelation.class ) ); 
		addRelations( m_pom.getObjects( POMRelation.class ) );
		addDisjointClasses( m_pom.getObjects( POMDisjointClasses.class ) );
		if( ALL_ENTITIES ){
			addConcepts( m_pom.getObjects( POMConcept.class ) );
			addInstances( m_pom.getObjects( POMInstance.class ) );
		}
		// saveXML( new File( physicalURI ) );
		saveRDF( new File( physicalURI ) );
	} 
	
	private void init() throws KAON2Exception {	
		OWLClass elementClass = m_factory.owlClass( m_sLogicalURI +"#Element" );
		OWLClass entityClass = m_factory.owlClass( m_sLogicalURI +"#Entity" );
	
		m_conceptClass = m_factory.owlClass( m_sLogicalURI +"#Concept" );
		m_instanceClass = m_factory.owlClass( m_sLogicalURI +"#Instance" );
		m_subclassOfClass = m_factory.owlClass( m_sLogicalURI +"#SubclassOf" );
		m_instanceOfClass = m_factory.owlClass( m_sLogicalURI +"#InstanceOf" );
		m_relationClass = m_factory.owlClass( m_sLogicalURI +"#Relation" );
		m_disjointClass = m_factory.owlClass( m_sLogicalURI +"#DisjointClasses" );
		m_referenceClass = m_factory.owlClass( m_sLogicalURI +"#DocumentPointer" );
		
		SubClassOf referenceThing = m_factory.subClassOf( m_referenceClass, m_factory.thing() );
		SubClassOf elementThing = m_factory.subClassOf( elementClass, m_factory.thing() );
		SubClassOf entityElement = m_factory.subClassOf( entityClass, elementClass );
		SubClassOf conceptEntity = m_factory.subClassOf( m_conceptClass, entityClass );
		SubClassOf instanceEntity = m_factory.subClassOf( m_instanceClass, entityClass );
		SubClassOf subclassOfElement = m_factory.subClassOf( m_subclassOfClass, elementClass );
		SubClassOf instanceOfElement = m_factory.subClassOf( m_instanceOfClass, elementClass );
		SubClassOf relationElement = m_factory.subClassOf( m_relationClass, elementClass );
		SubClassOf disjointElement = m_factory.subClassOf( m_disjointClass, elementClass );
		
		m_domainProperty = m_factory.objectProperty( m_sLogicalURI +"#Domain" );
		m_rangeProperty = m_factory.objectProperty( m_sLogicalURI +"#Range" );
		
		m_pointsToProperty = m_factory.dataProperty( m_sLogicalURI +"#PointsTo" );
		m_refersToProperty = m_factory.objectProperty( m_sLogicalURI +"#RefersTo" );
		
		m_nameProperty = m_factory.dataProperty( m_sLogicalURI +"#Name" );
		 
		List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		changes.add( new OntologyChangeEvent( referenceThing, OntologyChangeEvent.ChangeType.ADD ) );
		changes.add( new OntologyChangeEvent( elementThing, OntologyChangeEvent.ChangeType.ADD ) );
		changes.add( new OntologyChangeEvent( entityElement, OntologyChangeEvent.ChangeType.ADD ) ); 
		changes.add( new OntologyChangeEvent( conceptEntity, OntologyChangeEvent.ChangeType.ADD ) );
		changes.add( new OntologyChangeEvent( instanceEntity, OntologyChangeEvent.ChangeType.ADD ) );
		changes.add( new OntologyChangeEvent( subclassOfElement, OntologyChangeEvent.ChangeType.ADD ) );
		changes.add( new OntologyChangeEvent( instanceOfElement, OntologyChangeEvent.ChangeType.ADD ) ); 
		changes.add( new OntologyChangeEvent( relationElement, OntologyChangeEvent.ChangeType.ADD ) );
		changes.add( new OntologyChangeEvent( disjointElement, OntologyChangeEvent.ChangeType.ADD ) );
		 		 
		m_ontology.applyChanges( changes );  
	}

	private void addInstances( List instances ) throws KAON2Exception {
		Collections.sort( instances, m_comparator );
		Iterator iter = instances.iterator();
		while( iter.hasNext() )
		{ 
			POMInstance instance = (POMInstance)iter.next(); 
			createIndividual(instance);
		}
	}
	
	private void addConcepts( List concepts ) throws KAON2Exception {
		Collections.sort( concepts, m_comparator );
		Iterator iter = concepts.iterator();
		while( iter.hasNext() )
		{ 
			POMConcept concept = (POMConcept)iter.next(); 
			createClass(concept);
		} 
	}
	
	private String checkString( String s ){
		if( s.contains( "[" ) || s.contains( "]" ) || s.contains( "%" ) 
			|| s.contains( ">" ) || s.contains( "<" ) || s.contains( "^" ) 
			|| s.contains( "&lt;" ) || s.contains( "&gt;" ) )
		{
			s = s.replace( "[", "" ); // "sq_brackets_open" );
			s = s.replace( "]", "" ); // "sq_brackets_close" );
			s = s.replace( "%", "" ); // "percent" );	
			s = s.replace( ">", "" ); // "greater_than" );	
			s = s.replace( "<", "" ); // "less_than" );
			s = s.replace( "^", "" ); // "caret" );
			s = s.replace( "&gt;", "" ); // "greater_than" );	
			s = s.replace( "&lt;", "" ); // "less_than" );
		}
		if( s.length() == 0 ){
			return null;
		}
		return s;
	}

	private Individual createClass( POMConcept concept ){
		String uri = checkForInvalidChars(concept.getLabel().replaceAll( " ", "_" ));
		// check if uri still valid or 'empty'
		if (uri.length() < 1) {
			return null;
		}
		uri = m_sLogicalURI +"#"+ uri +"_c";
		if( classes.containsKey( uri ) ){
			return (Individual)classes.get( uri );
		}
		Individual c = m_factory.individual( uri );
		classes.put( uri, c );
		try {
		   ClassMember member = m_factory.classMember( m_conceptClass, c );
		   EntityAnnotation conceptAnn = createAnnotation( c, concept.getProbability() );
		   EntityAnnotation conceptLabel = createAnnotation( c, concept.getLabel() );
		   List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		   addAxiom( conceptAnn, changes );
		   addAxiom( conceptLabel, changes );
		   addAxiom( member, changes );
		   m_ontology.applyChanges( changes );
		}
		catch( Exception e ){ 
			System.out.println( e.toString() ); 
		}
		return c;
	}
	
	private Individual createIndividual( POMInstance instance ){
		String uri = checkForInvalidChars(instance.getLabel().replaceAll( " ", "_" ));
		// check if uri still valid or 'empty'
		if (uri.length() < 1) {
			return null;
		}
		uri = m_sLogicalURI +"#"+ uri +"_i";
		if( individuals.containsKey( uri ) ){ 
			return (Individual)individuals.get( uri );
		}
		Individual c = m_factory.individual( uri );
		individuals.put( uri, c );
		try {
		   ClassMember member = m_factory.classMember( m_instanceClass, c);
		   EntityAnnotation instanceAnn = createAnnotation( c, instance.getProbability() );
		   EntityAnnotation instanceLabel = createAnnotation( c, instance.getLabel() );
		   List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
		   addAxiom( instanceAnn, changes );
		   addAxiom( instanceLabel, changes );
		   addAxiom( member, changes );
		   m_ontology.applyChanges(changes);
		}
		catch( Exception e ){
			System.out.println( e.toString() ); 
		}
		return c;
	}
	
	private void addSubclassOf( List relations ) throws KAON2Exception {
		Collections.sort( relations, m_comparator );
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMSubclassOfRelation rel = (POMSubclassOfRelation)iter.next();
			POMConcept domain = (POMConcept)rel.getDomain(); //returns subclass
			POMConcept range = (POMConcept)rel.getRange();  // return superclass
			 
			String sRel = rel.getLabel().replaceAll( " ", "_" );

			try {
				Individual domainIndividual = createClass( domain );
				Individual rangeIndividual = createClass( range );
				if ( domainIndividual == null | rangeIndividual == null )
					System.out.println( "OWLWriter: abandoned subclass: " + sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )");
				else{

					Individual relationIndividual = m_factory.individual( m_sLogicalURI +"#"+ sRel +"_"+ (m_objId++) + System.currentTimeMillis()/1000 ); 

					ClassMember relationMember = m_factory.classMember( m_subclassOfClass, relationIndividual );

					EntityAnnotation relationAnn = createAnnotation( relationIndividual, rel.getProbability() );
					EntityAnnotation relationLabel = createAnnotation( relationIndividual, rel.getLabel() );

					// DataPropertyMember domainName = m_factory.dataPropertyMember( m_nameProperty, domainIndividual, sDomain );
					// DataPropertyMember rangeName = m_factory.dataPropertyMember( m_nameProperty, rangeIndividual, sRange );
					// DataPropertyMember relationName = m_factory.dataPropertyMember( m_nameProperty, relationIndividual, sRel );

					ObjectPropertyMember propertyDomain = m_factory.objectPropertyMember( m_domainProperty, relationIndividual, domainIndividual ); 
					ObjectPropertyMember propertyRange = m_factory.objectPropertyMember( m_rangeProperty, relationIndividual, rangeIndividual ); 

					List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();

					addAxiom( relationMember, changes ); 
					addAxiom( relationAnn, changes );
					addAxiom( relationLabel, changes );

					addAxiom( propertyDomain, changes ); 
					addAxiom( propertyRange, changes ); 

					m_ontology.applyChanges( changes );  

					if( m_REFS ){
						addReferences( domainIndividual, getReferences( domain ) );
						addReferences( rangeIndividual, getReferences( range ) );
						addReferences( relationIndividual, getReferences( rel ) );
					}
				} 
			}
			catch( Exception e ){
				System.out.println( "OWLWriter: cannot create relation "+ sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )" );
				e.printStackTrace();
			}
		} 
	}
		
	private void addInstanceOf( List relations ) throws KAON2Exception {
		Collections.sort( relations, m_comparator );
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMInstanceOfRelation rel = (POMInstanceOfRelation)iter.next();
			POMInstance domain = (POMInstance)rel.getDomain();
			POMConcept range = (POMConcept)rel.getRange();
			 
			String sRel = rel.getLabel().replaceAll( " ", "_" );
			
			try {
				Individual domainIndividual = createIndividual( domain );
				Individual rangeIndividual = createClass(range);
				if ( domainIndividual == null | rangeIndividual == null ){
					System.out.println( "OWLWriter: abandoned instance: " + sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )" );
				}
				else {
					Individual relationIndividual = m_factory.individual( m_sLogicalURI +"#"+ sRel +"_"+ (m_objId++) + System.currentTimeMillis()/1000 ); 

					ClassMember relationMember = m_factory.classMember( m_instanceOfClass, relationIndividual );

					EntityAnnotation relationAnn = createAnnotation( relationIndividual, rel.getProbability() );
					EntityAnnotation relationLabel = createAnnotation( relationIndividual, rel.getLabel() );

					// DataPropertyMember domainName = m_factory.dataPropertyMember( m_nameProperty, domainIndividual, sDomain );
					// DataPropertyMember rangeName = m_factory.dataPropertyMember( m_nameProperty, rangeIndividual, sRange );
					// DataPropertyMember relationName = m_factory.dataPropertyMember( m_nameProperty, relationIndividual, sRel );

					ObjectPropertyMember propertyDomain = m_factory.objectPropertyMember( m_domainProperty, relationIndividual, domainIndividual ); 
					ObjectPropertyMember propertyRange = m_factory.objectPropertyMember( m_rangeProperty, relationIndividual, rangeIndividual );  			

					List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();

					addAxiom( relationMember, changes ); 

					addAxiom( relationAnn, changes );
					addAxiom( relationLabel, changes );

					// addAxiom( domainName, changes );
					// addAxiom( rangeName, changes );
					// addAxiom( relationName, changes );

					addAxiom( propertyDomain, changes ); 
					addAxiom( propertyRange, changes ); 

					m_ontology.applyChanges( changes ); 

					if( m_REFS ){
						addReferences( domainIndividual, getReferences( domain ) );
						addReferences( rangeIndividual, getReferences( range ) ); 
						addReferences( relationIndividual, getReferences( rel ) );
					}
				}
			}
			catch( Exception e ){
				System.out.println( "OWLWriter: cannot create relation "+ sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )" );
				e.printStackTrace();
			}
		} 
	}
	
	private void addRelations( List relations ) throws KAON2Exception {
		Collections.sort( relations, m_comparator );
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMRelation rel = (POMRelation)iter.next();
			POMConcept domain = (POMConcept)rel.getDomain(); //returns subclass
			POMConcept range = (POMConcept)rel.getRange();  // return superclass
			 
			String sRel = rel.getLabel().replaceAll( " ", "_" );

			try {
				Individual domainIndividual = createClass( domain );
				Individual rangeIndividual = createClass( range );
				if ( domainIndividual == null | rangeIndividual == null ){
					System.out.println( "OWLWriter: abandoned relation: " + sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )");
				}
				else {
					Individual relationIndividual = m_factory.individual( m_sLogicalURI +"#"+ sRel +"_"+ (m_objId++) + System.currentTimeMillis()/1000 ); 

					ClassMember relationMember = m_factory.classMember( m_relationClass, relationIndividual );

					EntityAnnotation relationAnn = createAnnotation( relationIndividual, rel.getProbability() );
					EntityAnnotation relationLabel = createAnnotation( relationIndividual, rel.getLabel() );

					// DataPropertyMember domainName = m_factory.dataPropertyMember( m_nameProperty, domainIndividual, sDomain );
					// DataPropertyMember rangeName = m_factory.dataPropertyMember( m_nameProperty, rangeIndividual, sRange );
					// DataPropertyMember relationName = m_factory.dataPropertyMember( m_nameProperty, relationIndividual, sRel );

					ObjectPropertyMember propertyDomain = m_factory.objectPropertyMember( m_domainProperty, relationIndividual, domainIndividual ); 
					ObjectPropertyMember propertyRange = m_factory.objectPropertyMember( m_rangeProperty, relationIndividual, rangeIndividual ); 

					List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();

					addAxiom( relationMember, changes ); 
					addAxiom( relationAnn, changes );
					addAxiom( relationLabel, changes );

					addAxiom( propertyDomain, changes ); 
					addAxiom( propertyRange, changes ); 

					m_ontology.applyChanges( changes );  

					if( m_REFS ){
						addReferences( domainIndividual, getReferences( domain ) );
						addReferences( rangeIndividual, getReferences( range ) );
						addReferences( relationIndividual, getReferences( rel ) );
					}
				}
			}
			catch( Exception e ){
				System.out.println( "OWLWriter: cannot create relation "+ sRel +"( "+ domain.getLabel() +", "+ range.getLabel() +" )" );
				e.printStackTrace();
			}
		} 
	}
	
	/* private void addDisjointClasses( List<POMDisjointClasses> disjoints ) throws KAON2Exception {
		Collections.sort( disjoints, m_comparator );
		Iterator iter = disjoints.iterator();
		while( iter.hasNext() )
		{
			POMDisjointClasses disjoint = (POMDisjointClasses)iter.next(); 
			List<POMConcept> concepts = new ArrayList( disjoint.getConcepts() );
			if( concepts.size() == 2 ){
				addDisjointClasses( disjoint, (POMConcept)concepts.get(0), (POMConcept)concepts.get(1) );
			}
			else {
				for( int i=0; i<concepts.size(); i++ )
				{
					POMConcept concept1 = (POMConcept)concepts.get(i);
					for( int j=i+1; j<concepts.size(); j++ )
					{
						POMConcept concept2 = (POMConcept)concepts.get(j); 
						addDisjointClasses( disjoint, concept1, concept2 );
					}
				} 
			}
		}
	} */
	
	/* DEBUG */
	private void addDisjointClasses( List<POMDisjointClasses> disjoints ) throws KAON2Exception {
		// Collections.sort( disjoints, m_comparator );
		HashMap<POMConcept,POMConcept> hmConcept2Concept = new HashMap<POMConcept,POMConcept>();
		Iterator iter = disjoints.iterator();
		while( iter.hasNext() )
		{
			POMDisjointClasses disjoint = (POMDisjointClasses)iter.next();
			List<POMConcept> concepts = new ArrayList( disjoint.getConcepts() );
			if( concepts.size() == 2 )
			{
				POMConcept concept1 = concepts.get(0);
				POMConcept concept2 = concepts.get(1);
				POMConcept c2 = hmConcept2Concept.get( concept1 );
				POMConcept c1 = hmConcept2Concept.get( concept2 );
				if( !( c2 != null && c2.equals( concept2 ) )
					&& !( c1 != null && c1.equals( concept1 ) ) )
				{
					addDisjointClasses( disjoint, concept1, concept2 );
					hmConcept2Concept.put( concept1, concept2 );
					hmConcept2Concept.put( concept2, concept1 );
				}
			} 
			else {
				for( int i=0; i<concepts.size(); i++ )
				{
					POMConcept concept1 = (POMConcept)concepts.get(i);
					for( int j=i+1; j<concepts.size(); j++ )
					{
						POMConcept concept2 = (POMConcept)concepts.get(j); 
						addDisjointClasses( disjoint, concept1, concept2 );
					}
				} 
			}
		}
	}
	
	private void addDisjointClasses( POMDisjointClasses disjoint, POMConcept domain, POMConcept range ){
		// check relevance
		Set<POMConcept> concepts = disjoint.getConcepts();
		for( POMConcept concept: concepts )
		{
			if( m_pom.getRelationsWithRange( POMSubclassOfRelation.class, concept ).size() < 2
				&& m_pom.getRelationsWithRange( POMInstanceOfRelation.class, concept ).size() < 2 )
			{
				System.out.println( "OWLWriter: skipped axiom "+ disjoint );
				continue;	
			}
		}		
		String sDisjoint = disjoint.getLabel().replaceAll( " ", "_" );
		try {
			Individual domainIndividual = createClass( domain );
			Individual rangeIndividual = createClass( range );
			if ( domainIndividual == null | rangeIndividual == null ){
				System.out.println( "OWLWriter: abandoned axiom: " + sDisjoint +"( "+ domain +", "+ range +" )");
			}
			else
			{
				Individual disjointIndividual = m_factory.individual( m_sLogicalURI +"#"+ sDisjoint +"_"+ (m_objId++) + System.currentTimeMillis()/1000 );

				ClassMember disjointMember = m_factory.classMember( m_disjointClass, disjointIndividual );

				EntityAnnotation disjointAnn = createAnnotation( disjointIndividual, disjoint.getProbability() );
				EntityAnnotation disjointLabel = createAnnotation( disjointIndividual, disjoint.getLabel() );

				// DataPropertyMember domainName = m_factory.dataPropertyMember( m_nameProperty, domainIndividual, sDomain );
				// DataPropertyMember rangeName = m_factory.dataPropertyMember( m_nameProperty, rangeIndividual, sRange );
				// DataPropertyMember disjointName = m_factory.dataPropertyMember( m_nameProperty, disjointIndividual, sDisjoint );

				ObjectPropertyMember propertyDomain = m_factory.objectPropertyMember( m_domainProperty, disjointIndividual, domainIndividual ); 
				ObjectPropertyMember propertyRange = m_factory.objectPropertyMember( m_rangeProperty, disjointIndividual, rangeIndividual ); 

				List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();

				addAxiom( disjointMember, changes ); 
				addAxiom( disjointAnn, changes );
				addAxiom( disjointLabel, changes );

				addAxiom( propertyDomain, changes ); 
				addAxiom( propertyRange, changes ); 

				m_ontology.applyChanges( changes );  

				if( m_REFS ){
					addReferences( domainIndividual, getReferences( domain ) );
					addReferences( rangeIndividual, getReferences( range ) );
					addReferences( disjointIndividual, getReferences( disjoint ) );
				}
			}
		}
		catch( Exception e ){
			System.out.println( "OWLWriter: cannot create axiom "+ sDisjoint +"( "+ domain +", "+ range +" )" );
			e.printStackTrace();
		} 
	}
 
	private void addReferences( Individual object, List<DocumentReference> references ) throws KAON2Exception {  
		for( DocumentReference reference: references )
		{
			String sURI = reference.getDocument().getURI().toString();
			String sName = null;
			if (sURI.contains("."))
				sName = sURI.substring( sURI.lastIndexOf( "/" )+1, sURI.lastIndexOf( "." ) );
			else
				sName = sURI.substring( sURI.lastIndexOf( "/" )+1 );

			Individual referenceIndividual = m_factory.individual( m_sLogicalURI +"#Reference_"+ (m_refId++) + "_doc" + sName );
			ClassMember referenceMember = m_factory.classMember( m_referenceClass, referenceIndividual );
			DataPropertyMember pointsToMember = m_factory.dataPropertyMember( m_pointsToProperty, referenceIndividual, m_factory.constant( sName ) );
			ObjectPropertyMember refersToMember = m_factory.objectPropertyMember( m_refersToProperty, referenceIndividual, object );

			List<OntologyChangeEvent> changes = new ArrayList<OntologyChangeEvent>();
			
			addAxiom( referenceMember, changes );
			addAxiom( pointsToMember, changes );
			addAxiom( refersToMember, changes );
			
			// changes.add( new OntologyChangeEvent( referenceMember, OntologyChangeEvent.ChangeType.ADD ) ); 
			// changes.add( new OntologyChangeEvent( pointsToMember, OntologyChangeEvent.ChangeType.ADD ) ); 
			// changes.add( new OntologyChangeEvent( refersToMember, OntologyChangeEvent.ChangeType.ADD ) ); 
			
			m_ontology.applyChanges( changes );
		}
	}
	
	private void addAxiom( Axiom a, List <OntologyChangeEvent> changes ) throws KAON2Exception {
	   if( !m_ontology.containsAxiom( a, false ) ){
	   	changes.add( new OntologyChangeEvent( a, OntologyChangeEvent.ChangeType.ADD ) ); 
		}
	}
	
	private EntityAnnotation createAnnotation( OWLEntity entity, double dProb ) throws KAON2Exception { 	 
		AnnotationProperty annProp = m_factory.annotationProperty( m_sLogicalURI +"#Rating" );
		EntityAnnotation ann = m_factory.entityAnnotation( annProp, entity, m_factory.constant( dProb ) );
		return ann;
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



