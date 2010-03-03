package org.ontoware.text2onto.change;

import java.io.Serializable;
import java.util.List;

/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public interface ChangeStrategy extends Serializable {

	public List processChanges( List changes );

	public Object clone();
}


