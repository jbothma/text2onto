package org.ontoware.text2onto.corpus;

public class TextDocument extends AbstractDocument {

	public int hashCode() {
		if( m_uri == null ){
			return m_sContent.hashCode();
		}
		return m_uri.toString().hashCode();
	}

	public String toString() {
		if( m_uri == null ){
			return m_sContent;
		}
		return m_uri.toString();
	}

//	public boolean equals( Object object ) {
//		if( object == null || !( object instanceof AbstractDocument ) ){
//			return false;
//		}
//		AbstractDocument doc = (AbstractDocument) object;
//		if( doc.getURI() == null || this.getURI() == null )
//		{
//			if( this.getURI() != null ){
//				return false;
//			} 
//			else if( ( (TextDocument) doc ).getContent().equals( this.getContent() ) ) {
//				return true;
//			}
//			else {
//				return false;
//			}
//		}
//		if ( doc.getURI().equals( m_uri ) ){
//			return true;
//		}
//		return false;
//	}
}