package org.ontoware.text2onto.algorithm.combiner;

import java.util.List;
import java.util.ArrayList;

import org.ontoware.text2onto.algorithm.AbstractCombiner;
import org.ontoware.text2onto.change.ChangeRequest;


public class DefaultCombiner extends AbstractCombiner {
 
	protected List<ChangeRequest> combine() {
		ArrayList changeRequests = new ArrayList();
		for( List changeList: m_changeLists ){
			changeRequests.addAll( changeList );
		}
		return changeRequests;
	}
}
