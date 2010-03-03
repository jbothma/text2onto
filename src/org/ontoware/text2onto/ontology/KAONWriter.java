package org.ontoware.text2onto.ontology;

import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.net.URI;

import edu.unika.aifb.kaon.api.oimodel.*;
import edu.unika.aifb.kaon.api.KAONException;
import edu.unika.aifb.kaon.api.KAONManager;
import edu.unika.aifb.kaon.api.oimodel.KAONConnection;
import edu.unika.aifb.kaon.api.change.AddEntity;
import edu.unika.aifb.kaon.api.change.AddSubConcept;
import edu.unika.aifb.kaon.api.change.RemoveSubConcept;
import edu.unika.aifb.kaon.api.change.AddInstanceOf;
import edu.unika.aifb.kaon.api.change.AddIncludedOIModel;
import edu.unika.aifb.kaon.api.change.EvolutionStrategy;
import edu.unika.aifb.kaon.defaultevolution.EvolutionStrategyImpl;
import edu.unika.aifb.kaon.api.oimodel.LexicalEntry; 
import edu.unika.aifb.kaon.api.util.LexiconUtil;
import edu.unika.aifb.kaon.api.vocabulary.KAONVocabularyAdaptor;

import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.util.ProbabilityComparator;

import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.reference.ReferenceManager;


public class KAONWriter implements OntologyWriter { 

	protected POM m_pom;
	
	protected ProbabilityComparator m_comparator;
	
	protected OIModel m_oimodel;
	 
	protected EvolutionStrategy m_strategy;
	
	protected String m_sLogicalURI = "http://ontoware.org/text2onto/pom";
	
	
	protected KAONWriter(){}

	public KAONWriter( POM pom ){
		m_pom = pom;
		m_comparator = new ProbabilityComparator(); 
	}

	public void setEvidenceManager( EvidenceManager em ){
		// TODO
	}
	
	public void setReferenceManager( ReferenceManager rm ){
		// TODO
	}

	public void write( URI physicalURI ) throws Exception {
		m_oimodel = getOIModel( physicalURI.toString() ); 
		m_strategy = new EvolutionStrategyImpl( m_oimodel );
		addSubclassOf( m_pom.getObjects( POMSubclassOfRelation.class ) );
		addConcepts( m_pom.getObjects( POMConcept.class ) );
		addInstanceOf( m_pom.getObjects( POMInstanceOfRelation.class ) );
		addRelations( m_pom.getObjects( POMRelation.class ) ); 
		saveOIModel( physicalURI.toString() );
	}
	
	protected void addSubclassOf( List relations ) throws KAONException {
		Collections.sort( relations, m_comparator );
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMSubclassOfRelation rel = (POMSubclassOfRelation)iter.next();
			POMConcept domain = (POMConcept)rel.getDomain();
			POMConcept range = (POMConcept)rel.getRange();
			String sRel = rel.getLabel().replaceAll( " ", "_" );
			String sDomain = domain.getLabel().replaceAll( " ", "_" );
			String sRange = range.getLabel().replaceAll( " ", "_" ); 
			if( sDomain.length() < 2 || sRange.length() < 2 ){
				continue;
			}
			try {
			Concept conceptDomain = m_oimodel.getConcept( m_sLogicalURI +"#"+ sDomain );
			Concept conceptRange = m_oimodel.getConcept( m_sLogicalURI +"#"+ sRange );
			ArrayList changes = new ArrayList();
			if( !conceptDomain.isInOIModel() )
			{
				changes.add( new AddEntity( conceptDomain ) );  
				createLexicalEntry( conceptDomain, sDomain, changes ); 
			} 
			if( !conceptRange.isInOIModel() )
			{
				changes.add( new AddEntity( conceptRange ) ); 
				changes.add( new AddSubConcept( m_oimodel.getRootConcept(), conceptRange ) );
				createLexicalEntry( conceptRange, sRange, changes );
			}
			changes.add( new AddSubConcept( conceptRange, conceptDomain ) );
			if( conceptDomain.isSubConceptOf( m_oimodel.getRootConcept() ) ){
				changes.add( new RemoveSubConcept( m_oimodel.getRootConcept(), conceptDomain ) );
			}
			List requestedChanges = m_strategy.computeRequestedChanges( changes );
			m_oimodel.applyChanges( requestedChanges ); 
			} catch( Exception e ){
				System.out.println( "KAONWriter: cannot create relation "+ sRel +"( "+ sDomain +", "+ sRange +" )" );
			}
		} 
	}
	
	protected void addInstanceOf( List relations ) throws KAONException {
		Collections.sort( relations, m_comparator );
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{
			POMInstanceOfRelation rel = (POMInstanceOfRelation)iter.next();
			POMInstance domain = (POMInstance)rel.getDomain();
			POMConcept range = (POMConcept)rel.getRange();
			String sRel = rel.getLabel().replaceAll( " ", "_" );
			String sDomain = domain.getLabel().replaceAll( " ", "_" );
			String sRange = range.getLabel().replaceAll( " ", "_" ); 
			if( sDomain.length() < 2 || sRange.length() < 2 ){
				continue;
			}
			try {
				Instance instanceDomain = m_oimodel.getInstance( m_sLogicalURI +"#"+ sDomain );
				Concept conceptRange = m_oimodel.getConcept( m_sLogicalURI +"#"+ sRange );
				ArrayList changes = new ArrayList();
				if( !instanceDomain.isInOIModel() )
				{
					changes.add( new AddEntity( instanceDomain ) );  
					createLexicalEntry( instanceDomain, sDomain, changes ); 
				} 
				if( !conceptRange.isInOIModel() )
				{
					changes.add( new AddEntity( conceptRange ) ); 
					changes.add( new AddSubConcept( m_oimodel.getRootConcept(), conceptRange ) );
					createLexicalEntry( conceptRange, sRange, changes );
				}
				if( instanceDomain.getParentConcepts().size() == 1 ){
					changes.add( new AddInstanceOf( conceptRange, instanceDomain ) ); 
				}
				List requestedChanges = m_strategy.computeRequestedChanges( changes );
				m_oimodel.applyChanges( requestedChanges ); 
			} 
			catch( Exception e ){
				System.out.println( "KAONWriter: cannot create relation "+ sRel +"( "+ sDomain +", "+ sRange +" )" );
			}
		} 
	}
	
	protected void addConcepts( List concepts ) throws KAONException {
		Collections.sort( concepts, m_comparator );
		Iterator iter = concepts.iterator();
		while( iter.hasNext() )
		{ 
			POMConcept pomConcept = (POMConcept)iter.next(); 
			String sConcept = pomConcept.getLabel().replaceAll( " ", "_" );
			if( sConcept.length() < 2 ){
				continue;
			}
			try {
				Concept concept = m_oimodel.getConcept( m_sLogicalURI +"#"+ sConcept );
				ArrayList changes = new ArrayList();
				if( !concept.isInOIModel() )
				{
					changes.add( new AddEntity( concept ) );
					changes.add( new AddSubConcept( m_oimodel.getRootConcept(), concept ) );
					createLexicalEntry( concept, sConcept, changes ); 
				}  
				List requestedChanges = m_strategy.computeRequestedChanges( changes );
				m_oimodel.applyChanges( requestedChanges ); 
			} 
			catch( Exception e ){
				System.out.println( "KAONWriter: cannot create concept "+ sConcept );
			}
		} 
	}
	
	protected void addRelations( List relations ) throws KAONException {
		Collections.sort( relations, m_comparator );
		Iterator iter = relations.iterator();
		while( iter.hasNext() )
		{ 
			POMRelation pomRelation = (POMRelation)iter.next(); 
			// TODO
		} 
	}
	
	protected void createLexicalEntry( Entity entity, String sEntity, List changes ) throws KAONException { 
		LexicalEntry le = m_oimodel.getLexicalEntry( m_oimodel.createNewURI() );
		LexiconUtil.createLexicalEntry( le,
			KAONVocabularyAdaptor.INSTANCE.getKAONLabel(),
			sEntity,
			KAONVocabularyAdaptor.INSTANCE.getLanguageURI( "en" ),
			entity,
			changes ); 
	}

	protected OIModel getOIModel( String sPhysicalURI ) throws KAONException { 
		HashMap parameters = new HashMap(); 
		parameters.put( KAONManager.KAON_CONNECTION, "edu.unika.aifb.kaon.apionrdf.KAONConnectionImpl" ); 
		KAONConnection connection = KAONManager.getKAONConnection( parameters );
		OIModel oimodel = null; 
		try {
			oimodel = connection.createOIModel( sPhysicalURI, m_sLogicalURI );
			OIModel lexicalOIModel = connection.openOIModelLogical( KAONConnection.LEXICAL_OIMODEL_URI );
			oimodel.applyChanges( Collections.singletonList( new AddIncludedOIModel( oimodel, null, lexicalOIModel ) ) ); 
		}
		catch ( KAONException e ){
			connection.close();
			throw e;
		} 
		return oimodel;
	}
	
	protected void saveOIModel( String sPhysicalURI ) throws Exception { 
		File file = new File( new URI( sPhysicalURI ) );
		file.createNewFile();
		m_oimodel.save();
	}
}



