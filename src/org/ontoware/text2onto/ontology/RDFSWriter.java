package org.ontoware.text2onto.ontology;
 
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

import edu.unika.aifb.kaon.apionrdf.OIModelImpl;
import edu.unika.aifb.rdf.api.syntax.RDFSerializer;
import edu.unika.aifb.rdf.api.util.RDFManager;
 
import org.ontoware.text2onto.pom.*;
import org.ontoware.text2onto.util.ProbabilityComparator;


public class RDFSWriter extends KAONWriter { 
 
	public RDFSWriter( POM pom ){
		m_pom = pom;
		m_comparator = new ProbabilityComparator(); 
	}

	protected void saveOIModel( URI physicalURI ) throws Exception {
		File file = new File( physicalURI );
		file.createNewFile();
		FileOutputStream out = new FileOutputStream( file );
		RDFSerializer serializer = RDFManager.createSerializer();
		try {
			serializer.serialize( ( (OIModelImpl)m_oimodel ).getModel(), out, "UTF-8" );
		}
		catch( Exception e ){
			out.close();
		}
	}
}



