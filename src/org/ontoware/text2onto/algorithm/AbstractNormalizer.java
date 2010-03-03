package org.ontoware.text2onto.algorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ontoware.text2onto.change.ChangeRequest; 

/**
 * @author Johanna Voelker
 */
public abstract class AbstractNormalizer implements Serializable{

	protected abstract List<ChangeRequest> normalize( List<ChangeRequest> changes );
}
