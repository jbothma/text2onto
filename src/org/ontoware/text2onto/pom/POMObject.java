package org.ontoware.text2onto.pom;

import java.io.*;
	

public abstract class POMObject implements POMAbstractObject, Cloneable, Serializable { 

	protected String m_sLabel;

	protected double m_dProb = 0.0;

	protected Boolean m_bUser = null;
	
	protected boolean m_bInPOM = false;


	public void setLabel( String sLabel ){
		m_sLabel = sLabel;
	}

	public String getLabel(){
		return m_sLabel;
	}
	
	public boolean inPOM(){
		return m_bInPOM;
	}
	
	public void setInPOM( boolean bInPOM ){
		m_bInPOM = bInPOM;
	}

	public void setProbability( double dProb ){
		m_dProb = dProb; 
	}

	public double getProbability(){ 
		if( m_bUser == null ){
			return m_dProb;
		}
		else if( m_bUser.booleanValue() == true ){
			return 1.0;
		}
		else {
			return 0.0;
		}
	} 

	public void setUserEvidence( Boolean bCorrect ){
		m_bUser = bCorrect;
	}
	
	public Boolean getUserEvidence(){
		return m_bUser;
	}
 
	public abstract String toString();

	public abstract Object clone();

	public abstract boolean equals( Object object );

	public abstract int hashCode();
	
	public Object deepCopy() throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ObjectOutputStream( baos ).writeObject( this );

		ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );

		return new ObjectInputStream(bais).readObject();
	}

}