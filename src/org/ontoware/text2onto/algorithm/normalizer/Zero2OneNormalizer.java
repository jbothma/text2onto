package org.ontoware.text2onto.algorithm.normalizer;

import java.util.List;
import java.util.ArrayList;

import org.ontoware.text2onto.algorithm.AbstractNormalizer;
import org.ontoware.text2onto.change.ChangeRequest;
import org.ontoware.text2onto.change.Change;
import org.ontoware.text2onto.change.POMChange;
import org.ontoware.text2onto.pom.POMObject;


public class Zero2OneNormalizer extends AbstractNormalizer {

	private double dNewMin = 0.0;
	
	private double dNewMax = 1.0;

	/*
	 * v' = [ ( v - min ) / ( max - min ) ] * ( new_max - new_min ) + min_new
	 * v' = ( v - min ) / ( max - min ) fuer new_max = 1 und new_min = 0
	 */
	protected List<ChangeRequest> normalize( List<ChangeRequest> requests ) { 
		ArrayList<ChangeRequest> changeRequests = new ArrayList<ChangeRequest>();
		double dMin = Double.MAX_VALUE;
		double dMax = Double.MIN_VALUE;
		for( ChangeRequest request: requests )
		{  
			double dValue = ((Double)request.getValue()).doubleValue();
			if( dValue < dMin ){
				dMin = dValue;
			}
			else if( dValue > dMax ){
				dMax = dValue;
			}
		} 
		System.out.println( "Zero2OneNormalizer: min="+ dMin +" max="+ dMax );
		for( ChangeRequest request: requests )
		{  
			POMObject object = (POMObject)request.getObject();
			double dValue = ((Double)request.getValue()).doubleValue();
			double dNorm;
			if( dMin == dMax ){
				dNorm = dValue;
			} 
			else {
				dNorm = ( ( dValue - dMin ) / ( dMax - dMin ) ) * ( dNewMax - dNewMin ) + dNewMin;
			}
			// TODO!
			object.setProbability( dNorm ); 
			Change change = request.createChangeWithType( request.getType() );
			ChangeRequest newRequest = new ChangeRequest( new POMChange( Change.Type.MODIFY, this, object, dNorm, change ), request ); 
			// DEBUG
			if( dNorm > 0 ){
				changeRequests.add( newRequest );
			}
		}	
		return changeRequests;
	} 
}
