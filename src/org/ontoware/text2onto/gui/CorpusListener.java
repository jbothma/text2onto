package org.ontoware.text2onto.gui;
 
/**
 * @author Johanna Voelker (jvo@aifb.uni-karlsruhe.de)
 */
public interface CorpusListener {

	public final static int ADD = 0;
	
	public final static int REMOVE = 1;
	

	public void corpusChanged( int iMessage, String sFile );

}