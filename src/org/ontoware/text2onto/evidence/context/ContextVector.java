package org.ontoware.text2onto.evidence.context;
 
import java.util.HashMap; 
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;
import java.util.List;

import org.ontoware.text2onto.util.MyInteger;


public class ContextVector {

	private HashMap m_hmFeature2Freq;


	public ContextVector(){
		m_hmFeature2Freq = new HashMap();
	}
	
	public void addFeature( String sFeature ){
		MyInteger iFreq = (MyInteger)m_hmFeature2Freq.get( sFeature );
		if( iFreq == null )
		{
			iFreq = new MyInteger(0);
			m_hmFeature2Freq.put( sFeature, iFreq );
		}
		iFreq.increase();
	}

	public void removeFeature( String sFeature ){
		MyInteger iFreq = (MyInteger)m_hmFeature2Freq.get( sFeature );
		iFreq.decrease();
		if( iFreq.getValue() == 0 ){
			m_hmFeature2Freq.remove( sFeature );
		}
	}

	public List getFeatures(){
		ArrayList al = new ArrayList();
		Iterator iter = m_hmFeature2Freq.keySet().iterator();
		while( iter.hasNext() ){
			al.add( iter.next() );
		}
		return al;
	}

	public int getFrequency( String sFeature ){
		MyInteger iFreq =  (MyInteger)m_hmFeature2Freq.get( sFeature );
		if( iFreq == null ){
			return 0;
		} else {
			return iFreq.getValue();
		} 
	}

	public int size(){
		return m_hmFeature2Freq.size();
	}

	public String toString(){
		String s = "( ";
		Iterator iter = m_hmFeature2Freq.keySet().iterator();
		while( iter.hasNext() )
		{
			String sFeature = (String)iter.next();
			int iFreq = getFrequency( sFeature );
			s += sFeature +"="+ iFreq;
			if( iter.hasNext() ){
				s += ", ";
			}
		}
		s += " )";
		return s;
	}

	public double getCosinusSimilarity( ContextVector cv ){  
		Vector v1 = new Vector();
		Vector v2 = new Vector();
		List features1 = getFeatures();
		Iterator iter = features1.iterator();
		while( iter.hasNext() )
		{
			String sFeature = (String)iter.next();
			int iFreq1 = getFrequency( sFeature );
			int iFreq2 = cv.getFrequency( sFeature );
			v1.add( new Integer( iFreq1 ) );
			v2.add( new Integer( iFreq2 ) );
		}
		List features2 = cv.getFeatures();
		iter = features2.iterator();
		while( iter.hasNext() )
		{
			String sFeature = (String)iter.next();
			if( !features1.contains( sFeature ) )
			{
				int iFreq1 = getFrequency( sFeature );
				int iFreq2 = cv.getFrequency( sFeature );
				v1.add( new Integer( iFreq1 ) );
				v2.add( new Integer( iFreq2 ) );
			}
		}
		double dSum1 = 0.0;
		double dSum2a = 0.0;
		double dSum2b = 0.0;
		for( int i=0; i<v1.size(); i++ )
		{
			double d1 = (double)((Integer)v1.get(i)).intValue();
			double d2 = (double)((Integer)v2.get(i)).intValue();
			dSum1 += d1 * d2;
			dSum2a += d1 * d1;
			dSum2b += d2 * d2;
		}
		dSum2a = Math.sqrt( dSum2a );
		dSum2b = Math.sqrt( dSum2b );
		double dSim = 0.0;
		if( dSum2a > 0.0 && dSum2b > 0.0 ){
			dSim = dSum1 / ( dSum2a * dSum2b );
		}  
		return dSim;
	}
}