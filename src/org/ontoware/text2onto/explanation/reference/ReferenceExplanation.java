package org.ontoware.text2onto.explanation.reference;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.explanation.AbstractExplanation;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.pom.POMAbstractObject;
import org.ontoware.text2onto.pom.POMAbstractRelation;


public class ReferenceExplanation extends AbstractExplanation {
 
	private ArrayList<DocumentReference> m_references;
 

	public ReferenceExplanation( POMAbstractObject object, Object source, POMChange change ){
		m_object = object;
		m_source = source;
		m_change = change;
		m_timestamp = System.currentTimeMillis();
		m_references = new ArrayList<DocumentReference>();
	}
	
	public void addDocumentReference( DocumentReference reference ){
		m_references.add( reference );
	}
	
	public List<DocumentReference> getReferences(){
		return m_references;
	}
	 
	public boolean equals( Object object ){
		if( !( object instanceof ReferenceExplanation ) 
			|| !super.equals( object ) )
		{
			return false;
		}
		ReferenceExplanation explanation = (ReferenceExplanation)object; 
		return explanation.getReferences().equals( m_references ); 
	}
	
	public String getText(){
		String s = new String();
		if( m_object instanceof POMAbstractRelation )
		{
			POMAbstractRelation relation = (POMAbstractRelation)m_object;
			s += "The "+ relation.getLabel() +" relation between '"+ relation.getDomain() +"' and '"+ relation.getRange() +"' was "; 
		}
		else {
			s += "'"+ m_object +"' was "; 
		}
		int iChange = m_change.getType();
		if( iChange == Change.Type.ADD ){
			s += "added";
		} else if( iChange == Change.Type.MODIFY ){
			s += "modified";
		} else {
			s += "removed";
		}
		s += ", because the following phrases were found in recently changed documents:";
		for( DocumentReference reference: m_references )
		{
			s += "\n\""+ reference.getText() +"\" ("+ reference.getDocument() +")";
			s += " ["+ reference.getStartOffset().longValue() +","+ reference.getEndOffset().longValue() +"]";
		}	
		return s;
	}
	
	public String toString(){
		return "ReferenceExplanation: "+ m_references;
	}
}

