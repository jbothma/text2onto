package org.ontoware.text2onto.pom;

import java.io.Serializable;


public interface POMAbstractObject extends Serializable {

	public void setLabel( String sLabel );

	public String getLabel();
	
	public boolean inPOM();
	
	public void setInPOM( boolean bInPOM );
	
	public void setProbability( double dProb );

	public double getProbability();

	public void setUserEvidence( Boolean bCorrect );
	
	public Boolean getUserEvidence();
 
	public String toString();

	public Object clone();

	public boolean equals( Object object );

	public int hashCode();
}