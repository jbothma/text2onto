package org.ontoware.text2onto.evidence;

import java.util.List; 
 
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.Changeable;


public abstract class AbstractEvidenceStore extends Changeable {
   
	protected Change createChange( ChangeRequest changeRequest ) {
        // TODO dummy
        return changeRequest.createChangeWithType( changeRequest.getType() );
    }

    protected void processChangeRequests( List<ChangeRequest> changeRequests ) {
        super.processChangeRequests( changeRequests );
    }

    protected void processChangeRequest( ChangeRequest changeRequest ) {
        super.processChangeRequest( changeRequest );
    }
}

