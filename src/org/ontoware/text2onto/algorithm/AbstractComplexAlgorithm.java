package org.ontoware.text2onto.algorithm;

import java.util.List;
 
import org.ontoware.text2onto.change.ChangeRequest;

/**
 * @author Günter Ladwig
 */
public abstract class AbstractComplexAlgorithm extends AbstractAlgorithm {
 
	protected AbstractCombiner m_combiner;
	
	protected abstract List<ChangeRequest> getPOMChanges() throws Exception;
	
	protected List<ChangeRequest> getNormalizedPOMChanges() throws Exception {
		return m_normalizer.normalize( getPOMChanges() );	
	}
	
	/**
	 * sets the combiner to be used to combine the results of the sub algorithms
	 * 
	 * @param combiner
	 *           the combiner to be used
	 */
	public void setCombiner( AbstractCombiner combiner ) {
		m_combiner = combiner;
	}
	
	public AbstractCombiner getCombiner(){
		return m_combiner;
	}
}
