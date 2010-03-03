package org.ontoware.text2onto.ontology;
	
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URI;

import edu.unika.aifb.kaon.api.oimodel.OIModel; 
import edu.unika.aifb.kaon.api.oimodel.Concept;
import edu.unika.aifb.kaon.api.oimodel.Instance;
import edu.unika.aifb.kaon.api.oimodel.Property;
import edu.unika.aifb.kaon.api.oimodel.PropertyInstance;
import edu.unika.aifb.kaon.api.oimodel.Entity;
import edu.unika.aifb.kaon.api.KAONException;
import edu.unika.aifb.kaon.api.KAONManager;
import edu.unika.aifb.kaon.api.oimodel.KAONConnection;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.change.ChangeRequest;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class KAONReader implements OntologyReader { 

	private POMWrapper m_pomWrapper;

	private OIModel m_oimodel;
	 

	public static void main( String args[] ){
		try {
			KAONReader reader = new KAONReader( new URI( args[0] ) );
			System.out.println( reader.read() );
		} 
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public KAONReader( URI physicalURI ) throws KAONException {
		m_pomWrapper = new POMWrapper( POMFactory.newPOM() ); 
		m_oimodel = getOIModel( physicalURI.toString() ); 
	}
 
	/*
	 * TODO: special exception 
	 */
	public POM read() throws KAONException { 
		Set concepts = m_oimodel.getRootConcept().getAllSubConcepts();
		Iterator iter = concepts.iterator();
		while( iter.hasNext() )
		{
			Concept concept = (Concept)iter.next(); 
			List objects = createPOMObjects( concept ); 
			List<ChangeRequest> changes = new ArrayList();
			Iterator objIter = objects.iterator();
			while( objIter.hasNext() )
			{
				POMObject object = (POMObject)objIter.next();
				if( object == null ){
					continue;
				}
				// TODO value correct?
				changes.add( new ChangeRequest( new POMChange( Change.Type.ADD, this, object, 1.0, (Change)null ) ) );
			} 
			m_pomWrapper.processChangeRequests( changes );
		} 
		return m_pomWrapper.getChangeable();
	}

	private List createPOMObjects( Concept concept ) throws KAONException { 
		ArrayList al = new ArrayList();
		if( concept.getURI().startsWith( "http://kaon.semanticweb.org/2001/11/kaon-lexical#" ) ){
			return al;
		}
		if( !concept.equals( m_oimodel.getRootConcept() ) ){
			al.add( createPOMConcept( concept ) ); 
		}
		Set subSet = concept.getSubConcepts(); 
		Iterator iter = subSet.iterator();
		while( iter.hasNext() )
		{
			Concept subConcept = (Concept)iter.next();
			al.add( createPOMSubclassOfRelation( subConcept, concept ) );
		}
		// instances
		Set instances = concept.getInstances();
		iter = instances.iterator();
		while( iter.hasNext() )
		{
			Instance instance = (Instance)iter.next();
			al.add( createPOMInstance( instance ) );
			al.add( createPOMInstanceOfRelation( instance, concept ) );	
			// property instances
			Set properties = instance.getFromPropertyInstances();
			Iterator pIter = properties.iterator();
			while( pIter.hasNext() )
			{
				PropertyInstance propInst = (PropertyInstance)pIter.next(); 
				Object object = propInst.getTargetValue();  
				al.add( createPOMRelationInstance( propInst, instance, concept, object ) ); 
			}
		} 
		// properties
		Set props = concept.getAllPropertiesFromConcept();
		iter = props.iterator();
		while( iter.hasNext() )
		{
			Property prop = (Property)iter.next();
			Set domain = prop.getAllDomainConcepts(); 
			Set range = prop.getAllRangeConcepts(); 
			Iterator dIter = domain.iterator();
			while( dIter.hasNext() )
			{
				Concept concept1 = (Concept)dIter.next();
				Iterator rIter = range.iterator();
				while( rIter.hasNext() )
				{
					Concept concept2 = (Concept)rIter.next();
					al.add( createPOMRelation( prop, concept1, concept2 ) );
				}
			}
		} 
		return al;
	}  

	private String getLabel( Object object ) throws KAONException { 
		String sLabel = null;
		if( object instanceof Entity )
		{
			sLabel = ((Entity)object).getLabel( "http://kaon.semanticweb.org/2001/11/kaon-lexical#en" );
			if( sLabel ==  null )
			{
				sLabel = ((Entity)object).getURI().toString();
				sLabel = sLabel.substring( sLabel.lastIndexOf( "#" ) + 1 );
			}
		}
		else if( object instanceof PropertyInstance ){
			sLabel = getLabel( ((PropertyInstance)object).getProperty() );
		}
		else {
			sLabel = object.toString(); 
		}
		sLabel.replaceAll( "_", " " );
		sLabel = sLabel.toLowerCase();      
		return sLabel;
	}

	private POMConcept createPOMConcept( Object object ) throws KAONException {
		POMConcept concept = m_pomWrapper.getChangeable().newConcept( getLabel( object ) ); 
		concept.setProbability( 1.0 );
		concept.setUserEvidence( true );
		return concept;
	}

	private POMInstance createPOMInstance( Object object ) throws KAONException { 
		POMInstance instance = m_pomWrapper.getChangeable().newInstance( getLabel( object ) ); 
		instance.setProbability( 1.0 );
		instance.setUserEvidence( true );
		return instance;
	}
 
	private POMSubclassOfRelation createPOMSubclassOfRelation( Concept concept1, Concept concept2 ) throws KAONException {
		POMConcept domain = createPOMConcept( concept1 );
		POMConcept range = createPOMConcept( concept2 );
		POMSubclassOfRelation rel = m_pomWrapper.getChangeable().newSubclassOfRelation( domain, range );
		rel.setProbability( 1.0 );
		rel.setUserEvidence( true );
		return rel;   
	}

	private POMInstanceOfRelation createPOMInstanceOfRelation( Instance instance, Concept concept ) throws KAONException {
		POMInstance domain = createPOMInstance( instance );
		POMConcept range = createPOMConcept( concept );
		POMInstanceOfRelation rel = m_pomWrapper.getChangeable().newInstanceOfRelation( domain, range );
		rel.setProbability( 1.0 );
		rel.setUserEvidence( true );
		return rel;   
	}

	private POMRelation createPOMRelation( Property prop, Concept concept1, Object concept2 ) throws KAONException {		
		POMConcept domain = createPOMConcept( concept1 );
		POMConcept range = createPOMConcept( concept2 );
		POMRelation rel = m_pomWrapper.getChangeable().newRelation( getLabel( prop ), domain, range );
		if (rel != null) {
			rel.setProbability( 1.0 );
			rel.setUserEvidence( true );
		}
		return rel;
	}

	private POMRelationInstance createPOMRelationInstance( PropertyInstance propInst, Instance instance, Concept concept, Object object ) throws KAONException { 
		String sObjectConcept = object.getClass().getSimpleName();
		POMRelation relation = createPOMRelation( propInst.getProperty(), concept, sObjectConcept ); 
		POMInstance domain = createPOMInstance( instance ); 
		POMInstance range = createPOMInstance( object ); 
		POMRelationInstance rel = m_pomWrapper.getChangeable().newRelationInstance( relation, domain, range );
		if (rel != null)
			rel.setProbability( 1.0 );
		return rel;
	}

	private OIModel getOIModel( String sPhysicalURI ) throws KAONException {
		HashMap parameters = new HashMap(); 
		parameters.put( KAONManager.KAON_CONNECTION, "edu.unika.aifb.kaon.apionrdf.KAONConnectionImpl" ); 
		KAONConnection connection = KAONManager.getKAONConnection( parameters ); 
		OIModel oimodel = connection.openOIModelPhysical( sPhysicalURI );  
		return oimodel;
	}
}