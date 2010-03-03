package org.ontoware.text2onto.evidence.subcat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.Changeable;
import org.ontoware.text2onto.corpus.AbstractDocument;
import org.ontoware.text2onto.evidence.AbstractEvidenceStore;
import org.ontoware.text2onto.reference.document.DocumentReference;
import org.ontoware.text2onto.util.MyInteger;


public class SubcatStore extends AbstractEvidenceStore {

	private HashMap<String,SubcatFrame> m_hmRelation2Frame;
	 
	// private HashMap<AbstractDocument,List<SubcatFrameInstance>> m_hmDoc2Instances;

	// private HashMap<SubcatFrameInstance,DocumentReference> m_hmInstance2Reference;
	

	public SubcatStore(){
		super();
		m_hmRelation2Frame = new HashMap<String,SubcatFrame>(); 
		// m_hmDoc2Instances = new HashMap<AbstractDocument,List<SubcatFrameInstance>>(); 
		// m_hmInstance2Reference = new HashMap<SubcatFrameInstance,DocumentReference>();
	}
	
	public List<SubcatFrame> getSubcatFrames( SubcatFrameInstance instance ){
		List<SubcatFrame> frames = new ArrayList<SubcatFrame>();
		int iType = instance.getType();
		String sVerb = instance.getVerb();
		String sSubject = instance.getArgument( SubcatFrameInstance.SUBJECT );
		if( iType == SubcatFrameInstance.TRANSITIVE )
		{
			String sObject = instance.getArgument( SubcatFrameInstance.OBJECT );
			String sRelation = sVerb;
			frames.add( (SubcatFrame)m_hmRelation2Frame.get( sRelation ) );
		}
		else if( iType == SubcatFrameInstance.TRANSITIVE_PP )
		{
			String sObject = instance.getArgument( SubcatFrameInstance.OBJECT );
			String sPrep = instance.getArgument( SubcatFrameInstance.PREPOSITION );
			String sPObject = instance.getArgument( SubcatFrameInstance.POBJECT );		
			
			String sRelation1 = sVerb +"_"+ sPrep;
			frames.add( (SubcatFrame)m_hmRelation2Frame.get( sRelation1 ) );
			String sRelation2 = sVerb;
			frames.add( (SubcatFrame)m_hmRelation2Frame.get( sRelation2 ) ); 
		}
		else if( iType == SubcatFrameInstance.INTRANSITIVE_PP )
		{
			String sPrep = instance.getArgument( SubcatFrameInstance.PREPOSITION );
			String sPObject = instance.getArgument( SubcatFrameInstance.POBJECT );
			String sRelation = sVerb +"_"+ sPrep;
			frames.add( (SubcatFrame)m_hmRelation2Frame.get( sRelation ) );
		}
		return frames;
	}
	  
	protected void addSubcatFrameInstance( SubcatFrameInstance instance, DocumentReference reference ){ 
		// m_hmRelation2Frames
		int iType = instance.getType();
		String sVerb = instance.getVerb();
		String sSubject = instance.getArgument( SubcatFrameInstance.SUBJECT );
		if( iType == SubcatFrameInstance.TRANSITIVE )
		{
			String sObject = instance.getArgument( SubcatFrameInstance.OBJECT );
			String sRelation = sVerb;
			SubcatFrame frame = (SubcatFrame)m_hmRelation2Frame.get( sRelation );
			if( frame == null )
			{
				frame = new SubcatFrame( sRelation );
				m_hmRelation2Frame.put( sRelation, frame );	
			} 
			frame.addInstance( sSubject, sObject ); 
		}
		else if( iType == SubcatFrameInstance.TRANSITIVE_PP )
		{
			String sObject = instance.getArgument( SubcatFrameInstance.OBJECT );
			String sPrep = instance.getArgument( SubcatFrameInstance.PREPOSITION );
			String sPObject = instance.getArgument( SubcatFrameInstance.POBJECT );
			
			String sRelation1 = sVerb +"_"+ sPrep;
			SubcatFrame frame1 = (SubcatFrame)m_hmRelation2Frame.get( sRelation1 );
			if( frame1 == null )
			{
				frame1 = new SubcatFrame( sRelation1 );
				m_hmRelation2Frame.put( sRelation1, frame1 );	
			} 
			frame1.addInstance( sSubject, sPObject ); 
			
			String sRelation2 = sVerb;
			SubcatFrame frame2 = (SubcatFrame)m_hmRelation2Frame.get( sRelation2 );
			if( frame2 == null )
			{
				frame2 = new SubcatFrame( sRelation2 );
				m_hmRelation2Frame.put( sRelation2, frame2 );	
			} 
			frame2.addInstance( sSubject, sObject ); 
		}
		else if( iType == SubcatFrameInstance.INTRANSITIVE_PP )
		{
			String sPrep = instance.getArgument( SubcatFrameInstance.PREPOSITION );
			String sPObject = instance.getArgument( SubcatFrameInstance.POBJECT );
			String sRelation = sVerb +"_"+ sPrep;
		
			SubcatFrame frame = (SubcatFrame)m_hmRelation2Frame.get( sRelation );
			if( frame == null )
			{
				frame = new SubcatFrame( sRelation );
				m_hmRelation2Frame.put( sRelation, frame );	
			} 
			frame.addInstance( sSubject, sPObject ); 
		}  
	}	
	
	protected void removeSubcatFrameInstance( SubcatFrameInstance instance ){
		// m_hmRelation2Frames, m_hmRelation2Instances
		int iType = instance.getType();
		String sVerb = instance.getVerb();
		String sSubject = instance.getArgument( SubcatFrameInstance.SUBJECT );
		if( iType == SubcatFrameInstance.TRANSITIVE )
		{
			String sObject = instance.getArgument( SubcatFrameInstance.OBJECT );
			String sRelation = sVerb;
			SubcatFrame frame = (SubcatFrame)m_hmRelation2Frame.get( sRelation );
			if( frame != null )
			{
				frame.removeInstance( sSubject, sObject );
			} 
		}
		else if( iType == SubcatFrameInstance.TRANSITIVE_PP )
		{
			String sObject = instance.getArgument( SubcatFrameInstance.OBJECT );
			String sPrep = instance.getArgument( SubcatFrameInstance.PREPOSITION );
			String sPObject = instance.getArgument( SubcatFrameInstance.POBJECT );
			
			String sRelation1 = sVerb +"_"+ sPrep;
			SubcatFrame frame1 = (SubcatFrame)m_hmRelation2Frame.get( sRelation1 );
			if( frame1 != null )
			{
				frame1.removeInstance( sSubject, sPObject );
			}   
			
			String sRelation2 = sVerb;
			SubcatFrame frame2 = (SubcatFrame)m_hmRelation2Frame.get( sRelation2 );
			if( frame2 != null )
			{
				frame2.removeInstance( sSubject, sObject );	
			}   
		}
		else if( iType == SubcatFrameInstance.INTRANSITIVE_PP )
		{
			String sPrep = instance.getArgument( SubcatFrameInstance.PREPOSITION );
			String sPObject = instance.getArgument( SubcatFrameInstance.POBJECT );
			String sRelation = sVerb +"_"+ sPrep;
		
			SubcatFrame frame = (SubcatFrame)m_hmRelation2Frame.get( sRelation );
			if( frame != null )
			{
				frame.removeInstance( sSubject, sPObject );	
			}  
		}  
	}
	 
	public String toString(){
		return "[ Relation2Frame: "+ toString( m_hmRelation2Frame ) +" ]";
	}
	
	private String toString( HashMap hm ){
		String s = "[";
		Iterator iter = hm.keySet().iterator();
		while( iter.hasNext() )
		{
			Object key = iter.next();
			Object value = hm.get( key );
			s += key +"->"+ value;
			if( iter.hasNext() ){
				s += ", ";
			}
		}
		return s +"]";
	}
	
	/* Changeable */

	protected void executeChange( Change change ){
		switch( change.getType() ){
			case Change.Type.ADD: 
				executeAdd( change );
				break;
			case Change.Type.REMOVE:
				executeRemove( change );
				break;
		}
	}

	protected void executeAdd( Change change ){  
		addSubcatFrameInstance( (SubcatFrameInstance)change.getObject(), (DocumentReference)change.getValue() );
	} 

	protected void executeRemove( Change change ){
		removeSubcatFrameInstance( (SubcatFrameInstance)change.getObject() );
	} 
}