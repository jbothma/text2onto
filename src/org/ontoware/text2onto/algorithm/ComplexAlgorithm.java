package org.ontoware.text2onto.algorithm;

import java.util.List;

import org.ontoware.text2onto.algorithm.combiner.*;
import org.ontoware.text2onto.change.ChangeRequest;

/**
 * @author Günter Ladwig
 */
public class ComplexAlgorithm extends AbstractComplexAlgorithm {

	public ComplexAlgorithm() {
		this( null );
	}

	public ComplexAlgorithm( String sName ) {
		m_sName = sName;
		m_combiner = new AverageCombiner();
	}

	protected void initialize() {
		// TODO
	}

	// TODO reset
	protected List<ChangeRequest> getPOMChanges() throws ControllerException {
		m_combiner.reset();
		for( AbstractAlgorithm algo : m_algorithms ) {
			List changes = m_algorithmController.execute( algo );
			if( changes != null ) {
				m_combiner.addChangeList( changes );
			}
		}
		return m_combiner.combine();
	}
	 
	public boolean equals( Object o ) {
		if( o == null || ! ( o instanceof ComplexAlgorithm ) ) {
			return false;
		}
		ComplexAlgorithm ca = (ComplexAlgorithm)o;
		if( ! ( this.m_sName.equals( ca.m_sName ) ) ) {
			return false;
		}
		return true;

	}

	public String toString() {
		return m_sName + "( combiner=" + m_combiner.getClass().getName()
				+ " algorithms=" + m_algorithms + " )";
	}
}
