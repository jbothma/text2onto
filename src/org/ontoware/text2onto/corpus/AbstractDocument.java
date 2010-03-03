package org.ontoware.text2onto.corpus;

import java.io.Serializable;
import java.net.URI;

public abstract class AbstractDocument implements Serializable {

	protected URI m_uri;
	protected String m_sContent;


	protected void setURI( URI uri ) {
		m_uri = uri;
	}

	public URI getURI() {
		return m_uri;
	}

	public String toString() {
		return m_uri.toString();
	}
	 
	public int hashCode() {
		return m_uri.toString().hashCode();
	}

	public void setContent( String sContent ) {
		m_sContent = sContent;
	}

	public String getContent() {
		return m_sContent;
	}

//	public boolean equals( Object object ) {
//		if ( object == null || !( object instanceof AbstractDocument ) )
//		{
//			return false;
//		}
//		AbstractDocument doc = (AbstractDocument) object;
//		if ( doc.getURI().equals( m_uri ) )
//		{
//			return true;
//		}
//		return false;
//	}
	public boolean equals( Object object ) {
		if( object == null || !( object instanceof AbstractDocument ) ){
			return false;
		}
		AbstractDocument doc = (AbstractDocument) object;
		if( doc.getURI() == null || this.getURI() == null )
		{
			if( this.getURI() != null ){
				return false;
			} 
			else if( doc.getContent().equals( this.getContent() ) ) {
				return true;
			}
			else {
				return false;
			}
		}
		if ( doc.getURI().equals( m_uri ) ){
			return true;
		}
		return false;
	}
}