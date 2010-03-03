package org.ontoware.text2onto.explanation.reference;

import java.util.List;
import java.util.ArrayList; 

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.explanation.AbstractExplanation; 
import org.ontoware.text2onto.pom.POMAbstractObject;
import org.ontoware.text2onto.pom.POMAbstractRelation;
import org.ontoware.text2onto.corpus.AbstractDocument;


public class ObjectDocumentExplanation extends AbstractExplanation {
 
	private List<AbstractDocument> m_domainDocs;
	
	private List<AbstractDocument> m_rangeDocs;
	
	private List<AbstractDocument> m_commonDocs;
	
	private List<AbstractDocument> m_extraDomain;
	
	private List<AbstractDocument> m_extraRange;
 

	public ObjectDocumentExplanation( POMAbstractObject object, Object source, POMChange change ){
		m_object = object;
		m_source = source;
		m_change = change;
		m_timestamp = System.currentTimeMillis();
		m_domainDocs = new ArrayList<AbstractDocument>();
		m_rangeDocs = new ArrayList<AbstractDocument>();
	}
	
	public void setDocuments( List<AbstractDocument> domainDocs, List<AbstractDocument> rangeDocs ){
		m_domainDocs = domainDocs;
		m_rangeDocs = rangeDocs;
		for( AbstractDocument domainDoc: domainDocs ){
			if( rangeDocs.contains( domainDoc ) ){
				m_commonDocs.add( domainDoc );
			}
			else {
				m_extraDomain.add( domainDoc );
			}
		}
		for( AbstractDocument rangeDoc: rangeDocs ){
			if( domainDocs.contains( rangeDoc ) ){
				// done
			}
			else {
				m_extraRange.add( rangeDoc );
			}
		}
	}
	 
	public List<AbstractDocument> getDomainDocuments(){
		return m_domainDocs;
	}
	
	public List<AbstractDocument> getRangeDocuments(){
		return m_rangeDocs;
	}
	 
	public boolean equals( Object object ){
		if( !( object instanceof ObjectDocumentExplanation ) 
			|| !super.equals( object ) )
		{
			return false;
		}
		ObjectDocumentExplanation explanation = (ObjectDocumentExplanation)object; 
		return ( explanation.getDomainDocuments().equals( m_domainDocs )
			&& explanation.getRangeDocuments().equals( m_rangeDocs ) ); 
	}
	
	public String getText(){
		String s = new String();
		if( !( m_object instanceof POMAbstractRelation ) )
		{
			System.err.println( "ObjectDocumentExplanation: wrong object type" );
			return null;
		}
		POMAbstractRelation relation = (POMAbstractRelation)m_object;
		s += "The "+ relation.getLabel() +" relation between '"+ relation.getDomain() +"' and '"+ relation.getRange() +"' was "; 
		int iChange = m_change.getType();
		if( iChange == Change.Type.ADD ){
			s += "added";
		} else if( iChange == Change.Type.MODIFY ){
			s += "modified";
		} else {
			s += "removed";
		}
		s += ", because both concepts were found in the following documents: "+ m_commonDocs;
		boolean bDomain = m_extraDomain.size() > 0;
		boolean bRange = m_extraRange.size() > 0;
		boolean bTwo = bDomain && bRange;
		if( bTwo ){
			s += "Moreover, ";
		}
		if( bDomain ){
			s += "'"+ relation.getDomain() +"' occurs in the documents "+ m_extraDomain;
		}
		if( bTwo ){
			s += ", and ";
		}
		if( bRange ){
			s += "'"+ relation.getRange() +"' occurs in the documents "+ m_extraRange;
		}
		s += ".";
		return s;
	}
	
	public String toString(){
		return "ObjectDocumentExplanation: domain="+ m_domainDocs +" range="+ m_rangeDocs;
	}
}

