package org.ontoware.text2onto.change;
 
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class POMChangeStrategy implements ChangeStrategy {
 
	private double m_dDefault = 1.0; 
 
	private HashMap m_hmChangeType2Factor;  
	
 
	public POMChangeStrategy(){
		m_hmChangeType2Factor = new HashMap();
	} 
	
	public List processChanges( List changes ){
		// TODO
		return changes;
	}
	
	public void setModificationFactor( int iChangeType, double dFactor ){
		m_hmChangeType2Factor.put( new Integer( iChangeType ), new Double( dFactor ) );
	}

	public double getModificationFactor( int iChangeType ){
		Double dFactor = (Double)m_hmChangeType2Factor.get( new Integer( iChangeType ) );
		if( dFactor != null ){
			return dFactor.doubleValue();
		}
		return m_dDefault;
	} 

	public void setDefaultModificationFactor( double dDefault ){
		m_dDefault = dDefault;
	}
	
	public double getDefaultModificationFactor(){
		return m_dDefault;
	}
	
	public Object clone(){
		POMChangeStrategy strategy = new POMChangeStrategy();
		strategy.setDefaultModificationFactor( m_dDefault );
		Iterator iter = m_hmChangeType2Factor.keySet().iterator();
		while( iter.hasNext() )
		{
			int iChangeType = ((Integer)iter.next()).intValue();
			double dFactor = this.getModificationFactor( iChangeType );
			strategy.setModificationFactor( iChangeType, dFactor );
		}
		return strategy;
	}
}