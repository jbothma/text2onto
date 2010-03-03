package org.ontoware.text2onto.algorithm.normalizer;

import java.util.List;
import java.util.ArrayList;

import org.ontoware.text2onto.algorithm.AbstractNormalizer;
import org.ontoware.text2onto.change.ChangeRequest;


public class DefaultNormalizer extends AbstractNormalizer {
 
	protected List<ChangeRequest> normalize( List<ChangeRequest> changes ) {
		ArrayList<ChangeRequest> changeRequests = new ArrayList<ChangeRequest>();
		for( ChangeRequest change: changes ){
			changeRequests.add( change );
		}
		return changeRequests;
	}
}
