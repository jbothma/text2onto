package org.ontoware.text2onto.evidence.subcat;

import org.ontoware.text2onto.util.MyInteger;

import java.util.ArrayList;
import java.util.HashMap;


public class SubcatFrame {

	public final static int DOMAIN = 0;
	
	public final static int RANGE = 1;
	
 
	private String m_sRelation = null;

	private HashMap<String,MyInteger> m_hmDomain2Freq;
	
	private HashMap<String,MyInteger> m_hmRange2Freq;
	
	private int m_iInstances = 0;


	public SubcatFrame( String sRelation ){ 
		m_sRelation = sRelation;
		m_hmDomain2Freq = new HashMap<String,MyInteger>();
		m_hmRange2Freq = new HashMap<String,MyInteger>();
	}
	 
	public String getRelation(){
		return m_sRelation;
	}
	
	public void addInstance( String sDomain, String sRange ){
		MyInteger iDomain = (MyInteger)m_hmDomain2Freq.get( sDomain );
		if( iDomain == null )
		{
			iDomain = new MyInteger(0);
			m_hmDomain2Freq.put( sDomain, iDomain );
		} 
		iDomain.increase();
		MyInteger iRange = (MyInteger)m_hmRange2Freq.get( sDomain );
		if( iRange == null )
		{
			iRange = new MyInteger(0);
			m_hmRange2Freq.put( sRange, iRange );
		} 
		iRange.increase();
		m_iInstances++;
	}
	
	public void removeInstance( String sDomain, String sRange ){
		MyInteger iDomain = (MyInteger)m_hmDomain2Freq.get( sDomain );
		if( iDomain != null ){
			iDomain.decrease();
		}
		MyInteger iRange = (MyInteger)m_hmRange2Freq.get( sRange );
		if( iRange != null ){
			iRange.decrease();
		}  
		m_iInstances--;
	}
	
	public int getInstances(){
		return m_iInstances;
	}
	
	public String getMostFrequent( int iPosition ){
		if( iPosition == DOMAIN ){
			return getMostFrequent( m_hmDomain2Freq );
		}		
		else {
			return getMostFrequent( m_hmRange2Freq );
		}
	}
	
	private String getMostFrequent( HashMap<String,MyInteger> hmPosition2Freq ){
		int iMax = 0;
		String sMax = null;
		for( String sPosition: hmPosition2Freq.keySet() )
		{
			int iFreq = getFrequency( sPosition, hmPosition2Freq );
			if( iFreq >= iMax ){
				iMax = iFreq;
				sMax = sPosition;
			}
		}
		return sMax;
	}
	
	public int getFrequency( int iPosition, String sPosition ){
		if( iPosition == DOMAIN ){
			return getFrequency( sPosition, m_hmDomain2Freq );
		}		
		else {
			return getFrequency( sPosition, m_hmRange2Freq );
		}
	}
	
	private int getFrequency( String sPosition, HashMap<String,MyInteger> hmPosition2Freq ){
		MyInteger iFreq = (MyInteger)hmPosition2Freq.get( sPosition );
		if( iFreq == null ){
			return 0;
		}
		return iFreq.getValue();
	}
	 
	public String toString(){
		return "SCF( "+ m_sRelation +", domain="+ m_hmDomain2Freq +", range="+ m_hmRange2Freq +" )";
	}
}