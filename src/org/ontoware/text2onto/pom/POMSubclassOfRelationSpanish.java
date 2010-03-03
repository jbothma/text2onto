package org.ontoware.text2onto.pom;

public class POMSubclassOfRelationSpanish extends POMSubclassOfRelation{

	private String m_OriginalDomain;
	
	private String m_OriginalRange;

	public void setOriginalRange( String sRange ) {
		m_OriginalRange = sRange;
	}
	
	public void setOriginalDomain( String sDomain ) {
		m_OriginalDomain = sDomain;
	}
	
	public String getOriginalRange() {
		return m_OriginalRange;
	}

	public String getOriginalDomain() {
		return m_OriginalDomain;
	}
}