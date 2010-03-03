package org.ontoware.text2onto.change;

import java.io.Serializable;
import java.util.List;

import org.ontoware.text2onto.algorithm.AbstractAlgorithm;

/**
 * @author Günter Ladwig
 */
public interface ChangeableWrapper extends Serializable {

    public Changeable getChangeable();

    public void processChangeRequest( ChangeRequest changeRequest );

    public void processChangeRequests( List<ChangeRequest> changeRequests );
    
	public int getId();
	
	public String getName();
}
