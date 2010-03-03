package org.ontoware.text2onto.evidence.subcat;

import java.util.HashMap;


public class SubcatFrameInstance {

	public final static int TRANSITIVE = 0;
	
	public final static int TRANSITIVE_PP = 1;
	
	public final static int INTRANSITIVE_PP = 2;
	 
	
	public final static String SUBJECT = "subject";
	
	public final static String OBJECT = "object";
	
	public final static String POBJECT = "pobject"; 
	
	public final static String PREPOSITION = "preposition";
	

	private int m_iType = -1;
	
	private String m_sVerb = null;

	private HashMap m_hmArg2Value;


	public SubcatFrameInstance( int iType, String sVerb ){
		m_iType = iType;
		m_sVerb = sVerb;
		m_hmArg2Value = new HashMap();
	}

	public int getType(){
		return m_iType;
	}
	
	public String getVerb(){
		return m_sVerb;
	}

	public void setArgument( String sArgument, String sValue ){
		m_hmArg2Value.put( sArgument, sValue );
	}
	
	public String getArgument( String sArgument ){
		return (String)m_hmArg2Value.get( sArgument );
	}
	 
	public String toString(){
		return "SCFI( "+ m_iType +", "+ m_sVerb +", "+ m_hmArg2Value +" )"; 
	}
	 
	public int hashCode(){
		return ( m_iType + m_sVerb ).hashCode();
	}
}