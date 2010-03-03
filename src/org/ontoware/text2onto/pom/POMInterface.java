package org.ontoware.text2onto.pom;

import java.io.Serializable;
import java.util.List;

import org.ontoware.text2onto.change.ChangeObserver;
import org.ontoware.text2onto.change.ChangeStrategy;

/**
 * @author Günter Ladwig
 */
public class POMInterface implements Serializable {

	private POM m_pom;
	
	public POMInterface( POM pom ){
		m_pom = pom;
	}
	
	protected POM getPOM(){
		return m_pom;
	}
	
	public List getObjects( Class c ){
		return m_pom.getObjects( c );
	}
	
	public List getObjects( Class c, String sLabel ){
		return m_pom.getObjects( c, sLabel );
	}
	
	public List getObjects( Class c, double dMinProb ){
		return m_pom.getObjects( c, dMinProb );
	}
	
	public List getObjects( Class c, String sLabel, double dMinProb ){
		return m_pom.getObjects( c, sLabel, dMinProb );
	}

	public List getObjects(){
		return m_pom.getObjects();
	}
	 
	public List getObjects( double dMinProb ){
		return m_pom.getObjects( dMinProb );
	}
	
	public List getObjects( POMRequest request ){
		return m_pom.getObjects( request );		
	}
	
	public boolean isEmpty(){
		return m_pom.isEmpty();
	}
	 
	public boolean contains( POMAbstractObject object ){
		return m_pom.contains( object );
	}
	
	public POMConcept newConcept( String sLabel ) {
		return m_pom.newConcept( sLabel );
	}
 
	public String toString(){
		return m_pom.toString();
	}

	// Changeable
	
	public ChangeStrategy getChangeStrategy( Class changeClass ){
		return m_pom.getChangeStrategy( changeClass );
	}
		
	public void addChangeObserver( ChangeObserver co ){
		m_pom.addChangeObserver(co);
	}
	
	public List getChangesFor( ChangeObserver co ){
		return m_pom.getChangesFor(co);
	}

	public boolean hasChangesFor( ChangeObserver co ){
		return m_pom.hasChangesFor(co);
	}
	
	public void resetChangesFor( ChangeObserver co ){
		m_pom.resetChangesFor(co);
	} 

	public void resetChanges(){
		m_pom.resetChanges();
	}
}
