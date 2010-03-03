package org.ontoware.text2onto.pom;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.EvidenceChange;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.reference.document.DocumentReference;


public class POMProbabilityFilter { 

	/** minimum probability */
	private double m_dProb;


	public POMProbabilityFilter( double dProb ){
		m_dProb = dProb;
	}

	private Change getReferences( POM m_pom, POMObject object ){
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
					return cause;
				}
				else
					return null;
			}
		}
		return null;
	}
	
	public POM getFilteredPOM( POM pom ) throws Exception{
	    List objects = pom.getObjects( m_dProb );
	    POMWrapper fpom = new POMWrapper( POMFactory.newPOM() );
	    Iterator iter = objects.iterator();
	    while( iter.hasNext() )
	    {
	            POMObject object = (POMObject)iter.next();
	            ChangeRequest changeRequest =
	                    new ChangeRequest( new  POMChange(Change.Type.ADD, this, (POMAbstractObject) object.deepCopy(), getReferences(pom, object)) );
	            fpom.processChangeRequest( changeRequest );
	    }
	    return fpom.getChangeable();
	}
}