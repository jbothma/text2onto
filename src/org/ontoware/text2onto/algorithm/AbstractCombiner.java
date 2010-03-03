package org.ontoware.text2onto.algorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.change.ChangeRequest; 
import org.ontoware.text2onto.pom.POMObject;

/**
 * @author Günter Ladwig
 */
public abstract class AbstractCombiner implements Serializable {

	protected List<List> m_changeLists = new ArrayList<List>();

	protected void reset()
	{
		m_changeLists = new ArrayList<List>();
	}

	protected void addChangeList( List list ) {
		m_changeLists.add( list );
	}

    protected List getChangeList( int iList ) {
		if ( m_changeLists.size() > iList ) {
			return m_changeLists.get( iList );
		}
		return null;
	}
    
    protected List<ChangeRequest> getAllChangeRequests(){
    	List<ChangeRequest> requests = new ArrayList<ChangeRequest>();
    	for( List<ChangeRequest> changeList: m_changeLists ){
    		requests.addAll( changeList );
    	}
    	return requests;
    }
    
    protected List<POMObject> getObjects(){
		List<POMObject> objects = new ArrayList<POMObject>();
		for( List<ChangeRequest> changeList: m_changeLists )
		{
			for( ChangeRequest request: changeList )
			{
				POMObject object = (POMObject)request.getObject();
				if( !objects.contains( object ) ){
					objects.add( object );
				}
			}
		}
		return objects;
	}
	
	protected List<ChangeRequest> getLastChangeRequests( POMObject object ){
		List<ChangeRequest> requests = new ArrayList<ChangeRequest>();
		for( List<ChangeRequest> changeList: m_changeLists )
		{
			for( int i=changeList.size()-1; i>=0; i-- )
			{
 				ChangeRequest request = changeList.get(i);
 				if( request.getObject().equals( object ) )
 				{
 					requests.add( request );
 					break;
 				}
			} 
		}
		return requests;
	}
	
	protected abstract List<ChangeRequest> combine();
}
