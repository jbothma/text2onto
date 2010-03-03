package org.ontoware.text2onto.gui;
 
import java.util.List; 

import org.ontoware.text2onto.pom.POMObject;
 
/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public interface POMListener {

	public final static int ADD = 0;
	
	public final static int REMOVE = 1;
	

	public void pomChanged( int iMessage, List<? extends POMObject> objects );

}