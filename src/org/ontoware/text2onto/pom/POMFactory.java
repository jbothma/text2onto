package org.ontoware.text2onto.pom;
 
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URI;
import java.io.File;
import java.io.IOException;

import org.ontoware.text2onto.change.*;
import org.ontoware.text2onto.ontology.*;


public class POMFactory {

	public static POM newPOM(){
		POM pom = new POM(); 
		return pom;
	}
 
	public static POM newPOM( URI uri ) throws Exception {
		OntologyReader reader = null;
		String sURI = uri.toString();
		System.out.println(sURI);
		if( sURI.endsWith( ".kaon" ) ){
			reader = new KAONReader( uri );
		}
		else if( sURI.endsWith( ".owl" ) ){
			reader = new OWLReader( uri );
		} 
		if( reader != null ){
			return reader.read(); 
		}
		else {
			throw new Exception( "Unknown ontology format: "+ sURI ); 
		} 
	}
	
	public static void merge( POMWrapper pomWrapper1, POMWrapper pomWrapper2 ) throws Exception {
		POM pom1 = pomWrapper1.getChangeable();
		POM pom2 = pomWrapper2.getChangeable();
		List objects = pom2.getObjects();
		List objects2 = pom1.getObjects();
		ArrayList changes = new ArrayList();
		Iterator iter = objects.iterator();
		while( iter.hasNext() )
		{
			POMObject object = (POMObject)iter.next();
			if( pom1.contains( object ) )
			{
				POMObject newobject = (POMObject) object.deepCopy();
				double dProb2 = object.getProbability();
				double dProb1 = ((POMObject) (objects2.get(objects2.indexOf( object )))).getProbability();
				Double dProb = new Double( 0.5 * ( dProb1 + dProb2 ) );
				//System.out.println("setting new rating for " + object.toString() + ":" + dProb);
			    changes.add( new ChangeRequest( new POMChange( Change.Type.MODIFY, POMFactory.class, newobject, dProb, (Change)null ) ) );
			}
			else {
				changes.add( new ChangeRequest( new POMChange( Change.Type.ADD, POMFactory.class, object, (Change)null ) ) );
			}
		}
        // TODO pomwrapper
		pomWrapper1.processChangeRequests( changes );
	}
	
	public static POM merge( List<POM> poms ) throws Exception {
		POM newPOM = newPOM();
		List changes = new ArrayList();
		for( POM pom: poms )
		{
			List objects = pom.getObjects();
			List exObjects = newPOM.getObjects();
			Iterator iter = objects.iterator();
			while( iter.hasNext() )
			{
				POMObject object = (POMObject)iter.next();
				if( newPOM.contains( object ) )
				{
					POMObject newObject = (POMObject) object.deepCopy();
					double dProb = object.getProbability();
					double dExProb = ((POMObject)exObjects.get( exObjects.indexOf( object ) )).getProbability();
					Double dNewProb = new Double( 0.5 * ( dProb + dExProb ) );
					changes.add( new ChangeRequest( new POMChange( Change.Type.MODIFY, POMFactory.class, newObject, dNewProb, (Change)null ) ) );
				}
				else {
					changes.add( new ChangeRequest( new POMChange( Change.Type.ADD, POMFactory.class, object, (Change)null ) ) );
				}
			}
 
		}
		POMWrapper pomWrapper = new POMWrapper( newPOM );
		pomWrapper.processChangeRequests( changes );
		return pomWrapper.getChangeable();
	}
}