package org.ontoware.text2onto.ontology;

import java.net.URI;

import org.ontoware.text2onto.pom.POM;

import org.ontoware.text2onto.evidence.EvidenceManager;
import org.ontoware.text2onto.reference.ReferenceManager;


public interface OntologyWriter { 

	public abstract void write( URI uri ) throws Exception;
	
	public abstract void setEvidenceManager( EvidenceManager em );
	
	public abstract void setReferenceManager( ReferenceManager rm );
}



