package org.ontoware.text2onto.reference.document;

import org.ontoware.text2onto.reference.AbstractReference;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.pom.POMObject;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class DocumentReference extends AbstractReference {
 
	private AbstractDocument m_document;

	private Long[] m_lOffsets; 
	
	private String m_sText;


	public DocumentReference( POMObject object, Object source ){
		m_object = object;
		m_source = source;
		m_lOffsets = new Long[2];
		m_timestamp = System.currentTimeMillis();
	}
	
	public DocumentReference( Object source, AbstractDocument document, Long startOffset, Long endOffset, String sText ){
		this( null, source );
		m_document = document;
		setOffsets( startOffset, endOffset );
		m_sText = sText;
	}
	
	public void setDocument( AbstractDocument document ){
		m_document = document;
	}
 
	public AbstractDocument getDocument(){
		return m_document;
	}

	public void setOffsets( Long startOffset, Long endOffset ){
		m_lOffsets[0] = startOffset;
		m_lOffsets[1] = endOffset;
	}

	public Long getStartOffset(){
		return m_lOffsets[0];
	}

	public Long getEndOffset(){
		return m_lOffsets[1];
	}
	
	public void setText( String sText ){
		m_sText = sText;
	}
	
	public String getText(){
		return m_sText;
	}

	public boolean equals( Object object ){
		if( !( object instanceof DocumentReference ) 
			|| !super.equals( object ) )
		{
			return false;
		}
		DocumentReference reference = (DocumentReference)object;
		return ( reference.getDocument().equals( getDocument() )
			&& reference.getStartOffset().equals( getStartOffset() )
			&& reference.getEndOffset().equals( getEndOffset() ) ); 
	}

	public String toString(){
		return "DocumentReference( "+ m_document +" ["+ m_lOffsets[0].longValue() +","+ m_lOffsets[1].longValue() +"] -> "+ m_sText +" )";
	}
}