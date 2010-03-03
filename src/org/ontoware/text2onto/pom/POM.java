package org.ontoware.text2onto.pom;
 
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ontoware.text2onto.change.Change; 
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.Changeable;
import org.ontoware.text2onto.change.POMChangeStrategy;


/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public class POM extends Changeable {

	private HashMap m_hmClass2Objects; 
 
	private POMInterface m_pomInterface;
 

	protected POM(){ 
		m_defaultStrategy = new POMChangeStrategy(); 
		m_hmClass2Objects = new HashMap();  
		m_pomInterface = new POMInterface( this );
	}
	
	/************************************************************************************************/
 
	public List getObjects( Class c ){
		List objects = (List)m_hmClass2Objects.get( c );
		if( objects == null ){
			objects = new ArrayList();
		}	
		return objects;
	}
	
	public List getObjects( Class c, String sLabel ){
		return getObjects( c, sLabel, 0.0 );
	}
	
	public List getObjects( Class c, double dMinProb ){
		return getObjects( c, null, dMinProb );
	}
	
	public List getObjects( Class c, String sLabel, double dMinProb ){
		ArrayList al = new ArrayList();
		List objects = getObjects( c );
		Iterator iter = objects.iterator(); 
		while( iter.hasNext() )
		{
			POMAbstractObject object = (POMAbstractObject)iter.next();
			if( ( sLabel == null || object.getLabel().equals( sLabel ) ) 
				&& object.getProbability() >= dMinProb )
			{
				al.add( object );
			}
		}
		return al; 
	}

	public List getObjects(){
		return getObjects( 0.0 );
	}
	 
	public List getObjects( double dMinProb ){
		ArrayList al = new ArrayList();  
		Iterator iter = m_hmClass2Objects.values().iterator();
		while( iter.hasNext() )
		{
			List objects = (List)iter.next();
			Iterator listIter = objects.iterator();
			while( listIter.hasNext() )
			{ 
				POMAbstractObject object = (POMAbstractObject)listIter.next();
				if( object.getProbability() >= dMinProb ){
					al.add( object );
				}	
			}
		}
		return al;
	}
	
	public List<POMAbstractRelation> getRelations( Class c, POMAbstractObject domain, POMAbstractObject range ){
		List<POMAbstractRelation> relations = new ArrayList(); 
		for( Object object: getObjects( c ) )
		{
			POMAbstractRelation relation = (POMAbstractRelation)object;
			if( relation.getDomain().equals( domain ) 
				&& relation.getRange().equals( range ) )
			{
				relations.add( relation );
			}
		}
		return relations;
	}
	
	public List<POMAbstractRelation> getRelationsWithDomain( Class c, POMAbstractObject domain ){
		List<POMAbstractRelation> relations = new ArrayList(); 
		for( Object object: getObjects( c ) )
		{
			POMAbstractRelation relation = (POMAbstractRelation)object;
			if( relation.getDomain().equals( domain ) )
			{
				relations.add( relation );
			}
		}
		return relations;
	}

	public List<POMAbstractRelation> getRelationsWithRange( Class c, POMAbstractObject range ){
		List<POMAbstractRelation> relations = new ArrayList(); 
		for( Object object: getObjects( c ) )
		{
			POMAbstractRelation relation = (POMAbstractRelation)object;
			if( relation.getRange().equals( range ) )
			{
				relations.add( relation );
			}
		}
		return relations;
	}
	
	public List<POMDisjointClasses> getDisjointClasses( List<POMConcept> concepts ){
		List<POMDisjointClasses> disjoints = new ArrayList();
		for( Object object: getObjects( POMDisjointClasses.class ) )
		{
			POMDisjointClasses disjoint = (POMDisjointClasses)object;
			Set<POMConcept> classes = disjoint.getConcepts();
			if( classes.containsAll( concepts ) )
			{
				disjoints.add( disjoint );
			}
		}
		return disjoints;

	}
	
	public List getObjects( POMRequest request ){
		// TODO
		return null;
	}
	
	/************************************************************************************************/
	
	public POMObject newObject( Class c, Object[] parameters ){
		// TODO
		return null;
	}
	
	public POMConcept newConcept( String sLabel ){
		/* List objects = getObjects( POMConcept.class, sLabel );
		if( objects.size() > 0 ){
			return (POMConcept)objects.get(0);
		} */
		return new POMConcept( sLabel ); 
	}
	
	public POMInstance newInstance( String sLabel ){
		/* List objects = getObjects( POMInstance.class, sLabel );
		if( objects.size() > 0 ){
			return (POMInstance)objects.get(0);
		} */
		return new POMInstance( sLabel ); 
	}

	public POMSubclassOfRelation newSubclassOfRelation( POMConcept domain, POMConcept range ){
		/* List relations = getRelations( POMSubclassOfRelation.class, domain, range );
		if( relations.size() > 0 ){
			return (POMSubclassOfRelation)relations.get(0);
		} */
		POMSubclassOfRelation relation = new POMSubclassOfRelation();
		relation.setDomain( domain );
		relation.setRange( range );
		return relation;
	}
	
	public POMInstanceOfRelation newInstanceOfRelation( POMInstance domain, POMConcept range ){
		/* List relations = getRelations( POMInstanceOfRelation.class, domain, range );
		if( relations.size() > 0 ){
			return (POMInstanceOfRelation)relations.get(0);
		} */
		POMInstanceOfRelation relation = new POMInstanceOfRelation();
		relation.setDomain( domain );
		relation.setRange( range );
		return relation;
	}
	
	public POMRelation newRelation( String sLabel, POMConcept domain, POMConcept range ){
		/* List relations = getRelations( POMRelation.class, domain, range );
		if( relations.size() > 0 ){
			return (POMRelation)relations.get(0);
		} */
		POMRelation relation = new POMRelation( sLabel );
		relation.setDomain( domain );
		relation.setRange( range );
		return relation;
	}
	
	public POMRelationInstance newRelationInstance( POMRelation relation, POMInstance domain, POMInstance range ){
		// TODO
		return null;
	}
	
	public POMSimilarityRelation newSimilarityRelation( POMEntity domain, POMEntity range ){
		POMSimilarityRelation rel = new POMSimilarityRelation();
		rel.setDomain( domain );
		rel.setRange( range );
		return rel;
	}	
	
	public POMDisjointClasses newDisjointClasses( List<POMConcept> concepts ){
		POMDisjointClasses dc = new POMDisjointClasses();
		for( POMConcept concept: concepts ){
			dc.addConcept( concept );
		}
		return dc;
	}
	
	public POMSubtopicOfRelation newSubtopicOfRelation( POMConcept domain, POMConcept range ){
		/* List relations = getRelations( POMSubtopicOfRelation.class, domain, range );
		if( relations.size() > 0 ){
			return (POMSubtopicOfRelation)relations.get(0);
		} */
		POMSubtopicOfRelation relation = new POMSubtopicOfRelation();
		relation.setDomain( domain );
		relation.setRange( range );
		return relation;
	}
	
	/************************************************************************************************/
	
	public boolean isEmpty(){
		return ( m_hmClass2Objects.size() == 0 );
	}
	 
	public boolean contains( POMAbstractObject object ){
		return getObjects( object.getClass() ).contains( object );
	}
 
	public String toString(){
		return m_hmClass2Objects.toString();
	}
	
	public String toStringDetails(){
		StringBuffer sb = new StringBuffer();
		Iterator iter = m_hmClass2Objects.keySet().iterator();
		while( iter.hasNext() )
		{
			Class c = (Class)iter.next();
			sb.append( "\n"+ c +"=[" );
			List objects = getObjects( c );
			Iterator objIter = objects.iterator();
			while( objIter.hasNext() )
			{
				POMObject obj = (POMObject)objIter.next();
				String sObject = obj.toString();
				double dProb = obj.getProbability();
				sb.append( sObject +"="+ dProb );
				if( objIter.hasNext() ){
					sb.append( ", " );
				}
			}
			sb.append( "]\n" );
		}
		return sb.toString();
	}
	
	public POMInterface getPOMInterface() {
		return m_pomInterface;
	}

	/************************************************************************************************/

	/* Changeable */
	
	protected Change createChange( ChangeRequest changeRequest ) { 
		Change c;
		if( changeRequest.getType() != Change.Type.REMOVE )
		{
			if( contains( (POMAbstractObject)changeRequest.getObject() ) ){
				c = changeRequest.createChangeWithType( Change.Type.MODIFY );
			} 
			else {
				c = changeRequest.createChangeWithType( Change.Type.ADD );
			}
		} 
		else {
			if( contains( (POMAbstractObject)changeRequest.getObject() ) ){
				c = changeRequest.createChangeWithType( Change.Type.REMOVE );
			}
			else {
				c = null;
			}
		} 
		return c;
	}
    
    protected void processChangeRequests( List<ChangeRequest> changeRequests ) {
        super.processChangeRequests( changeRequests );
    }

    protected void processChangeRequest( ChangeRequest changeRequest ) {
        super.processChangeRequest( changeRequest );
    }
    
	protected void executeChange( Change change ){  
		switch( change.getType() )
		{
			case Change.Type.ADD: 
				executeAdd( change ); 
				break;
			case Change.Type.REMOVE: 
				executeRemove( change ); 
				break;
			case Change.Type.MODIFY: 
				executeModify( change ); 
				break;
		}	
	}

	private void executeAdd( Change change ){
		POMAbstractObject object = (POMAbstractObject)change.getObject(); 
		Class c = object.getClass();
		ArrayList objects = (ArrayList)m_hmClass2Objects.get( c );
		if( objects == null )
		{
			objects = new ArrayList();
			m_hmClass2Objects.put( c, objects );
		}
		object.setInPOM( true );
		objects.add( object ); 
	} 

	private void executeRemove( Change change ){
		POMAbstractObject object = (POMAbstractObject)change.getObject(); 
		Class c = object.getClass();
		ArrayList objects = (ArrayList)m_hmClass2Objects.get( c );
		if( objects != null && objects.contains( object ) )
		{
			object.setInPOM( false );
			objects.remove( object );
		} 
		if( objects.size() == 0 ){
			m_hmClass2Objects.remove( c );
		}
	}
 
	private void executeModify( Change change ){ 
		POMAbstractObject object = (POMAbstractObject)change.getObject(); 
		double dProb = ((Double)change.getValue()).doubleValue();
		List objects = getObjects( object.getClass() ); 
		for( int i=0; i<objects.size(); i++ )
		{
			POMAbstractObject obj = (POMAbstractObject)objects.get(i);
			if( obj.equals( object ) )
			{
				obj.setProbability( dProb );
				break;			 						
			}
		}
	} 
	
	public static POM load( String sFile ) throws Exception { 
		Object object;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try
		{
			fis = new FileInputStream( sFile );
			in = new ObjectInputStream( fis );
			object = in.readObject();
			in.close();
		}
		finally {
			fis.close();
		}
		return (POM)object;
	}
	
	public void save( String sFile ) throws Exception { 
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream( sFile );
			out = new ObjectOutputStream( fos );
			out.writeObject( m_pomInterface.getPOM() );
			out.close();
		} 
		finally {
			fos.close();
		} 
	}
}