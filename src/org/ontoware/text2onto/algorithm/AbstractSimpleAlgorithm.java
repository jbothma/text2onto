package org.ontoware.text2onto.algorithm;
 
import java.util.List;

import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.explanation.AbstractExplanation;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public abstract class AbstractSimpleAlgorithm extends AbstractAlgorithm {

	protected abstract List<ChangeRequest> getEvidenceChanges() throws Exception;
	 
	protected abstract AbstractExplanation getExplanation( POMChange change ) throws Exception;
	
	protected abstract List<ChangeRequest> getPOMChanges() throws Exception;
	
	protected abstract List<ChangeRequest> getReferenceChanges() throws Exception;	
	
	protected List<ChangeRequest> getNormalizedPOMChanges() throws Exception { 
		return m_normalizer.normalize( getPOMChanges() );	
	}
}
